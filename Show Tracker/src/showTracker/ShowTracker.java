package showTracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;

public class ShowTracker
{
	public ArrayList<Show> shows;

	public ShowTracker()
	{
		//load the shows from the data file
		shows = readShowsFromFile();
		
		//if there are no shows, initialize the array list
		if(shows == null)
			shows = new ArrayList<Show>();
	}

	public Episode[] getUnseenEpisodes()
	{
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		
		for(int i=0;i<shows.size();++i)
		{
			Show show = shows.get(i);
			
			//iterate through all seasons
			for(int j=0;j<show.seasons.size();++j)
			{
				Season season = show.seasons.get(j);

				//iterate through all episodes
				for(int k=0;k<season.episodes.size();++k)
				{
					Episode episode = season.episodes.get(k);
					if(!episode.isWatched())
						episodes.add(episode);
				}
			}
		}
		
		episodes.sort(new Comparator<Episode>()
		{
			public int compare(Episode arg0, Episode arg1)
			{
				if(arg0.airDate == null && arg1.airDate == null) return 0;
				if(arg1.airDate == null) return 1;
				if(arg0.airDate == null) return -1;
				return arg0.airDate.compareTo(arg1.airDate);
			}
		});
		return episodes.toArray(new Episode[episodes.size()]);
	}
	
	private ArrayList<Show> readShowsFromFile()
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("show_data")));
			@SuppressWarnings("unchecked")
			ArrayList<Show> shows = (ArrayList<Show>)ois.readObject();
			ois.close();

			return shows;
		}
		catch(FileNotFoundException fnfe)
		{
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Data file corrupted: "+e.getMessage());
			System.exit(0);
			return null;
		}
	}

	public void addShowToFile(Show show)
	{
		shows.add(show);
		writeShowsToFile();
	}
	
	public void writeShowsToFile()
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("show_data")));
			oos.writeObject(shows);
			oos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void removeShowFromFile(int index)
	{
		shows.remove(index);
		writeShowsToFile();
	}
}