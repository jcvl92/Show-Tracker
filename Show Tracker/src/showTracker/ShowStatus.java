package showTracker;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ShowStatus implements Serializable
{
	public String showID, showName, epPosition;
	
	ShowStatus(String sid, String sn, String dp)
	{
		showID = sid;
		showName = sn;
		epPosition = dp;
	}
	
	ShowStatus(ShowEntry show)
	{
		showID = show.showID;
		showName = show.showName;
		epPosition = show.seasons.get(0).episodes.get(0).information.get("epnum");
	}
}