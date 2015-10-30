package showTracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Season implements Serializable
{
	public ArrayList<Episode> episodes;
	String seasonTag;

	Season(ArrayList<HashMap<String, String>> episodesData, Show show)
	{
		seasonTag = episodesData.get(0).get("seasonnumber");

		episodes = new ArrayList<Episode>();
		for(HashMap<String, String> episodeData : episodesData)
			this.episodes.add(new Episode(episodeData, show));
	}
	
	public String toString()
	{
		return seasonTag;
	}

	public String getText()
	{
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<episodes.size(); ++i)
		{
			sb.append(episodes.get(i)+"\n");
		}
		return sb.toString();
	}
}