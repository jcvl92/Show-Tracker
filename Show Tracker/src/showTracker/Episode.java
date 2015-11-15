package showTracker;

import gui.Main;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Episode implements Serializable
{
	HashMap<String, String> information;
	private DateTime airDate;
	ImageIcon image = null;
	public Show show;
	boolean seen = false;
	transient DateTimeFormatter parseFormatter;
	transient DateTimeFormatter writeFormatter;

	Episode(HashMap<String, String> info, Show show)
	{
		this.show = show;
		information = info;
		setFormatters();
		try {
			airDate = DateTime.parse(info.get("firstaired")+"-"+show.showData.get("airs_time"), parseFormatter);
		} catch (Exception e) {}
		airDate = airDate;
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
		.appendHourOfDay(1)
		.appendLiteral(':')
		.appendMinuteOfHour(2)
		.appendLiteral(' ')
		.appendHalfdayOfDayText()
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
		return getSENumber()+" - "+title()+(isWatched() || (airDate != null && !airDate.isBeforeNow()) ? "" : "*");
	}

	public String title()
	{
		return information.get("episodename");
	}

	public String getText()
	{
		return information.get("overview");
	}

	public ImageIcon getImage()
	{
		if(image == null || image.getImage() == null || image.getImageLoadStatus()==MediaTracker.ERRORED)
		{
			try {
				URL url = new URL("http://thetvdb.com/banners/" + information.get("filename"));
				url.getContent();
				image = new ImageIcon(url);
			} catch (Exception e) {}
			if(image == null || image.getImage() == null || image.getImageLoadStatus()==MediaTracker.ERRORED)
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
		return "S"+ getSeasonNumber() +'E'+ getEpisodeNumber();
	}

	public String getSeasonNumber()
	{
		String seasonNum = information.get("combined_season");
		if(seasonNum == null || seasonNum == "")
			return "00";
		if(seasonNum.length()==1)
			return '0'+seasonNum;
		return seasonNum;
	}

	public String getEpisodeNumber()
	{
		String episodeNum = information.get("combined_episodenumber");
		if(episodeNum.contains(".")) episodeNum = episodeNum.substring(0, episodeNum.indexOf('.'));
		if(episodeNum == null || episodeNum == "")
			return "00";
		if(episodeNum.length()==1)
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
				//get the magnet link from TPB
				String link = "https://thepiratebay.se/search/"+show.getSearchText()+' '+getSENumber();
				//try HD first
				Element result = Jsoup.connect(link+"/0/99/208").userAgent("Mozilla").timeout(30*1000).get().getElementsByClass("detName").first();
				//if no HD, try all categories
				if(result == null)
					result = Jsoup.connect(link+"/0/99/0").userAgent("Mozilla").timeout(30*1000).get().getElementsByClass("detName").first();
				
				//open the magnet link
				new MagnetLink(result.text(), result.siblingElements().get(0).attr("href")).open();
			}

			//return true if it worked
			return true;
		}
		catch(Exception e)
		{
			//return false if it failed
			e.printStackTrace();
			return false;
		}
	}

	public void setWatched(boolean b)
	{
		seen = b;
		ShowTracker.ShowArrayList.setDirty(true);
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