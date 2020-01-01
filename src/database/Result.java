package database;

import java.sql.*;
import java.util.*;

public class Result
{
	public int rows;
	public final List<String> columns=new ArrayList<>();
	public final List<Map<String,Object>> values=new ArrayList<>();
	void set(ResultSet rs)
	{
		try
		{
			ResultSetMetaData rsmd;
			rsmd=rs.getMetaData();
			rs.last();
			rows=rs.getRow();
			rs.first();
			int col=rsmd.getColumnCount();
			//column names
			for(int j=1;j<=col;j++)
				columns.add(rsmd.getColumnName(j));
			//values
			for(int i=1;i<=rows;i++)
			{
				Map<String,Object> map=new HashMap<>();
				for(int j=1;j<=col;j++)
				{
					String key=rsmd.getColumnName(j);
					Object value;
					switch(rsmd.getColumnType(j))
					{
						case Types.TIMESTAMP:
							value=rs.getTimestamp(j);
							break;
						case Types.INTEGER:
						case Types.BIGINT:
							value=rs.getInt(j);
							break;
						default:
						case Types.VARCHAR:
						case Types.CHAR:
							value=rs.getString(j);
							break;
					}
					map.put(key,value);
				}
				values.add(map);
				rs.next();
			}
			rs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
