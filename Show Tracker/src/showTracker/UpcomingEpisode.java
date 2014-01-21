package showTracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class UpcomingEpisode
{
	Episode episode;
	ShowEntry show;
	
	UpcomingEpisode(Episode e, ShowEntry se)
	{
		episode = e;
		show = se;
	}
	
	public String toString()
	{
		MagnetLink magLink = null;
		
		if(Main.linksOn)
			magLink = getMagnetLink();
		
		try
		{
			if(magLink != null)
				magLink.link = WebHandler.getBarePage("http://tinyurl.com/api-create.php?url="+magLink.link).trim();
		}
		catch(Exception e){e.printStackTrace();}
		
		String time = episode.timeDifference();
		for(int i=time.length(); i<40; ++i)
			time += ' ';
		
		return time+(magLink!=null ? magLink : show.showName);
	}
	
	public MagnetLink getMagnetLink()
	{
		Document search = null;
		try
		{
			search = Jsoup.connect("http://thepiratebay.se/search/"+show.search+' '+episode.getTPBTag()+"/0/7/0").timeout(30*1000).get();
			
			Element result = search.getElementsByClass("detName").first();
			
			return new MagnetLink(result.text(), result.siblingElements().get(0).attr("href"));
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	class MagnetLink
	{
		String name, link;
		MagnetLink(String text, String magLink)
		{
			name = text;
			link = magLink;
		}
		public String toString()
		{
			if(name == null || link == null)
				return "";
			return name+" - "+link;
		}
	}
}