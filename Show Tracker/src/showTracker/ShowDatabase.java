package showTracker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
	 * @param user	the user object to insert into the database
	 * @return		true if the update/insert was successful, false otherwise
	 * @throws SQLException	if there was an error processing the request
	 */
	public boolean updateUser(User user) throws SQLException
	{
		//try to update
		//build the statement
		PreparedStatement update = CONN.prepareStatement("UPDATE USERS SET object = ? WHERE username = ? AND password = ?");
		update.setObject(1, user);
		update.setString(2, user.getUsername());
		update.setString(3, user.getPassword());
		
		//execute the statement
		if(update.execute())
		{
			//free the resources of the prepared statement
			update.close();
			
			//return sucessful
			return true;
		}
		//if that fails, try to insert
		else
		{
			//build the statement
			PreparedStatement insert = CONN.prepareStatement("INSERT INTO USERS (object, username, password) VALUES (?, ?, ?)");
			insert.setObject(1, user);
			insert.setString(2, user.getUsername());
			insert.setString(3, user.getPassword());
			
			//execute the statement
			boolean res = insert.execute();
			
			//free the resources of the prepared statement
			insert.close();
			
			//return the result
			return res;
		}
	}
	
	/**
	 * Inserts or updates a show entry into the database
	 * @param show	the show entry object to insert into the database
	 * @return		true if the update/insert was successful, false otherwise
	 * @throws SQLException	if there was an error processing the request
	 */
	public boolean updateShow(ShowEntry show) throws SQLException
	{
		//try to update
		//build the statement
		PreparedStatement update = CONN.prepareStatement("UPDATE SHOWS SET object = ? WHERE showid = ?");
		update.setObject(1, show);
		update.setString(2, show.showID);
		
		//execute the statement
		if(update.execute())
		{
			//free the resources of the prepared statement
			update.close();
			
			//return sucessful
			return true;
		}
		//if that fails, try to insert
		else
		{
			//build the statement
			PreparedStatement insert = CONN.prepareStatement("INSERT INTO SHOWS (object, showid) VALUES (?, ?)");
			insert.setObject(1, show);
			insert.setString(2, show.showID);
			
			//execute the statement
			boolean res = insert.execute();
			
			//free the resources of the prepared statement
			insert.close();
			
			//return the result
			return res;
		}
	}
	
	/**
	 * Gets a user object from the database
	 * @param username  the username of the requested user
	 * @param password	the password of the requested user
	 * @return			the requested User object or null if not found
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public User getUser(String username, String password) throws SQLException, IOException, ClassNotFoundException
	{
		//prepare the statement
		PreparedStatement pstmt = CONN.prepareStatement("SELECT object FROM USERS WHERE username = ? AND password = ?");
	    pstmt.setString(1, username);
	    pstmt.setString(2, password);
	    
	    //execute the query
	    ResultSet rs = pstmt.executeQuery();
	    
	    //close the prepared statement
	    pstmt.close();
	    
	    //convert the blob contents to a user object and return that
	    if(rs.first())
	    {
	    	//get the result object
	    	User result = (User)new ObjectInputStream(new ByteArrayInputStream(rs.getBytes(1))).readObject();
	    	
	    	//close the result set
	    	rs.close();
	    	
	    	//return the result
	    	return result;
	    }
	    //or return null if the user entry was not found
	    else
	    {
	    	//close the result set
	    	rs.close();
	    	
	    	return null;
	    }
	}
	
	/**
	 * Gets a show entry object from the database
	 * @param showID	the id of the requested show
	 * @return			the requested ShowEntry object or null if not found
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public ShowEntry getShow(String showID) throws SQLException, IOException, ClassNotFoundException
	{
		//prepare the statement
		PreparedStatement pstmt = CONN.prepareStatement("SELECT object FROM SHOWS WHERE showid = ?");
	    pstmt.setString(1, showID);
	    
	    //execute the query
	    ResultSet rs = pstmt.executeQuery();
	    
	    //close the prepared statement
	    pstmt.close();
	    
	    //convert the blob contents to a show entry object and return that
	    if(rs.first())
	    {
	    	//get the result object
	    	ShowEntry result = (ShowEntry)new ObjectInputStream(new ByteArrayInputStream(rs.getBytes(1))).readObject();
	    	
	    	//close the result set
	    	rs.close();
	    	
	    	//return the result
	    	return result;
	    }
	    //or return null if the user entry was not found
	    else
	    {
	    	//close the result set
	    	rs.close();
	    	
	    	return null;
	    }
	}
}