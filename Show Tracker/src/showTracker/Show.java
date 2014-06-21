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
public class Show implements Serializable
{
	String showName, seasonCount, runTime, airTime, status, search;
	int showID;
	public ArrayList<Season> seasons = new ArrayList<Season>();

	public Show(String nameOfShow) throws IOException, InterruptedException
	{
		//save the search string for TPB magnet link searches
		search = nameOfShow;

		//get the data
		getFromTVRage(nameOfShow);
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
				showID = Integer.parseInt(showDescription.get(i).text());
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

						episodes.add(new Episode(ep, airTime, this));
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

	public void update() throws IOException, InterruptedException
	{
		//TODO: this is not robust, it relies on the fact that the new episodes will have nothing removed. find a better way to update while keeping the seen value
		//TODO: maybe we could use the TVRage update api call?
		//store the current episode contents
		@SuppressWarnings("unchecked")
		ArrayList<Season> oldSeasons = (ArrayList<Season>)seasons.clone();
		//clear the current episode contents
		seasons = new ArrayList<Season>();

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

						episodes.add(new Episode(ep, airTime, this));
					}
				}

				seasons.add(new Season(
						(aSeason.attr("no")!="" ? "season "+aSeason.attr("no") : ((Element)aSeason).tagName())
						, episodes));
			}
		}
		
		//iterate through the episodes and set the correct seen values
		for(int i=0; i<oldSeasons.size(); ++i)
		{
			Season oldSeason = oldSeasons.get(i);
			Season newSeason = seasons.get(i);
			for(int j=0; j<oldSeason.episodes.size(); ++j)
			{
				newSeason.episodes.get(j).setWatched(oldSeason.episodes.get(j).isWatched());
			}
		}
	}
}