package mail;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
public class Mail
{
	public static final Pattern pattern=Pattern.compile("([.a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+)");
	private static final SimpleDateFormat sdf=new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z",Locale.US);
	public String sender,subject,content;
	public List<String> receivers;
	public List<Mail> attachments;
	public Date date;
	public byte[] contentb;
	private final List<Pair<String,List<String>>> headers=new ArrayList<>();
	private final List<String> body=new ArrayList<>();
	private String raw;
	public Mail(){}
	public Mail(String mail)
	{
		this();
		raw=mail;
		parse();
	}
	public Mail(File file)
	{
		this();
		try
		{
			raw=Files.readString(file.toPath(),StandardCharsets.US_ASCII);
			parse();
		}
		catch(IOException fe)
		{}
	}
	private String getHeader(String key)
	{
		String value=null;
		for(int i=headers.size()-1;i>=0;i--)
		{
			Pair<String,List<String>> p=headers.get(i);
			if(key.equalsIgnoreCase(p.key))
			{
				value=p.value.get(0);
				for(int j=1;j<p.value.size();j++)
					value+=" "+p.value.get(j);
				break;
			}
		}
		return value;
	}
	private void parse()
	{
		Scanner sc=new Scanner(raw);
		Pair<String,List<String>> pair=null;
		while(true)
		{
			String line=sc.nextLine();
			if(line.isBlank())
			{
				if(pair==null)
					continue;
				else
					break;
			}
			else if(line.substring(0,1).isBlank())
			{
				pair.value.add(line.stripLeading());
			}
			else
			{
				int idx=line.indexOf(":");
				List<String> list=new ArrayList<>();
				list.add(line.substring(idx+1).stripLeading());
				pair=new Pair<>(line.substring(0,idx),list);
				headers.add(pair);
			}
		}
		while(sc.hasNext())
			body.add(sc.nextLine());
		Pattern pattern;
		Matcher matcher;
		sender=getHeader("From");
		if(sender!=null)
		{
			matcher=Mail.pattern.matcher(getHeader("From"));
			if(matcher.find())
				sender=matcher.group(0);
			else
				sender=null;
		}
		subject=getHeader("Subject");
		String rs=getHeader("To");
		if(rs!=null)
		{
			receivers=new ArrayList<>();
			matcher=Mail.pattern.matcher(rs);
			while(matcher.find())
				receivers.add(matcher.group(0));
			if(receivers.isEmpty())
				receivers=null;
		}
		else
			receivers=null;
		try
		{
			date=sdf.parse(getHeader("Date"));
		}
		catch(Exception e)
		{
			date=null;
		}
		String contentType=getHeader("Content-Type"),encoding=getHeader("Content-Transfer-Encoding");
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<body.size();i++)
		{
			sb.append(body.get(i));
			if(i!=body.size()-1)
				sb.append("\r\n");
		}
		content=sb.toString();
		if("base64".equalsIgnoreCase(encoding))
		{
			Base64.Decoder decoder=Base64.getMimeDecoder();
			contentb=decoder.decode(content);
			content=new String(contentb);
		}
		else
			contentb=content.getBytes();
		attachments=null;
		if(contentType!=null)
		{
			pattern=Pattern.compile("boundary=\"(.*)\"");
			matcher=pattern.matcher(contentType);
			if(matcher.find())
			{
				attachments=new ArrayList<>();
				String boundary="--"+matcher.group(1);
				String strs[]=content.split(boundary);
				content="";
				for(int i=1;i<strs.length-1;i++)
				{
					Mail m=new Mail(strs[i]);
					String disposition=m.getHeader("Content-Disposition");
					if(disposition!=null&&disposition.contains("attachment"))
					{
						pattern=Pattern.compile("filename=\"(.*)\"");
						matcher=pattern.matcher(disposition);
						if(matcher.find())
							m.subject=matcher.group(1);
						attachments.add(m);
					}
					else
						content+="\r\n"+m.content;
				}
			}
		}
	}
	public void compose()
	{
		headers.clear();
		body.clear();
		raw="";
		if(sender==null||receivers==null||receivers.isEmpty())
			return;
		List<String> values;
		values=new ArrayList<>();
		values.add("\""+sender.split("@")[0]+"\" <"+sender+">");
		headers.add(new Pair<>("From",values));
		values=new ArrayList<>();
		for(String receiver:receivers)
			values.add("\""+receiver.split("@")[0]+"\" <"+receiver+">");
		headers.add(new Pair<>("To",values));
		if(subject!=null)
		{
			values=new ArrayList<>();
			values.add(subject);
			headers.add(new Pair<>("Subject",values));
		}
		if(date!=null)
		{
			values=new ArrayList<>();
			values.add(sdf.format(date));
			headers.add(new Pair<>("Date",values));
		}
		if(content==null)
			content="";
		Collections.addAll(body,content.split("\r?\n"));
		StringBuilder sb=new StringBuilder();
		for(Pair<String,List<String>> pair:headers)
		{
			sb.append(pair.key).append(": ").append(pair.value.get(0)).append("\r\n");
			for(int i=1;i<pair.value.size();i++)
				sb.append("\t").append(pair.value.get(i)).append("\r\n");
		}
		sb.append("\r\n");
		for(String line:body)
			sb.append(line).append("\r\n");
		raw=sb.substring(0,sb.length()-2);
	}
	@Override
	public String toString()
	{
		return raw;
	}
	public boolean store(File file)
	{
		try
		{
			PrintWriter pw=new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)));
			pw.print(raw);
			pw.flush();
			pw.close();
			return true;
		}
		catch(FileNotFoundException fe)
		{
			return false;
		}
	}
	public static class Pair<K,V>
	{
		final K key;
		final V value;
		Pair(K key,V value)
		{
			this.key=key;
			this.value=value;
		}
		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof Pair))
				   return false;
			Pair pair=(Pair)obj;
			return key.equals(pair.key)&&value.equals(pair.value);
		}
		@Override
		public int hashCode()
		{
			return key.hashCode()|value.hashCode();
		}
	}
}
