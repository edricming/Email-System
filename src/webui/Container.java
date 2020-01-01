package webui;

import java.net.*;
import java.util.*;
public abstract class Container
{
	protected Map<String,Object> env=new HashMap<>();
	static Container copy(Container container)
	{
		try
		{
			Class<? extends Container> containerClass=container.getClass();
			Container newContainer=containerClass.newInstance();
			newContainer.env.putAll(container.env);
			return newContainer;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public void env(Map<String,Object> env)
	{
		this.env.clear();
		this.env.putAll(env);
	}
	public abstract void process(Socket socket) throws Exception;
}
