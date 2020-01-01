package webui.http.worker;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import common.*;
import database.*;
import mail.*;
import webui.http.*;
public class MailAdmin extends Handler
{
	@Override
	public void doGet(Request request,Response response) throws HTTPException
	{
		PrintWriter pw=response.getWriter();
		response.setContentType("application/json");
		List<Map<String,Object>> list;
		switch(request.getParameter("operation"))
		{
			case "select":
				String username=request.getParameter("username"),status=request.getParameter("status");
				list=CRUD.selectMail(username.isEmpty()?null:username,status.isEmpty()?null:status,null).values;
				pw.write(new JSON(list).toString());
				break;
			case "view":
				Mail mail=ToolBox.take(Integer.parseInt(request.getParameter("id")));
				Map<String,Object> map=new HashMap<>();
				map.put("sender",mail.sender);
				StringBuilder sb=new StringBuilder();
				for(String receiver:mail.receivers)
					sb.append(receiver).append(";");
				if(sb.length()>0)
					sb.deleteCharAt(sb.length()-1);
				map.put("receivers",sb.toString());
				map.put("subject",mail.subject);
				map.put("date",mail.date.toString());
				List<String> attachments=new ArrayList<>();
				if(mail.attachments!=null)
					for(Mail m:mail.attachments)
						attachments.add(m.subject);
				map.put("attachments",attachments);
				map.put("content",(mail.content));
				map.put("raw",mail.toString());
				pw.write(new JSON(map).toString());
				break;
		}
		pw.flush();
		pw.close();
	}
	@Override
	public void doPost(Request request,Response response) throws HTTPException
	{
		PrintWriter pw=response.getWriter();
		List<Thread> threads=new ArrayList<>();
		List<Boolean> successes=new ArrayList<>();
		boolean flag=false;
		switch(request.getParameter("operation"))
		{
			case "retrieve":
				String username=request.getParameter("username");
				List<String> usernames=new ArrayList<>();
				if(username.isEmpty())
				{
					for(Map<String,Object> map : CRUD.selectUser().values)
						usernames.add((String)map.get("username"));
				}
				else
					usernames.add(username);
				for(int i=0;i<usernames.size();i++)
				{
					successes.add(false);
					Map<String,Object> map=CRUD.selectUser().values.get(i);
					final int j=i;
					threads.add(new Thread(()->{
						successes.set(j,ToolBox.receive(Account.getByName((String)map.get("username"))));
					}));
				}
				for(Thread thread:threads)
					//Shared.THREAD_POOL.execute(thread);
					thread.start();
				try
				{
					for(Thread thread:threads)
						thread.join();
				}
				catch(InterruptedException e){}
				break;
			case "send":
				flag=true;
			case "update":
				Mail mail=new Mail();
				Matcher matcher;
				matcher=Mail.pattern.matcher(request.getParameter("sender"));
				if(matcher.find())
					mail.sender=matcher.group(0);
				mail.receivers=new ArrayList<>();
				matcher=Mail.pattern.matcher(request.getParameter("receivers"));
				while(matcher.find())
					mail.receivers.add(matcher.group(0));
				mail.subject=request.getParameter("subject");
				mail.date=new Date();
				mail.content=request.getParameter("content");
				mail.compose();
				String id=request.getParameter("id");
				Integer i=id.isEmpty()?null:Integer.parseInt(id);
				if(!flag)
					successes.add(ToolBox.edit(i,Account.getByName(request.getParameter("username")),mail)>0);
				else if(i!=null)
					successes.add(ToolBox.send(i));
				else
					successes.add(ToolBox.send(Account.getByName(request.getParameter("username")),mail));

				break;
			case "delete":
				successes.add(ToolBox.delete(Integer.parseInt(request.getParameter("id"))));
				break;
		}
		pw.write(new JSON(successes).toString());
		pw.flush();
		pw.close();
	}
}
