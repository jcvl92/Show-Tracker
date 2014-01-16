package showTracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ShowDatabase
{
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private final String DB_URL;
	private final String USER;
	private final String PASS;
	private Connection CONN;
	
	/**
	 * Creates a new mySQL connection
	 * @param db_url
	 * @param user
	 * @param password
	 * @throws Exception
	 */
	public ShowDatabase(String db_url, String user, String password) throws Exception
	{
		DB_URL = "jdbc:mysql://"+db_url;
		USER = user;
		PASS = password;
		
		//Register JDBC driver
		Class.forName(JDBC_DRIVER);
		
		//Open a connection
		CONN = DriverManager.getConnection(DB_URL,USER,PASS);
	}
	
	/**
	 * Closes the connection
	 */
	public void close() throws SQLException
	{
		if(CONN != null)
		{
			CONN.close();
			CONN = null;
		}
	}
	
	/**
	 * Inserts or updates a user entry into the database
	 * @param user 
	 */
	public void updateUser(User user)
	{
		
	}
	
	/**
	 * Inserts or updates a show entry into the database
	 */
	public void updateShow()
	{
		
	}
	
	/**
	 * Gets a user object from the database
	 * @param password 
	 * @param username 
	 */
	public User getUser(String username, String password)
	{
		return null;
	}
	
	/**
	 * Gets a show entry object from the database
	 * @param showID 
	 */
	public ShowEntry getShow(String showID)
	{
		return null;
	}
}