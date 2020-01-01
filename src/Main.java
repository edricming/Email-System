import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

import common.*;
import webui.*;
import webui.http.*;

public class Main
{
    public static void main(String[] args) throws Exception
    {
	    Scanner sc=new Scanner(new BufferedInputStream(new FileInputStream(args[0])));
	    boolean autoOpen=false;
	    String username=null,password=null;
	    while(sc.hasNext())
	    {
	    	String line=sc.nextLine();
	    	if(line.isEmpty())
	    		continue;
	    	String[] strs=line.split("=");
	    	switch(strs[0])
		    {
			    case "WEBROOT":
			    	Shared.WEBROOT=strs[1];
			    	break;
			    case "WEBADDRESS":
				    Shared.WEBADDRESS=strs[1];
				    break;
			    case "WEBUSERNAME":
				    username=strs[1];
				    break;
			    case "WEBPASSWORD":
				    password=strs[1];
				    break;
			    case "MAILROOT":
				    Shared.MAILROOT=strs[1];
				    break;
			    case "DBDRIVER":
				    Shared.DBDRIVER=strs[1];
				    break;
			    case "DBURL":
				    Shared.DBURL=strs[1];
				    break;
			    case "DBUSERNAME":
			        Shared.DBUSERNAME=strs[1];
			        break;
			    case "DBPASSWORD":
				    Shared.DBPASSWORD=strs[1];
				    break;
			    case "AUTOOPENBROWSER":
			    	autoOpen=Boolean.parseBoolean(strs[1]);
		    }
	    }
	    if(username!=null&&password!=null)
	    	Shared.AUTH=new String(Base64.getEncoder().encode((username+":"+password).getBytes()),StandardCharsets.US_ASCII);
	    Server server=new Server();
	    HTTPContainer container=new HTTPContainer();
	    container.env(new HashMap<>(){
		    {
			    put("webroot",Shared.WEBROOT);
			    put("exe",new HashMap<String,String>(){
				    {
				    	put("/accountadmin","AccountAdmin");
				    	put("/mailadmin","MailAdmin");
				    	put("/download","Download");
				    	put("/upload","Upload");
				    }
			    });
		    }
	    });
        server.add(Shared.WEBADDRESS,container);
	    if(autoOpen&&Desktop.isDesktopSupported())
	    {
		    try
		    {
			    URI uri=URI.create("http://"+Shared.WEBADDRESS+"/index.html");
			    Desktop dp=Desktop.getDesktop();
			    if (dp.isSupported(Desktop.Action.BROWSE))
				    dp.browse(uri);
		    }catch(Exception e){e.printStackTrace();}
	    }
    }
}
