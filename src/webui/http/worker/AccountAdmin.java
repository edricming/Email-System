package webui.http.worker;

import java.io.*;
import java.util.*;

import common.*;
import database.*;
import webui.http.*;
public class AccountAdmin extends Handler
{
	@Override
	public void doGet(Request request,Response response)
	{
		PrintWriter pw=response.getWriter();
		response.setContentType("application/json");
		List<Map<String,Object>> list=CRUD.selectUser().values;
		if("select".equals(request.getParameter("operation")))
		{
			String username=request.getParameter("username");
			if(!username.isEmpty())
			{
				for(Map<String,Object> map:list)
				{
					if(username.equals((map).get("username")))
					{
						List<Map<String,Object>> l=new ArrayList<>();
						l.add(map);
						pw.write(new JSON(l).toString());
						break;
					}
				}
			}
			else
				pw.write(new JSON(list).toString());
		}
		else
		{
			List<String> usernames=new ArrayList<>();
			for(Map<String,Object> map:list)
				usernames.add((String)map.get("username"));
			pw.write(new JSON(usernames).toString());
		}
		pw.flush();
		pw.close();
	}
	@Override
	public void doPost(Request request,Response response)
	{
		PrintWriter pw=response.getWriter();
		String username,password,smtp,pop3,pusername;
		username=request.getParameter("username");
		password=request.getParameter("password");
		smtp=request.getParameter("smtp");
		pop3=request.getParameter("pop3");
		pusername=request.getParameter("pusername");
		boolean success=false;
		switch(request.getParameter("operation"))
		{
			case "insert":
				username=username.isEmpty()?null:username;
				password=password.isEmpty()?null:password;
				smtp=smtp.isEmpty()?null:smtp;
				pop3=pop3.isEmpty()?null:pop3;
				success=CRUD.insertUser(username,password,smtp,pop3);
				break;
			case "delete":
				pusername=pusername.isEmpty()?null:pusername;
				success=CRUD.deleteUser(pusername);
				break;
			case "update":
				username=username.isEmpty()?null:username;
				password=password.isEmpty()?null:password;
				smtp=smtp.isEmpty()?null:smtp;
				pop3=pop3.isEmpty()?null:pop3;
				pusername=pusername.isEmpty()?null:pusername;
				success=CRUD.updateUser(pusername,username,password,smtp,pop3);
				break;
		}
		List<Boolean> successes=new ArrayList<>();
		successes.add(success);
		pw.write(new JSON(successes).toString());
		pw.flush();
		pw.close();
	}
}
