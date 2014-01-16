package showTracker;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class User implements Serializable
{
	private String username, password;
	private ArrayList<ShowStatus> showList = new ArrayList<ShowStatus>();
	
	User(String u, String p)
	{
		username = u;
		password = p;
	}
	
	public void addShow(ShowStatus show)
	{
		showList.add(show);
	}
	
	public ShowStatus[] getShowList()
	{
		return (ShowStatus[]) showList.toArray();
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getPassword()
	{
		return password;
	}
}