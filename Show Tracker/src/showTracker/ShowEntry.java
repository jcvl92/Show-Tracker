package showTracker;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

@SuppressWarnings("serial")
public class ShowEntry implements Serializable
{
	String showName, showID, seasonCount, runTime, airTime, status, search;
	ArrayList<Season> seasons = new ArrayList<Season>();
	
	ShowEntry(String nameOfShow)
	{
		search = nameOfShow;
		try
		{
			getFromTVRage(nameOfShow);
		}
		catch (Exception e)
		{
			System.out.println("could not get show data.");
			e.printStackTrace();
		}
	}
	
	private void getFromTVRage(String nameOfShow) throws IOException, InterruptedException
	{
		//get the search xml document
		Document search = Jsoup.connect("http://services.tvrage.com/feeds/full_search.php?show="+nameOfShow).timeout(30*1000).get();
		
		//pick the first entry of the search, this is our show
		List<Element> showDescription = ((Element)search.childNode(1).childNode(1).childNode(0).childNode(1)).children();
		
		//search all fields for the values we want
		for(int i=0; i<showDescription.size(); ++i)
		{
			switch(showDescription.get(i).tagName())
			{
			case "showid":
				showID = showDescription.get(i).text();
				break;
			case "name":
				showName = showDescription.get(i).text();
				break;
			case "seasons":
				seasonCount = showDescription.get(i).text();
				break;
			case "runtime":
				runTime = showDescription.get(i).text();
				break;
			case "airtime":
				airTime = showDescription.get(i).text();
				break;
			case "airday":
				airTime = showDescription.get(i).text()+" at "+airTime;
				break;
			case "status":
				status = showDescription.get(i).text();
				break;
			}
		}
		
		//get the season details xml document
		Document list = Jsoup.connect("http://services.tvrage.com/feeds/episode_list.php?sid="+showID).timeout(30*1000).get();
		
		//pick the episode list
		Element episodeList = (Element) list.childNode(1).childNode(1).childNode(0).childNode(5);
		
		for(int i=0; i<episodeList.childNodeSize(); ++i)
		{
			if(episodeList.childNode(i).getClass() == Element.class)
			{
				ArrayList<Episode> episodes = new ArrayList<Episode>();
				Node aSeason = episodeList.childNode(i);
				for(int j=0; j<aSeason.childNodeSize(); ++j)
				{
					if(aSeason.childNode(j).getClass() == Element.class)
					{
						HashMap<String, String> ep = new HashMap<String, String>();
						Node episode = aSeason.childNode(j);
						
						for(int k=0; k<episode.childNodeSize(); ++k)
						{
							if(episode.childNode(k).getClass() != Element.class)
								ep.put("link", ((TextNode)episode.childNode(k)).text());
							else
								ep.put(((Element)episode.childNode(k)).tagName(),
										((Element)episode.childNode(k)).text());
						}
						ep.put("inseason", aSeason.attr("no"));
						
						episodes.add(new Episode(ep, showDescription.get(12).text()));
					}
				}
			
				seasons.add(new Season(
						(aSeason.attr("no")!="" ? "season "+aSeason.attr("no") : ((Element)aSeason).tagName())
						, episodes));
			}
		}
	}
	
	public String toString()
	{
		return showName;
	}
	
	public String getText()
	{
		return "Title: "+showName+"\nNumber of seasons: "+seasonCount+"\nRun time: "+runTime+"\nAir time: "+airTime;
	}
	
	public Episode getNextEpisode()
	{
		for(int i=0; i<seasons.size(); ++i)
		{
			ArrayList<Episode> episodes = seasons.get(i).episodes;
			for(int j=0; j<episodes.size(); ++j)
			{
				if(episodes.get(j).airDate != null)
					if(episodes.get(j).airDate.isAfterNow())
						return episodes.get(j);
			}
		}
		return null;
	}
	
	public Episode getLastEpisode()
	{
		for(int i=seasons.size()-1; i>=0; --i)
		{
			if(!seasons.get(i).seasonTag.contains("season"))
				continue;
			ArrayList<Episode> episodes = seasons.get(i).episodes;
			for(int j=episodes.size()-1; j>=0; --j)
			{
				if(episodes.get(j).airDate != null)
					if(episodes.get(j).airDate.isBeforeNow())
						return episodes.get(j);
			}
		}
		return null;
	}
}