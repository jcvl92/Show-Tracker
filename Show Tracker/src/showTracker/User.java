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
		return showList.toArray(new ShowStatus[0]);
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getPassword()
	{
		return password;
	}
	

	public boolean isInList(String showID)
	{
		for(int i=0; i<showList.size(); ++i)
		{
			if(showList.get(i).showID.equals(showID))
				return true;
		}
		return false;
	}
}