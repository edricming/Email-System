package mail;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import common.*;
class POP3
{
	private static final String OK="+OK",ERR="-ERR";
	private Socket socket;
	private Scanner sc;
	private PrintWriter pw;
	private String lastLine;

	boolean connect(InetAddress inetAddress,int port)
	{
		disconnect();
		try
		{
			socket=new Socket(inetAddress,port);
			sc=new Scanner(new BufferedInputStream(socket.getInputStream()));
			pw=new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
			if(checkLine()) return true;
			else
			{
				disconnect();
				return false;
			}
		}
		catch(Exception e)
		{
			disconnect();
			return false;
		}
	}
	void disconnect()
	{
		try
		{
			if(sc!=null)
				sc.close();
			if(pw!=null)
			{
				pw.flush();
				pw.close();
			}
			if(socket!=null)
				socket.close();
		}
		catch(Exception e){}
	}
	private boolean checkLine()
	{
		lastLine=sc.nextLine();
		System.err.println(lastLine);
		return OK.equals(lastLine.split(" ")[0]);
	}
	private String readLines()
	{
		StringBuilder sb=new StringBuilder();
		String str;
		while(true)
		{
			str=sc.nextLine();
			if(".".equals(str))
				break;
			else if(str.startsWith("."))
				str=str.substring(1);
			sb.append(str).append("\r\n");
		}
		if(sb.length()<2)
			return "";
		return sb.substring(0,sb.length()-2);
	}
	private void writeLine(String str)
	{
		pw.println(str);
		pw.flush();
	}
	boolean login(String username,String password,String method)
	{
		switch(method)
		{
			case "PLAIN":
				writeLine("USER "+username);
				if(!checkLine()) return false;
				writeLine("PASS "+password);
				if(!checkLine()) return false;
				break;
			case "APOP":
				Matcher matcher=Pattern.compile("(<.+>)").matcher(lastLine);
				if(!matcher.find())
					return false;
				String timestamp=matcher.group(0);
				String password2=MD5.MD5Encode(timestamp+password,"US-ASCII");
				writeLine("APOP "+username+","+password2);
				if(!checkLine()) return false;
				break;
		}
		return true;
	}
	boolean quit()
	{
		writeLine("QUIT");
		return checkLine();
	}
	boolean rset()
	{
		writeLine("RSET");
		return checkLine();
	}
	boolean noop()
	{
		writeLine("NOOP");
		return checkLine();
	}
	boolean dele(int msg)
	{
		writeLine("DELE "+msg);
		return checkLine();
	}
	List<Integer> stat()
	{
		writeLine("STAT");
		if(!checkLine()) return null;
		String[] strs=lastLine.split(" ");
		List<Integer> list=new ArrayList<>();
		list.add(Integer.parseInt(strs[1]));
		list.add(Integer.parseInt(strs[2]));
		return list;
	}
	List<List<Integer>> list()
	{
		writeLine("LIST");
		String[] lines=readLines().split("\r\n"),strs=lines[0].split(" ");
		if(!OK.equals(strs[0]))
			return null;
		List<List<Integer>> list=new ArrayList<>();
		for(int i=1;i<lines.length;i++)
		{
			strs=lines[i].split(" ");
			List<Integer> list2=new ArrayList<>();
			list2.add(Integer.parseInt(strs[0]));
			list2.add(Integer.parseInt(strs[1]));
			list.add(list2);
		}
		return list;
	}
	int list(int msg)
	{
		writeLine("LIST "+msg);
		if(!checkLine()) return -1;
		String[] strs=lastLine.split(" ");
		return Integer.parseInt(strs[2]);
	}
	String uidl(int msg)
	{
		writeLine("UIDL "+msg);
		if(!checkLine()) return null;
		String[] strs=lastLine.split(" ");
		return strs[2];
	}
	String retr(int msg)
	{
		writeLine("RETR "+msg);
		String lines=readLines();
		int index=lines.indexOf("\r\n");
		String line1=lines.substring(0,index),rest=lines.substring(index+2);
		if(OK.equals(line1.split(" ")[0]))
			return rest;
		else
			return null;
	}
	String top(long msg,long n)
	{
		writeLine("TOP "+msg+" "+n);
		String lines=readLines();
		int index=lines.indexOf("\r\n");
		String line1=lines.substring(0,index),rest=lines.substring(index+2);
		if(OK.equals(line1.split(" ")[0]))
			return rest;
		else
			return null;
	}
}
