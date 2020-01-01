package common;

import java.security.*;
public class MD5
{
	private static final String[] hexDigIts={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
	public static String MD5Encode(String origin,String charsetname)
	{
		String resultString=null;
		try
		{
			resultString=origin;
			MessageDigest md=MessageDigest.getInstance("MD5");
			if(null==charsetname||"".equals(charsetname))
				resultString=byteArrayToHexString(md.digest(resultString.getBytes()));
			else
				resultString=byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
		}
		catch(Exception e){}
		return resultString;
	}

	private static String byteArrayToHexString(byte[] b)
	{
		StringBuilder resultSb=new StringBuilder();
		for(byte i:b)
			resultSb.append(byteToHexString(i));
		return resultSb.toString();
	}

	private static String byteToHexString(byte b)
	{
		int n=b;
		if(n<0)
			n+=256;
		int d1=n/16;
		int d2=n%16;
		return hexDigIts[d1]+hexDigIts[d2];
	}
}
