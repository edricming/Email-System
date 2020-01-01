package mail;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
class SMTP
{
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
			if(checkLine()==220) return true;
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
	private int checkLine()
	{
		lastLine=sc.nextLine();
		System.err.println(lastLine);
		return Integer.parseInt(lastLine.split(" ")[0]);
	}
	private void writeLines(String str)
	{
		String[] lines=str.split("\r?\n");
		for(String line:lines)
		{
			if(".".equals(line))
				pw.print(".");
			pw.println(line);
		}
		pw.println(".");
		pw.flush();
	}
	private void writeLine(String str)
	{
		pw.println(str);
		pw.flush();
	}
	boolean helo()
	{
		writeLine("HELO "+"muzhik");
		return checkLine()==250;
	}
	boolean login(String username,String password)
	{
		String username2=Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.US_ASCII)),
			password2=Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.US_ASCII));
		writeLine("AUTH LOGIN");
		if(checkLine()!=334) return false;
		writeLine(username2);
		if(checkLine()!=334) return false;
		writeLine(password2);
		return checkLine()==235;
	}
	boolean quit()
	{
		writeLine("QUIT");
		return checkLine()==221;
	}
	boolean mailFrom(String address)
	{
		writeLine("MAIL FROM:<"+address+">");
		return checkLine()==250;
	}
	boolean rcptTo(String address)
	{
		writeLine("RCPT TO:<"+address+">");
		int value=checkLine();
		return value==250||value==251;
	}
	String data(String data)
	{
		writeLine("DATA");
		if(checkLine()!=354) return null;
		writeLines(data);
		if(checkLine()==250)
		{
			String[] strs=lastLine.split(" ");
			return strs[strs.length-1];
		}
		else
			return null;
	}
	boolean rset()
	{
		writeLine("RSET");
		return checkLine()==250;
	}
	boolean noop()
	{
		writeLine("NOOP");
		return checkLine()==250;
	}
	List<String> ehlo(String name)
	{
		writeLine("EHLO "+name);
		List<String> list=new ArrayList<>();
		while(true)
		{
			String line=sc.nextLine();
			String[] strs=line.split("-");
			if(strs.length==2)
			{
				if(Integer.parseInt(strs[0])!=250) return null;
				list.add(strs[1]);
			}
			else if(strs.length==1)
			{
				int idx=line.indexOf(" ");
				if(Integer.parseInt(line.substring(0,idx))!=250) return null;
				list.add(line.substring(idx+1));
				break;
			}
			else
				return null;
		}
		return list;
	}
	boolean vrfy(String address)
	{
		writeLine("VRFY "+address);
		return checkLine()/100==2;
	}
}
