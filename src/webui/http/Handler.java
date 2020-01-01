package webui.http;

import java.util.*;
public abstract class Handler
{
	private Map<String,Object> env;
	void env(Map<String,Object> env)
	{
		this.env=env;
	}
	protected Map<String,Object> env()
	{
		return env;
	}
	public void init() throws HTTPException
	{
	}
	public void doGet(Request request,Response response) throws HTTPException
	{
		doDefault(request,response);
	}
	public void doPost(Request request,Response response) throws HTTPException
	{
		doDefault(request,response);
	}
	void doDefault(Request request,Response response) throws HTTPException
	{
	}
}
