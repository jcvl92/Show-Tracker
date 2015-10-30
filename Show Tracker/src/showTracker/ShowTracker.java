package showTracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ShowTracker implements AutoCloseable
{
	public static String apiKey = "2E1B82E95982EB80";
	@SuppressWarnings("serial")
	public static class ShowArrayList extends ArrayList<Show>
	{
		public static Boolean dirty = false;
		ShowArrayList()
		{
			super();
			synchronized(dirty)
			{
				dirty = true;
			}
		}
		public boolean add(Show s)
		{
			synchronized(dirty)
			{
				dirty = true;
				return super.add(s);
			}
		}
		public Show remove(int i)
		{
			synchronized(dirty)
			{
				dirty = true;
				return super.remove(i);
			}
		}
		public boolean isDirty()
		{
			synchronized(dirty)
			{
				boolean b = dirty;
				dirty = false;
				return b;
			}
		}
		public static void setDirty(boolean b)
		{
			synchronized(dirty)
			{
				dirty = b;
			}
		}
	}
	public static Comparator<Episode> episodeComparator = new Comparator<Episode>()
	{
		public int compare(Episode arg0, Episode arg1)
		{
			if(arg0.getAirDate() == null && arg1.getAirDate() == null) return 0;
			if(arg1.getAirDate() == null) return 1;
			if(arg0.getAirDate() == null) return -1;
			int result = arg0.getAirDate().compareTo(arg1.getAirDate());
			return result!=0 ?  result : Integer.parseInt(arg0.getEpisodeNumber()) - Integer.parseInt(arg1.getEpisodeNumber());
		}
	};
	public static ShowArrayList shows;

	public ShowTracker()
	{
		//load the shows from the data file
		shows = readShowsFromFile();
		
		//if there are no shows, initialize the array list
		if(shows == null)
			shows = new ShowArrayList();
		
		//spawn the file writer
		/*new Thread()
		{
			public void run()
			{
				while(true)
				{
					try
					{
						sleep(1000);
						close();
					}
					catch(Exception e){}
				}
			}
		}.start();*/
	}

	public ArrayList<Episode> getUnseenEpisodes()
	{
		synchronized(shows)
		{
			ArrayList<Episode> episodes = new ArrayList<Episode>();

			for(Show show : shows)
				for(Season season : show.seasons.values())
					for(Episode episode : season.episodes)
						if(!episode.isWatched() && (episode.getAirDate()!=null && episode.getAirDate().isBeforeNow()))
							episodes.add(episode);

			Collections.sort(episodes, episodeComparator);
			
			return episodes;
		}
	}

	private ShowArrayList readShowsFromFile()
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("show_data")));
			@SuppressWarnings("unchecked")
			ShowArrayList shows = (ShowArrayList)ois.readObject();
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

	public static void addShow(Show show)
	{
		synchronized(shows)
		{
			shows.add(show);
		}
	}

	public static void removeShow(int index)
	{
		synchronized(shows)
		{
			shows.remove(index);
		}
	}

	public static Episode[] getTimelineEpisodes(long timelineBegin, long timelineEnd)
	{
		ArrayList<Episode> times = new ArrayList<Episode>();

		for(Show show : shows)
			for(Season season : show.seasons.values())
				for(Episode episode : season.episodes) {
					if (episode.getAirDate() == null)
						continue;
					long episodeTime = episode.getAirDate().toDate().getTime();
					if (episodeTime > timelineBegin && episodeTime < timelineEnd)
						times.add(episode);
				}
		
		Collections.sort(times, episodeComparator);
		
		return times.toArray(new Episode[times.size()]);
	}
	
	public void close() throws Exception
	{
		synchronized(shows)
		{
			if(shows.isDirty())
			{
				//read in the old file data
				ShowArrayList temp = readShowsFromFile();
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("show_data", false));
				try {
					oos.writeObject(shows);
					//revert the change if it would corrupt the file
					if(new ObjectInputStream(new FileInputStream(new File("show_data"))).readObject() != null)
						throw new Exception();
				} catch(Exception e) {
					oos.writeObject(temp);
				} finally {
					oos.close();
				}
			}
		}
	}
}