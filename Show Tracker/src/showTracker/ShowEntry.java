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
	String showName, seasonCount, runTime, airTime, status, search;
	int showID;
	public int seasonPos;
	public int episodePos;
	public ArrayList<Season> seasons = new ArrayList<Season>();

	public ShowEntry(String nameOfShow) throws IOException, InterruptedException
	{
		//TODO: the workaround of not picking a last watched results in the first episode being last watched instead of some null position or something, fix it
		//save the search string for TPB magnet link searches
		search = nameOfShow;

		//get the data
		getFromTVRage(nameOfShow);

		//set the last watched episode position
		//manageWatchPosition();
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

	public Episode getNextEpisodeToWatch()
	{
		if(seasonPos == -1 && episodePos == -1)
			return seasons.get(0).episodes.get(0);

		Season s = seasons.get(seasonPos);
		//if this is the last episode in the season
		if(episodePos == s.episodes.size()-1)
		{
			//if this is the last season
			if(seasonPos == seasons.size()-1)
				return null;

			//get the first episode of the next season
			return seasons.get(seasonPos+1).episodes.get(0);
		}
		return s.episodes.get(episodePos+1);
	}

	public Episode getLastEpisodeWatched()
	{
		if(seasonPos == -1 && episodePos == -1)
			return null;
		return seasons.get(seasonPos).episodes.get(episodePos);
	}

	public void manageWatchPosition()
	{/*
		System.out.println("Have you seen any episodes of "+showName+"? (y/n)");
		if(ShowTracker.scanner.nextLine().equals("y"))
		{
			//print out the season list
			System.out.println();
			for(int i=0;i<seasons.size();++i)
				System.out.println((i+1)+". "+seasons.get(i));
			System.out.println("\nPlease choose the season of the last episode you have seen. (1-"+seasons.size()+')');

			//get the season position
			seasonPos = Integer.parseInt(ShowTracker.scanner.nextLine())-1;

			//print out the episode list
			System.out.println();
			for(int i=0;i<seasons.get(seasonPos).episodes.size();++i)
				System.out.println((i+1)+". "+seasons.get(seasonPos).episodes.get(i)+" ("+seasons.get(seasonPos).episodes.get(i).getDate()+')');
			System.out.println("\nPlease choose the last episode you have seen. (1-"+seasons.get(seasonPos).episodes.size()+')');

			//get the episode position
			episodePos = Integer.parseInt(ShowTracker.scanner.nextLine())-1;
		}
		else
		{
			seasonPos = -1;
			episodePos = -1;
		}*/
	}

	/**
	 * @param episode	An episode to check.
	 * @return			true if the air date of the episode is on or before the air date of the watch position episode, false otherwise
	 */
	public boolean isSeen(Episode episode)
	{
		if(episode==seasons.get(seasonPos).episodes.get(episodePos) ||
				episode.airDate.isBefore(seasons.get(seasonPos).episodes.get(episodePos).airDate))
			return true;
		return false;
	}

	public void update() throws IOException, InterruptedException
	{
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
	}

}