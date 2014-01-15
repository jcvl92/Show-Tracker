package showTracker;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

@SuppressWarnings("serial")
public class ShowEntry implements Serializable
{
	String showName, showID, seasonCount, runTime, airTime, status;
	ArrayList<Season> seasons = new ArrayList<Season>();
	
	ShowEntry(String nameOfShow)
	{
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
	
	class Episode implements Serializable
	{
		HashMap<String, String> information;
		String airTime, description=null;
		DateTime airDate;
		transient DateTimeFormatter parseFormatter;
		transient DateTimeFormatter writeFormatter;
		
		Episode(HashMap<String, String> info, String time) throws InterruptedException
		{
			setFormatters();
			information = info;
			airTime = time;
			try
			{
				airDate = DateTime.parse(info.get("airdate")+'-'+airTime, parseFormatter);
			}
			catch(Exception e)
			{
				airDate = null;
			}
		}
		
		private void setFormatters()
		{
			parseFormatter = new DateTimeFormatterBuilder()
			.appendYear(4, 4)
			.appendLiteral('-')
			.appendMonthOfYear(2)
			.appendLiteral('-')
			.appendDayOfMonth(2)
			.appendLiteral('-')
			.appendHourOfDay(2)
			.appendLiteral(':')
			.appendMinuteOfHour(2)
			.toFormatter();
			
			writeFormatter = new DateTimeFormatterBuilder()
			.appendMonthOfYearShortText()
			.appendLiteral(", ")
			.appendDayOfMonth(1)
			.appendLiteral(' ')
			.appendYearOfCentury(2, 2)
			.toFormatter();
		}
		
		private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, IOException
		{
			in.defaultReadObject();
			
			setFormatters();
		}
		
		public String toString()
		{
			if(airDate != null)
				return airDate.toString(writeFormatter);
			else
				return information.get("title");
		}
		
		public String getText() throws IOException
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append(information.get("title")+":\n");
			if(information.get("epnum")!=null)
				sb.append("Number in series: "+information.get("epnum")+'\n');
			if(information.get("seasonnum")!=null)
				sb.append("Number in season: "+information.get("seasonnum")+"\n\n");
			
			if(airDate != null)
			{
				sb.append("Airdate: "+airDate.toDate().toString()+'\n');
			}
			
			sb.append(timeDifference()+"\n\n");
			
			if(description == null)
			{
				try
				{
					//FIXME: this doesn't always work.
					Document link = Jsoup.connect(information.get("link")).timeout(30*1000).get();
					
					//this grabs the description of the show
					Elements e = link.getElementsByClass("padding_bottom_10").select("div.left:first-of-type");
					description = ((TextNode)e.get(0).childNode(0)).text();
				}
				catch(Exception e)
				{
					description = "";
				}
			}
			
			sb.append(description);
			
			return sb.toString();
		}
		
		public String timeDifference()
		{
			StringBuilder sb = new StringBuilder();
			
			if(airDate != null)
			{
				if(airDate.isAfterNow())
				{
					Period p = new Period((ReadableInstant)null, airDate, PeriodType.standard());
					
					if(p.getDays() == 0 && p.getWeeks() == 0 && p.getMonths() == 0 && p.getYears() == 0)
						sb.append("Airing today at "+airTime+".");
					else
					{
						sb.append("Airing in ");
						if(p.getYears()>0)
							sb.append(p.getYears()+" years, ");
						if(p.getMonths()>0)
							sb.append(p.getMonths()+" months, ");
						if(p.getWeeks()>0)
							sb.append(p.getWeeks()+" weeks, ");
						sb.append("and "+p.getDays()+" days.");
					}
				}
				else
				{
					Period p = new Period(airDate, (ReadableInstant)null, PeriodType.standard());
					
					if(p.getDays() == 0 && p.getWeeks() == 0 && p.getMonths() == 0 && p.getYears() == 0)
						sb.append("Aired today at "+airTime+".");
					else
					{
						sb.append("Aired ");
						if(p.getYears()>0)
							sb.append(p.getYears()+" years, ");
						if(p.getMonths()>0)
							sb.append(p.getMonths()+" months, ");
						if(p.getWeeks()>0)
							sb.append(p.getWeeks()+" weeks, ");
						sb.append("and "+p.getDays()+" days ago.");
					}
				}
			}
			
			return sb.toString();
		}

		public String getTitle()
		{
			return information.get("title");
		}
	}
}