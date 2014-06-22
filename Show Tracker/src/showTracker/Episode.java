package showTracker;

import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

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
	DateTime airDate;
	Image image = null;
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
		return information.get("title");
	}

	public String getText()
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
				Document link = Jsoup.connect(information.get("link")).timeout(30*1000).get();
				
				//this grabs the description of the episode
				description = link.getElementsByClass("show_synopsis").text();
				
				//this grabs the image of the episode
				image = ImageIO.read(new URL(link.getElementsByClass("padding_bottom_10").get(1).child(0).attr("src")));
			}
			catch(Exception e)
			{
				description = "";
				image = null;
			}
		}

		sb.append(description);

		return sb.toString();
	}

	public Image getImage()
	{
		//if the image hasn't been grabbed, and the grab fails, return null
		if(image == null && getText() == null)
		    return null;
	    return image;
	}
	
	public String getDate()
	{
		if(airDate != null)
			return airDate.toString(writeFormatter);
		else
			return "";
	}

	public String getEpisodeNumber()
	{
		String seasonNum = information.get("inseason"),
				episodeNum = information.get("seasonnum");
		if(seasonNum == null)
			seasonNum = "0";
		if(episodeNum == null)
			episodeNum = "0";
		if(seasonNum.length()<2)
			seasonNum = '0'+seasonNum;
		if(episodeNum.length()<2)
			episodeNum = '0'+episodeNum;
		return 'S'+seasonNum+'E'+episodeNum;
	}

	public String timeDifference()
	{
		StringBuilder sb = new StringBuilder();

		if(airDate != null)
		{
			if(airDate.isAfterNow())
			{
				Period p = new Period((ReadableInstant)null, airDate, PeriodType.standard());

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
				Period p = new Period(airDate, (ReadableInstant)null, PeriodType.standard());

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
			if(true) return false;
			
			//get the link from TPB
			Element result = Jsoup.connect("http://thepiratebay.se/search/"+show.search+' '+getEpisodeNumber()+"/0/7/0").timeout(30*1000).get().getElementsByClass("detName").first();
			
			//open the link
			new MagnetLink(result.text(), result.siblingElements().get(0).attr("href")).open();
			
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
}