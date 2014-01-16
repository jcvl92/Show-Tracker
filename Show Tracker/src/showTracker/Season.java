package showTracker;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
class Season implements Serializable
{
	ArrayList<Episode> episodes;
	String seasonTag;
	
	Season(String tag, ArrayList<Episode> eps)
	{
		seasonTag = tag;
		episodes = eps;
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
			sb.append(episodes.get(i)+"\t- "+episodes.get(i).getTitle()+'\n');
		}
		return sb.toString();
	}
}