package webui;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import common.*;
public class Server
{
	private final Map<ServerSocket,Listener> map=new HashMap<>();
	public boolean add(String address,Container container)
	{
		String[] strs=address.split(":");
		try
		{
			if(strs.length==2)
				addListener(InetAddress.getByName(strs[0]),Integer.parseInt(strs[1]),container);
			else if(strs.length==1)
				addListener(InetAddress.getByName("0.0.0.0"),Integer.parseInt(strs[0]),container);
			else
				return false;
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	public boolean remove(String address)
	{
		String[] strs=address.split(":");
		try
		{
			if(strs.length==2)
				removeListener(InetAddress.getByName(strs[0]),Integer.parseInt(strs[1]));
			else if(strs.length==1)
				removeListener(InetAddress.getByName("0.0.0.0"),Integer.parseInt(strs[0]));
			else
				return false;
			return true;
		}
		catch(Exception e)
		{
			return true;
		}
	}
	private void addListener(InetAddress inetAddress,int port,Container container) throws IOException
	{
		ServerSocket serverSocket=new ServerSocket(port,50,inetAddress);
		Listener listener=new Listener(serverSocket,container);
		map.put(serverSocket,listener);
		Shared.THREAD_POOL.execute(listener);
	}
	private void removeListener(InetAddress inetAddress,int port) throws Exception
	{
		for(ServerSocket serverSocket:map.keySet())
		{
			if(serverSocket.getInetAddress().equals(inetAddress)&&serverSocket.getLocalPort()==port)
			{
				map.get(serverSocket).stop();
				map.remove(serverSocket);
				return;
			}
		}
		throw new Exception();
	}
}
