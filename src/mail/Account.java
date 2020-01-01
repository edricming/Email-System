package mail;

import java.net.*;
import java.util.*;

import database.*;
public class Account
{
	public final String username,password;
	public final InetAddress smtpAddress,pop3Address;
	public final int smtpPort,pop3Port;
	public static Account getByName(String username)
	{
		Result result=CRUD.selectUser(username);
		if(result==null||result.rows==0)
			return null;
		Map<String,Object> map=result.values.get(0);
		return new Account(username,(String)map.get("password"),(String)map.get("smtp"),(String)map.get("pop3"));
	}
	private Account(String username,String password,String smtpServer,String pop3Server)
	{
		this.username=username;
		this.password=password;
		InetAddress smtpAddress,pop3Address;
		String[] strs=smtpServer.split(":");
		if(strs.length>1)
			smtpPort=Integer.parseInt(strs[1]);
		else
			smtpPort=25;
		try
		{
			smtpAddress=InetAddress.getByName(strs[0]);
		}
		catch(Exception e)
		{
			smtpAddress=null;
		}
		this.smtpAddress=smtpAddress;
		strs=pop3Server.split(":");
		if(strs.length>1)
			pop3Port=Integer.parseInt(strs[1]);
		else
			pop3Port=110;
		try
		{
			pop3Address=InetAddress.getByName(strs[0]);
		}
		catch(Exception e)
		{
			pop3Address=null;
		}
		this.pop3Address=pop3Address;
//		if(getByName(username)==null)
//			CRUD.insertUser(username,password,smtpServer,pop3Server);
//		else
//			CRUD.updateUser(username,username,password,smtpServer,pop3Server);
	}
//	public boolean remove()
//	{
//		return CRUD.deleteUser(username);
//	}
}
