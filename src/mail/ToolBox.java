package mail;

import java.io.*;
import java.sql.*;
import java.util.*;

import common.*;
import database.*;
public class ToolBox
{
	public static int edit(Integer id,Account account,Mail mail)
	{
		try
		{
			StringBuilder sb=new StringBuilder();
			for(String receiver:mail.receivers)
				sb.append(receiver).append(";");
			if(sb.length()>0)
				sb.deleteCharAt(sb.length()-1);
			if(id==null)
			{
				int newid=CRUD.insertMail(account!=null?account.username:null,"D",mail.date!=null?new Timestamp(mail.date.getTime()):null,null,mail.sender,sb.length()>0?sb.toString():null,mail.subject);
				mail.store(new File(Shared.MAILROOT+newid+".eml"));
				return newid;
			}
			else
			{
				if(!"D".equals(CRUD.selectMail(id).values.get(0).get("status")))
					return -1;
				if(CRUD.updateMail(id,account!=null?account.username:null,"D",mail.date!=null?new Timestamp(mail.date.getTime()):null,null,mail.sender,sb.length()>0?sb.toString():null,mail.subject))
				{
					mail.store(new File(Shared.MAILROOT+id+".eml"));
					return id;
				}
				else
					return -1;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}
	public static boolean send(int id)
	{
		try
		{
			boolean flag=false;
			SMTP smtp=new SMTP();
			Mail mail=new Mail(new File(Shared.MAILROOT+id+".eml"));
			Result result;
			result=CRUD.selectMail(id);
			Map<String,Object> map=result.values.get(0);
			if(!"D".equals(map.get("status")))
				return false;
			Account account=Account.getByName((String)map.get("username"));
			if(!smtp.connect(account.smtpAddress,account.smtpPort))
			{
				System.err.println("Connection failed!");
				return false;
			}
			for(String login:smtp.ehlo("muzhik"))
			{
				if(login.contains("AUTH"))
				{
					flag=true;
					break;
				}
			}
			if(!flag)
			{
				System.err.println("SMTP server does not support plain text login!");
				return false;
			}
			if(!smtp.login(account.username,account.password))
			{
				if(!smtp.login(account.username.split("@")[0],account.password))
				{
					System.err.println("Login failed!");
					return false;
				}
			}
			if(!smtp.mailFrom(mail.sender)) return false;
			flag=false;
			for(String receiver:mail.receivers)
				flag|=smtp.rcptTo(receiver);
			if(!flag)
			{
				System.err.println("Relay not allowed!");
				return false;
			}
			String uid=smtp.data(mail.toString());
			if(uid==null)
			{
				System.err.println("Sending data failed!");
				return false;
			}
			StringBuilder sb=new StringBuilder();
			for(String receiver:mail.receivers)
				sb.append(receiver).append(";");
			if(sb.length()>0)
				sb.deleteCharAt(sb.length()-1);
			if(!CRUD.updateMail(id,account.username,"S",mail.date!=null?new Timestamp(mail.date.getTime()):null,uid,mail.sender,sb.length()>0?sb.toString():null,mail.subject))
				return false;
			smtp.quit();
			smtp.disconnect();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	public static boolean send(Account account,Mail mail)
	{
		return send(edit(null,account,mail));
	}
	public static boolean receive(Account account)
	{
		try
		{
			POP3 pop3=new POP3();
			if(!pop3.connect(account.pop3Address,account.pop3Port))
			{
				System.err.println("Connection failed!");
				return false;
			}
			if(!pop3.login(account.username,account.password,"PLAIN"))
			{
				System.err.println("Login failed!");
				return false;
			}
			List<List<Integer>> mailList=pop3.list();
			for(List<Integer> mail:mailList)
			{
				int no=mail.get(0);
				String msg=pop3.retr(no);
				Mail m=new Mail(msg);
				String uid=pop3.uidl(no);
				int id;
				Result result=CRUD.selectMail(account.username,"R",uid);
				if(result.rows>0)
					id=(int)result.values.get(0).get("id");
				else
				{
					StringBuilder sb=new StringBuilder();
					for(String receiver:m.receivers)
						sb.append(receiver).append(";");
					if(sb.length()>0)
						sb.deleteCharAt(sb.length()-1);
					id=CRUD.insertMail(account.username,"R",new Timestamp(m.date.getTime()),uid,m.sender,sb.length()>0?sb.toString():null,m.subject);
				}
				if(id<=0)
					return false;
				m.store(new File(Shared.MAILROOT+id+".eml"));
				pop3.dele(no);
			}
			pop3.quit();
			pop3.disconnect();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	public static boolean delete(int id)
	{
		if(CRUD.deleteMail(id))
			return (new File(Shared.MAILROOT+id+".eml")).delete();
		else
			return false;
	}
	public static Mail take(int id)
	{
		return new Mail(new File(Shared.MAILROOT+id+".eml"));
	}
}
