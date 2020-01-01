package webui;

import java.io.*;
import java.net.*;

import common.*;

class Listener implements Runnable
{
	private final ServerSocket serverSocket;
	private final Container container;
	private boolean running;
	private Thread thread;
	Listener(ServerSocket serverSocket,Container container)
	{
		this.serverSocket=serverSocket;
		this.container=container;
		running=true;
	}
	void stop()
	{
		running=false;
		try
		{
			new Socket(serverSocket.getInetAddress(),serverSocket.getLocalPort());
			thread.join();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
	}
	public void run()
	{
		Socket socket;
		thread=Thread.currentThread();
		while(running)
		{
			try
			{
				socket=serverSocket.accept();
				socket.setKeepAlive(true);
				Shared.THREAD_POOL.execute(new Connector(socket,Container.copy(container)));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		//executor.shutdownNow();
		try
		{
			serverSocket.close();
		}
		catch(IOException ioe)
		{
			//ioe.printStackTrace();
		}
	}
}
