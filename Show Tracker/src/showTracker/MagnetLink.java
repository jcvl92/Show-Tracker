package showTracker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
	
	public String toString()
	{
		if(name == null || link == null)
			return "";
		return name+" - "+link;
	}
	

	public boolean open()
	{
		return false;
	}
}