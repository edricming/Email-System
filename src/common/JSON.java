package common;

import java.util.*;
import java.util.Date;
public class JSON
{
	private StringBuilder sb=new StringBuilder();
	public JSON(Object o)
	{
		toJSON(o);
	}
	private void toJSON(Object o)
	{

		if(o instanceof List)
		{
			sb.append('[');
			for(Object obj:(List)o)
			{
				toJSON(obj);
				sb.append(',');
			}
			sb.append(']');
		}
		else if(o instanceof Map)
		{
			sb.append('{');
			for(Object k:((Map)o).keySet())
			{
				toJSON(k);
				sb.append(':');
				toJSON(((Map)o).get(k));
				sb.append(',');
			}
			sb.append('}');
		}
		else if(o instanceof String)
			sb.append("\"").append(unescape((String)o)).append("\"");
		else if(o instanceof Date)
			sb.append("\"").append(o).append("\"");
		else
			sb.append(o);
	}
	private String unescape(String s)
	{
		char[] str=s.toCharArray();
		StringBuilder sb=new StringBuilder();
		for(char c:str)
		{
			switch(c)
			{
				case '\r':
					sb.append("\\r");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '"':
					sb.append("\\\"");
					break;
				case '\'':
					sb.append("\\'");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	@Override
	public String toString()
	{
		return sb.toString();
	}
}
