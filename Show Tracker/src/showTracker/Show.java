package showTracker;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

@SuppressWarnings("serial")
public class Show implements Serializable
{
	String showName, seasonCount, runTime, airTime, status, search, link;
	ImageIcon image;
	int showID;
	public ArrayList<Season> seasons = new ArrayList<Season>();

	public static ArrayList<HashMap<String, String>> search(String searchText) throws IOException
	{
		//get the search xml document
		Document search = Jsoup.connect("http://services.tvrage.com/feeds/full_search.php?show="+searchText).timeout(30*1000).get();

		//get the possible entries
		Node nodes = search.childNode(1).childNode(1).childNode(0);
		ArrayList<HashMap<String, String>> entries = new ArrayList<HashMap<String, String>>();
		for(int i=0; i<nodes.childNodeSize(); ++i)
			if(nodes.childNode(i).getClass().equals(Element.class))
			{
				List<Node> showDescription = nodes.childNode(i).childNodes();
				HashMap<String, String> showInfo = new HashMap<String, String>();

				//get the fields
				for(int j=0; j<showDescription.size(); ++j)
				{
					if(showDescription.get(j).getClass() == Element.class)
						showInfo.put(((Element)showDescription.get(j)).tagName(),
								((Element)showDescription.get(j)).text());
					else if(((TextNode)showDescription.get(j)).text().contains("http"))
						showInfo.put("link", ((TextNode)showDescription.get(j)).text());
				}
				
				showInfo.put("search", searchText);

				entries.add(showInfo);
			}

		//return the entries
		return entries;
	}

	public static Show getShow(HashMap<String, String> showEntry) throws InterruptedException, IOException
	{
		Show show = new Show();

		show.showID = Integer.parseInt(showEntry.get("showid"));
		show.showName = showEntry.get("name");
		show.seasonCount = showEntry.get("seasons");
		show.runTime = showEntry.get("runtime");
		show.airTime = showEntry.get("airtime");
		if(showEntry.containsKey("airday"))
			show.airTime = showEntry.get("airday")+" at "+show.airTime;
		show.status = showEntry.get("status");
		show.link = showEntry.get("link");
		show.search = showEntry.get("search");

		//get the season details xml document
		Document list = Jsoup.connect("http://services.tvrage.com/feeds/episode_list.php?sid="+show.showID).timeout(30*1000).get();

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

						episodes.add(new Episode(ep, show.airTime, show));
					}
				}

				show.seasons.add(new Season(
						(aSeason.attr("no")!="" ? "season "+aSeason.attr("no") : ((Element)aSeason).tagName())
						, episodes));
			}
		}

		//get the image for the show
		try
		{
			Document page = Jsoup.connect(show.link).timeout(30*1000).get();
			show.image = new ImageIcon(new URL(page.getElementsByClass("padding_bottom_10").get(0).child(0).attr("src")));
		}
		catch(Exception e)
		{
			show.image = null;
		}

		return show;
	}

	public ImageIcon getImage()
	{
		return image;
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

	public ArrayList<Episode> getAiredEpisodes()
	{
		ArrayList<Episode> airedEpisodes = new ArrayList<Episode>();
		//iterate through the seasons and episodes and add each episode if it has aired
		for(int i=0; i<seasons.size(); ++i)
			for(int j=0; j<seasons.get(i).episodes.size(); ++j)
				if(seasons.get(i).episodes.get(j).getAirDate() != null && seasons.get(i).episodes.get(j).getAirDate().isBeforeNow())
					airedEpisodes.add(seasons.get(i).episodes.get(j));

		Collections.sort(airedEpisodes, new Comparator<Episode>()
				{
			public int compare(Episode arg0, Episode arg1)
			{
				if(arg0.getAirDate() == null && arg1.getAirDate() == null) return 0;
				if(arg1.getAirDate() == null) return -1;
				if(arg0.getAirDate() == null) return 1;
				return arg1.getAirDate().compareTo(arg0.getAirDate());
			}
				});

		return airedEpisodes;
	}
	
	public String showName()
	{
		return showName;
	}
	
	public String getSearchText()
	{
		return search;
	}
	
	public void setSearchText(String str)
	{
		search = str;
	}
}