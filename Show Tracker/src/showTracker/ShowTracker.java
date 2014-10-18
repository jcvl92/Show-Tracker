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
		new Thread()
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
		}.start();
	}

	public ArrayList<Episode> getUnseenEpisodes()
	{
		synchronized(shows)
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
						if(!episode.isWatched() && (episode.getAirDate()!=null && episode.getAirDate().isBeforeNow()))
							episodes.add(episode);
					}
				}
			}

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

		for(int i=0; i<shows.size(); ++i)
		{
			Show show = shows.get(i);
			for(int j=0; j<shows.get(i).seasons.size(); ++j)
			{
				Season season = show.seasons.get(j);
				for(int k=0; k<shows.get(i).seasons.get(j).episodes.size(); ++k)
				{
					Episode episode = season.episodes.get(k);
					if(episode.getAirDate() == null)
						continue;
					long episodeTime = episode.getAirDate().toDate().getTime();
					if(episodeTime > timelineBegin && episodeTime < timelineEnd)
						times.add(episode);
				}
			}
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
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("show_data", false));
				oos.writeObject(shows);
				oos.flush();
				oos.close();
			}
		}
	}
}