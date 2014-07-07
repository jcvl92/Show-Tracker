package showTracker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;

public class ShowTracker
{
	public static ArrayList<Show> shows;
	public static ReentrantLock writing = new ReentrantLock();
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

	public ShowTracker()
	{
		//load the shows from the data file
		shows = readShowsFromFile();

		//if there are no shows, initialize the array list
		if(shows == null)
			shows = new ArrayList<Show>();
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
						if(!episode.isWatched() && (episode.getAirDate()==null || episode.getAirDate().isBeforeNow()))
							episodes.add(episode);
					}
				}
			}

			Collections.sort(episodes, episodeComparator);
			
			return episodes;
		}
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

	public static void addShowToFile(Show show)
	{
		synchronized(shows)
		{
			shows.add(show);
			writeShowsToFile();
		}
	}

	public static void writeShowsToFile()
	{
		synchronized(shows)
		{
			try
			{
				writing.lock();
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File("show_data"), false)));
				oos.writeObject(shows);
				oos.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				writing.unlock();
			}
		}
	}

	public static void removeShowFromFile(int index)
	{
		synchronized(shows)
		{
			shows.remove(index);
			writeShowsToFile();
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
}