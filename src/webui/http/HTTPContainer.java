package webui.http;

import common.*;
import webui.*;

import java.net.*;
import java.util.*;

public class HTTPContainer extends Container
{
	private Request request;
	private Response response;
	@Override
	public void process(Socket socket) throws Exception
	{
		response=new Response(socket);
		try
		{
			request=new Request(socket);
			request.parse();
			if(Shared.AUTH!=null)
			{
				String authorization=request.getHeader("authorization");
				if(authorization==null||!authorization.substring(authorization.indexOf(' ')+1).equals(Shared.AUTH))
				{
					response.setHeader("www-authenticate","Basic realm=\"Connecting to Muzhik\"");
					throw new HTTPException(401);
				}
			}
			Class<? extends Handler> servletClass=FileManager.class;
			Map<String,String> exe=(Map<String, String>)env.get("exe");
			if(exe!=null)
			{
				String className=exe.get(request.getRequestURI());
				if(className!=null)
					servletClass=(Class<? extends Handler>)Class.forName(this.getClass().getPackageName()+".worker."+className);
			}
			Handler handler=servletClass.newInstance();
			handler.env(env);
			handler.init();
			switch(request.getMethod())
			{
				case "GET":
				case "HEAD":
					handler.doGet(request,response);
					break;
				case "POST":
					handler.doPost(request,response);
					break;
			}
			throw new HTTPException(200);
		}
		catch(HTTPException he)
		{
			response.commit(he);
		}
	}
}
