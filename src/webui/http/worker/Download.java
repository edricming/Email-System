package webui.http.worker;

import java.io.*;

import mail.*;
import webui.http.*;
public class Download extends Handler
{
	@Override
	public void doGet(Request request,Response response) throws HTTPException
	{
		OutputStream os=response.getStream();
		response.setContentType("application/octet-stream");
		Mail mail=ToolBox.take(Integer.parseInt(request.getParameter("id")));
		String aid=request.getParameter("aid");
		try
		{
			if(aid!=null&&!aid.isBlank())
			{
				mail=mail.attachments.get(Integer.parseInt(aid));
				response.setHeader("content-disposition","attachment;filename="+mail.subject);
				os.write(mail.contentb);
			}
			else
			{
				response.setHeader("content-disposition","attachment;filename="+mail.subject+".eml");
				os.write(mail.toString().getBytes());
			}
			os.flush();
			os.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}
