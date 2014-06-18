package showTracker;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MagnetLink
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


	public void open() throws IOException, URISyntaxException
	{
		Desktop.getDesktop().browse(new URI(link));
	}
}