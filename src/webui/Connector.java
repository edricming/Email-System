package webui;

import java.net.*;
class Connector implements Runnable
{
	private final Socket socket;
	private final Container container;
	Connector(Socket socket,Container container)
	{
		this.socket=socket;
		this.container=container;
	}
	@Override
	public void run()
	{
		try
		{
			container.process(socket);
			if(!socket.isClosed())
				socket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
