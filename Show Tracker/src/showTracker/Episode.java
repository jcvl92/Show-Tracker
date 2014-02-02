package showTracker;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

@SuppressWarnings("serial")
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

	public String getTitle()
	{
		return information.get("title");
	}
	
	public String getTPBTag()
	{
		String seasonNum = information.get("inseason"),
				episodeNum =information.get("seasonnum");
		if(seasonNum.length()<2)
			seasonNum = '0'+seasonNum;
		if(episodeNum.length()<2)
			episodeNum = '0'+episodeNum;
		return 'S'+seasonNum+'E'+episodeNum;
	}
}