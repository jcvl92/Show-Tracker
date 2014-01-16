package showTracker;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ShowStatus implements Serializable
{
	public String showID, datePosition;
	
	ShowStatus(String sid, String dp)
	{
		showID = sid;
		datePosition = dp;
	}
}