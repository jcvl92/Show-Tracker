package showTracker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jsoup.Jsoup;
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
	
	@Override
	public String toString()
	{
		String time = episode.timeDifference();
		for(int i=time.length(); i<47; ++i)
			time += ' ';
		
		return time+show.showName;
	}
	
	public MagnetLink getMagnetLink()
	{
		try
		{
			Element result = Jsoup.connect("http://thepiratebay.se/search/"+show.search+' '+episode.getTPBTag()+"/0/7/0").timeout(30*1000).get().getElementsByClass("detName").first();
			
			return new MagnetLink(result.text(), result.siblingElements().get(0).attr("href"));
		}
		catch (IOException e)
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
			try
			{
				link = new java.util.Scanner((Readable) ((HttpURLConnection) new URL("http://tinyurl.com/api-create.php?url="+magLink).openConnection()).getContent()).useDelimiter("\\A").next();
			}
			catch (IOException e)
			{
				link = "";
			}
		}
		
		@Override
		public String toString()
		{
			if(name == null || link == null)
				return "";
			return name+" - "+link;
		}
	}
}