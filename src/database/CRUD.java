package database;

import java.sql.*;
public class CRUD
{
	private static boolean initiated;
	private static CRUD crud=new CRUD();
	private CRUD()
	{
		if(!initiated)
			initiated=init();
	}
	private static boolean init()
	{
		DB.connect();
		PreparedStatement createTable;
		try
		{
			if(!DB.tableExists("user"))
			{
				createTable=DB.conn.prepareStatement("CREATE TABLE `user` ("+" `username` varchar(60) NOT NULL,"+" `password` varchar(60) NOT NULL,"+" `smtp` varchar(60) DEFAULT NULL,"+" `pop3` varchar(60) DEFAULT NULL,"+" PRIMARY KEY (`username`)"+") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
				createTable.executeUpdate();
			}
			if(!DB.tableExists("mail"))
			{
				createTable=DB.conn.prepareStatement("CREATE TABLE `mail` ("+" `id` int(11) NOT NULL AUTO_INCREMENT,"+" `username` varchar(60) DEFAULT NULL,"+" `status` enum('R','S','D','T') NOT NULL,"+" `mtime` datetime DEFAULT NULL,"+" `uid` varchar(60) DEFAULT NULL,"+" `sender` varchar(60) DEFAULT NULL,"+" `receivers` varchar(250) DEFAULT NULL,"+" `subject` varchar(250) DEFAULT NULL,"+" PRIMARY KEY (`id`),"+" KEY `username` (`username`),"+" CONSTRAINT `mail_ibfk_1` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE ON UPDATE CASCADE"+") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
				createTable.executeUpdate();
			}
			return true;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	public static boolean insertUser(String username,String password,String smtp,String pop3)
	{
		DB.connect();
		try
		{
			PreparedStatement ps=DB.conn.prepareStatement("INSERT INTO `user` VALUES(?,?,?,?);");
			ps.setString(1,username);
			if(password!=null)
				ps.setString(2,password);
			else
				ps.setNull(2,Types.VARCHAR);
			if(smtp!=null)
				ps.setString(3,smtp);
			else
				ps.setNull(3,Types.VARCHAR);
			if(pop3!=null)
				ps.setString(4,pop3);
			else
				ps.setNull(4,Types.VARCHAR);
			return ps.executeUpdate()>0;
		}
		catch(SQLException e)
		{
			return false;
		}
	}
	public static Result selectUser()
	{
		DB.connect();
		try
		{
			Result result=new Result();
			PreparedStatement ps=DB.conn.prepareStatement("SELECT * FROM `user` ORDER BY `username`;");
			result.set(ps.executeQuery());
			return result;
		}
		catch(SQLException e)
		{
			return null;
		}
	}
	public static Result selectUser(String username)
	{
		DB.connect();
		try
		{
			Result result=new Result();
			PreparedStatement ps=DB.conn.prepareStatement("SELECT * FROM `user` WHERE `username`=?;");
			ps.setString(1,username);
			result.set(ps.executeQuery());
			return result;
		}
		catch(SQLException e)
		{
			return null;
		}
	}
	public static boolean updateUser(String oldUsername,String username,String password,String smtp,String pop3)
	{
		DB.connect();
		try
		{
			PreparedStatement ps=DB.conn.prepareStatement("UPDATE `user` SET `username`=?,`password`=?,`smtp`=?,`pop3`=? WHERE `username`=?;");
			ps.setString(1,username);
			if(password!=null)
				ps.setString(2,password);
			else
				ps.setNull(2,Types.VARCHAR);
			if(smtp!=null)
				ps.setString(3,smtp);
			else
				ps.setNull(3,Types.VARCHAR);
			if(pop3!=null)
				ps.setString(4,pop3);
			else
				ps.setNull(4,Types.VARCHAR);
			ps.setString(5,oldUsername);
			return ps.executeUpdate()>0;
		}
		catch(SQLException e)
		{
			return false;
		}
	}
	public static boolean deleteUser(String username)
	{
		DB.connect();
		try
		{
			PreparedStatement ps=DB.conn.prepareStatement("DELETE FROM `user` WHERE `username`=?;");
			ps.setString(1,username);
			return ps.executeUpdate()>0;
		}
		catch(SQLException e)
		{
			return false;
		}
	}
	public static int insertMail(String username,String status,Timestamp mtime,String uid,String sender,String receivers,String subject)
	{
		DB.connect();
		try
		{
			PreparedStatement ps=DB.conn.prepareStatement("INSERT INTO `mail`(`username`,`status`,`mtime`,`uid`,`sender`,`receivers`,`subject`) VALUES(?,?,?,?,?,?,?);");
			if(username!=null)
				ps.setString(1,username);
			else
				ps.setNull(1,Types.VARCHAR);
			ps.setString(2,status);
			if(mtime!=null)
				ps.setTimestamp(3,mtime);
			else
				ps.setNull(3,Types.TIMESTAMP);
			if(uid!=null)
				ps.setString(4,uid);
			else
				ps.setNull(4,Types.TIMESTAMP);
			if(sender!=null)
				ps.setString(5,sender);
			else
				ps.setNull(5,Types.VARCHAR);
			if(receivers!=null)
				ps.setString(6,receivers);
			else
				ps.setNull(6,Types.VARCHAR);
			if(subject!=null)
				ps.setString(7,subject);
			else
				ps.setNull(7,Types.VARCHAR);
			if(ps.executeUpdate()>0)
			{
				Result result=selectMail(username,status,uid);
				return (int)result.values.get(0).get("id");
			}
			else
				return -1;
		}
		catch(SQLException e)
		{
			return -1;
		}
	}
	public static Result selectMail(int id)
	{
		DB.connect();
		try
		{
			Result result=new Result();
			PreparedStatement ps=DB.conn.prepareStatement("SELECT * FROM `mail` WHERE `id`=?;");
			ps.setInt(1,id);
			result.set(ps.executeQuery());
			return result;
		}
		catch(SQLException e)
		{
			return null;
		}
	}
	public static Result selectMail(String username,String status,String uid)
	{
		DB.connect();
		String c1="`username`",c2="`status`",c3="`uid`";
		try
		{
			Result result=new Result();
			if(username==null)
				c1=username="1";
			if(status==null)
				c2=status="1";
			if(uid==null)
				c3=uid="1";
			PreparedStatement ps=DB.conn.prepareStatement("SELECT * FROM `mail` WHERE "+c1+"=? AND "+c2+"=? AND "+c3+"=? ORDER BY `mtime` DESC;");
			ps.setString(1,username);
			ps.setString(2,status);
			ps.setString(3,uid);
			result.set(ps.executeQuery());
			return result;
		}
		catch(SQLException e)
		{
			return null;
		}
	}
	public static boolean updateMail(int id,String username,String status,Timestamp mtime,String uid,String sender,String receivers,String subject)
	{
		try
		{
			DB.connect();
			PreparedStatement ps=DB.conn.prepareStatement("UPDATE `mail` SET `username`=?,`status`=?,`mtime`=?,`uid`=?,`sender`=?,`receivers`=?,`subject`=? WHERE `id`=?;");
			if(username!=null)
				ps.setString(1,username);
			else
				ps.setNull(1,Types.VARCHAR);
			ps.setString(2,status);
			if(mtime!=null)
				ps.setTimestamp(3,mtime);
			else
				ps.setNull(3,Types.TIMESTAMP);
			if(uid!=null)
				ps.setString(4,uid);
			else
				ps.setNull(4,Types.TIMESTAMP);
			if(sender!=null)
				ps.setString(5,sender);
			else
				ps.setNull(5,Types.VARCHAR);
			if(receivers!=null)
				ps.setString(6,receivers);
			else
				ps.setNull(6,Types.VARCHAR);
			if(subject!=null)
				ps.setString(7,subject);
			else
				ps.setNull(7,Types.VARCHAR);
			ps.setInt(8,id);
			return ps.executeUpdate()>0;
		}
		catch(SQLException e)
		{
			return false;
		}
	}
	public static boolean deleteMail(int id)
	{
		DB.connect();
		try
		{
			PreparedStatement ps=DB.conn.prepareStatement("DELETE FROM `mail` WHERE `id`=?;");
			ps.setInt(1,id);
			return ps.executeUpdate()>0;
		}
		catch(SQLException e)
		{
			return false;
		}
	}
}
