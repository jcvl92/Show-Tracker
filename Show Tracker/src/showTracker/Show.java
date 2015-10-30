package showTracker;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.ImageIcon;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

@SuppressWarnings("serial")
public class Show implements Serializable
{
	ImageIcon image;
	HashMap<String, String> showData;
	public LinkedHashMap<String, Season> seasons = new LinkedHashMap<String, Season>();

	Show(HashMap<String, String> showDetails, LinkedHashMap<String, ArrayList<HashMap<String, String>>> episodes) throws IOException
	{
		showData = showDetails;

		//set the seasons
		for(Entry<String, ArrayList<HashMap<String, String>>> season : episodes.entrySet())
			seasons.put(season.getKey(), new Season(season.getValue(), this));

		//fetch the image
		URL url = new URL("http://thetvdb.com/banners/"+showData.get("poster"));
		url.getContent();
		image = new ImageIcon(url);
	}
	
	public static ArrayList<HashMap<String, String>> search(String searchText) throws IOException
	{
		//get the search xml document
		Document search = Jsoup.connect("http://thetvdb.com/api/GetSeries.php?seriesname="+searchText).timeout(60*1000).get();

		//get the possible entries
		Elements nodes = search.getElementsByTag("series");
		ArrayList<HashMap<String, String>> entries = new ArrayList<HashMap<String, String>>();

		for(int i=0; i<nodes.size(); ++i)
		{
			Node series = nodes.get(i);

			HashMap<String, String> showInfo = new HashMap<String, String>();
			for(Node field : series.childNodes())
				if(field.getClass().equals(Element.class))
					showInfo.put(((Element)field).tag().getName(), ((TextNode)field.childNode(0)).text());
			showInfo.put("search", searchText);

			entries.add(showInfo);
		}

		return entries;
	}

	public static Show getShow(String showID) throws InterruptedException, IOException
	{
		//TODO: fix search text not being included
		//get the series details xml document
		Document list = Jsoup.connect("http://thetvdb.com/api/"+ShowTracker.apiKey+"/series/"+showID+"/all").timeout(60 * 1000).get();
		Node series = list.getElementsByTag("series").get(0);

		//get the show details
		HashMap<String, String> showDetails = new HashMap<String, String>();
		for(Node field : series.childNodes())
			if(field.getClass().equals(Element.class))
				showDetails.put(((Element) field).tag().getName(), ((Element) field).text());

		//get the episodes
		LinkedHashMap<String, ArrayList<HashMap<String, String>>> episodes = new LinkedHashMap<String, ArrayList<HashMap<String, String>>>();
		for(Element episodeElement : list.getElementsByTag("episode"))
		{
			//get the episode details
			HashMap<String, String> episode = new HashMap<String, String>();
			for(Node field : episodeElement.childNodes())
				if(field.getClass().equals(Element.class))
					episode.put(((Element)field).tag().getName(), ((Element)field).text());

			//put the episode into the corresponding season
			ArrayList<HashMap<String, String>> season = episodes.get(episode.get("seasonid"));
			if(season == null)
			{
				season = new ArrayList<HashMap<String, String>>();
				episodes.put(episode.get("seasonid"), season);
			}
			season.add(episode);
		}
		
		return new Show(showDetails, episodes);
	}

	public ImageIcon getImage()
	{
		return image;
	}

	public String toString()
	{
		return showName();
	}

	public void update() throws IOException, InterruptedException
	{
		//store the current episode contents
		@SuppressWarnings("unchecked")
		ArrayList<Season> oldSeasons = (ArrayList<Season>)seasons.clone();
		//refresh the show contents
		Show newShow = getShow(showData.get("id"));
		seasons = newShow.seasons;
		showData = newShow.showData;
		image = newShow.image;

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
		
		ShowTracker.ShowArrayList.setDirty(true);
	}

	public ArrayList<Episode> getAiredEpisodes()
	{
		ArrayList<Episode> airedEpisodes = new ArrayList<Episode>();
		//iterate through the seasons and episodes and add each episode if it has aired
		for(Season season : seasons.values())
			for(Episode episode : season.episodes)
				if(episode.getAirDate() != null && episode.getAirDate().isBeforeNow())
					airedEpisodes.add(episode);
		

		Collections.sort(airedEpisodes, ShowTracker.episodeComparator);
		Collections.reverse(airedEpisodes);

		return airedEpisodes;
	}

	public String showName()
	{
		return showData.get("seriesname");
	}

	public String getSearchText()
	{
		return showData.get("search");
	}

	public void setSearchText(String str)
	{
		showData.put("search", str);
	}
}