package showTracker;

import gui.Main;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@SuppressWarnings("serial")
public class Episode implements Serializable
{
	HashMap<String, String> information;
	String airTime, description=null;
	private DateTime airDate;
	ImageIcon image = null;
	public Show show;
	boolean seen = false;
	transient DateTimeFormatter parseFormatter;
	transient DateTimeFormatter writeFormatter;

	Episode(HashMap<String, String> info, String time, Show s) throws InterruptedException
	{
		show = s;
		setFormatters();
		information = info;
		airTime = time;
		try
		{
			airDate = DateTime.parse(info.get("airdate")+'-'+airTime.substring(time.lastIndexOf(' ')+1), parseFormatter);
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
		.appendYear(4, 4)
		.toFormatter();
	}

	private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, IOException
	{
		in.defaultReadObject();

		setFormatters();
	}

	public String toString()
	{
		return getSENumber()+" - "+information.get("title")+(isWatched() || !airDate.isBeforeNow() ? "" : "*");
	}

	public String title()
	{
		return information.get("title");
	}

	public String getText()
	{
		if(description == null || image == null)
		{
			try
			{
				Document link = Jsoup.connect(information.get("link")).timeout(60*1000).get();
				
				//grab the description of the episode
				if(description == null)
					try
					{
						description = "";
						description = link.getElementsByClass("show_synopsis").text();
						if(description.equals(""))
							description = link.getElementsByClass("padding_bottom_10").get(1).text();
					}
					catch(Exception e){}

				//grab the image of the episode
				if(image == null)
					try
					{
						try
						{
							image = new ImageIcon();
							image = new ImageIcon(new URL(link.getElementsByClass("padding_bottom_10").get(1).child(0).attr("src")));
						}
						catch(Exception e)
						{
							image = new ImageIcon(new URL(link.getElementsByClass("padding_bottom_10").get(1).child(0).child(0).attr("src")));
						}
					}
					catch(Exception e)
					{
						image = getImageFromTVDB();
					}
			}
			catch(Exception e){}
		}

		return description;
	}
	
	private ImageIcon getImageFromTVDB() throws IOException
	{
		//get the episode link
		Document link = Jsoup.connect("http://thetvdb.com/?tab=seasonall&id="+show.TVDBId).timeout(60*1000).get();
		
		//search word by word until you get only one result(helps find episodes with appended titles)
		String[] words = information.get("title").split(" ");
		String searcher = words[0];
		Elements results = link.getAllElements();
		for(int i=0; i<words.length && results.size()>1; searcher=words[++i])
			results = link.getElementsContainingOwnText(searcher);
		
		String episodeLink = results.get(0).attr("href");
		
		//get the episode ID
		String TVDBEpisodeId = episodeLink.substring(episodeLink.indexOf("&id")+4, episodeLink.indexOf("&lid"));
		
		//get the image
		return new ImageIcon(new URL("http://thetvdb.com/banners/episodes/"+show.TVDBId+"/"+TVDBEpisodeId+".jpg"));
	}

	public ImageIcon getImage()
	{
		//if the image hasn't been grabbed, and the grab fails, return the show image
		if(image ==null || image.getImage() == null)
		{
			getText();
			if(image==null || image.getImage() == null)
				return show.getImage();
		}
		return image;
	}

	public String getDate()
	{
		if(getAirDate() != null)
			return getAirDate().toString(writeFormatter);
		else
			return "Unaired";
	}

	public String getSENumber()
	{
		String seasonNum = information.get("inseason");
		if(seasonNum == null)
			seasonNum = "0";
		if(seasonNum.length()<2)
			seasonNum = '0'+seasonNum;
		return 'S'+seasonNum+'E'+getEpisodeNumber();
	}
	
	public String getEpisodeNumber()
	{
		String episodeNum = information.get("seasonnum");
		if(episodeNum == null)
			return "00";
		if(episodeNum.length()<2)
			return '0'+episodeNum;
		return episodeNum;
	}
	
 	public String timeDifference()
	{
		StringBuilder sb = new StringBuilder();

		if(getAirDate() != null)
		{
			if(getAirDate().isAfterNow())
			{
				Period p = new Period((ReadableInstant)null, getAirDate(), PeriodType.standard());

				sb.append("Airing in");
				if(p.getYears()>0)
					sb.append(" "+p.getYears()+" years");
				if(p.getMonths()>0)
					sb.append(" "+p.getMonths()+" months");
				if(p.getWeeks()>0)
					sb.append(" "+p.getWeeks()+" weeks");
				if(p.getDays()>0)
					sb.append(" "+p.getDays()+" days");
				if(p.getHours()>0)
					sb.append(" "+p.getHours()+" hours");
				if(p.getMinutes()>0)
					sb.append(" "+p.getMinutes()+" minutes");
				sb.append('.');
			}
			else
			{
				Period p = new Period(getAirDate(), (ReadableInstant)null, PeriodType.standard());

				sb.append("Aired");
				if(p.getYears()>0)
					sb.append(" "+p.getYears()+" years");
				if(p.getMonths()>0)
					sb.append(" "+p.getMonths()+" months");
				if(p.getWeeks()>0)
					sb.append(" "+p.getWeeks()+" weeks");
				if(p.getDays()>0)
					sb.append(" "+p.getDays()+" days");
				if(p.getHours()>0)
					sb.append(" "+p.getHours()+" hours");
				if(p.getMinutes()>0)
					sb.append(" "+p.getMinutes()+" minutes");
				sb.append(" ago.");
			}
		}

		return sb.toString();
	}

	public boolean download()
	{
		try
		{
			if(Main.DL_ON)
			{
				//get the link from TPB
				Element result = Jsoup.connect("http://thepiratebay.se/search/"+show.search+' '+getSENumber()+"/0/7/0").timeout(30*1000).get().getElementsByClass("detName").first();

				//open the link
				new MagnetLink(result.text(), result.siblingElements().get(0).attr("href")).open();
			}

			//return true if it worked
			return true;
		}
		catch(Exception e)
		{
			//return false if it failed
			return false;
		}
	}

	public void setWatched(boolean b)
	{
		seen = b;
	}

	public boolean isWatched()
	{
		return seen;
	}

	public DateTime getAirDate()
	{
		return airDate;
	}
}