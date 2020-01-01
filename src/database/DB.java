package database;

import java.sql.*;
import java.util.*;

import common.*;

class DB
{
	static Connection conn;
	static boolean connect()
	{
		try
		{
			if(conn==null||conn.isClosed())
			{
				Class.forName(Shared.DBDRIVER);
				conn=DriverManager.getConnection(Shared.DBURL,Shared.DBUSERNAME,Shared.DBPASSWORD);
			}
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			disconnect();
			return false;
		}
	}
	static void disconnect()
	{
		try
		{
			conn.close();
		}
		catch(Exception e)
		{}
		conn=null;
	}
	static boolean tableExists(String table)
	{
		connect();
		boolean flag=false;
		try
		{
			ResultSet rs=conn.getMetaData().getTables(null,null,"%",null);
			while(rs.next())
			{
				if(table.equalsIgnoreCase(rs.getString("TABLE_NAME")))
				{
					flag=true;
					break;
				}
			}
		}
		catch(Exception e){}
		return flag;
	}
	static List<String> getPrimaryKey(String table)
	{
		connect();
		List<String> pks=new LinkedList<>();
		try
		{
			ResultSet rs=conn.getMetaData().getPrimaryKeys(conn.getCatalog(),conn.getMetaData().getUserName(),table);
			while(rs.next())
				pks.add(rs.getString("COLUMN_NAME"));
		}
		catch(Exception e){}
		return pks;
	}
}
