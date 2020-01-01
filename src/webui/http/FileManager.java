package webui.http;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import common.*;

class FileManager extends Handler
{
	private Map<String,Object> env;
	@Override
	public void init()
	{
		env=env();
	}
	@Override
	public void doGet(Request request,Response response) throws HTTPException
	{
		String URI=request.getRequestURI();
		File file=new File(env.get("webroot")+URI);
		checkFile(file,"r");
		String extension="";
		int index=URI.lastIndexOf(".");
		if(index>=0)
			extension=URI.substring(index+1);
		String mime=MIME.get(extension);
		response.setContentType(mime!=null?mime:MIME.get(""));
		try
		{
			BufferedInputStream bis=new BufferedInputStream(new FileInputStream(file));
			response.setHeader("content-length",file.length()+"");
			if(!"HEAD".equals(request.getMethod()))
				Pipe.pipe(bis,response.getStream());
			bis.close();

		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			throw new HTTPException(500,ioe);
		}
	}
	@Override
	public void doPost(Request request,Response response) throws HTTPException
	{
		doGet(request,response);
	}
	private void checkFile(File file,String mode) throws HTTPException
	{
		// Check for file permission or not found error.
		if(file.isDirectory()||!Paths.get(file.getAbsolutePath()).normalize().startsWith(Paths.get(new File((String)(env.get("webroot"))).getAbsolutePath()).normalize()))
		{
			throw new HTTPException(403,"You have no permission to access "+file.getName()+" on this server");
		}
		if((mode.contains("e")||mode.contains("r")||mode.contains("w"))&&!file.exists())
		{
			throw new HTTPException(404,"Unable to find "+file.getName()+" on this server");
		}
		if(mode.contains("r")&&!file.canRead()||mode.contains("w")&&!file.canWrite())
		{
			throw new HTTPException(403,"You have no permission to access "+file.getName()+" on this server");
		}
	}
	static final Map<String,String> MIME=new HashMap<>()
	{
		{
			put("","application/octet-stream");
			put("txt","text/plain");
			put("html","text/html; charset=utf-8");
			put("css","text/css");
			put("js","application/x-javascript");
			put("json","application/json");
			put("jpg","image/jpeg");
			put("png","image/png");
			put("bmp","image/bmp");
			put("gif","image/gif");
			put("svg","image/svg+xml");
			put("mp3","audio/mpeg");
			put("mp4","video/mp4");
			put("zip","application/zip");
		}
	};
}
