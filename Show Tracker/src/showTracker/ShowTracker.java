package showTracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.joda.time.DateTime;

public class ShowTracker
{
	//TODO: make sure updates and deletions are all properly modifying the file
	//TODO: fix update crashing when no episodes have been seen
	ArrayList<UpcomingEpisode> upcoming = new ArrayList<UpcomingEpisode>();
	public ArrayList<ShowEntry> shows;

	public ShowTracker()
	{
		//load the shows from the data file
		shows = readShowsFromFile();
		
		//if there are no shows, initialize the array list
		if(shows == null)
			shows = new ArrayList<ShowEntry>();
	}

	public Episode[] showLinks()
	{
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		//int linkNum=0;
		//iterate through each show and open magnet links for that show
		for(int i=0;i<shows.size();++i)
		{
			ShowEntry show = shows.get(i);

			//get the date of the current watch position
			DateTime date = show.getLastEpisodeWatched()!=null ?
					show.getLastEpisodeWatched().airDate
					: new DateTime(0);

			//iterate through all seasons
			for(int j=0;j<show.seasons.size();++j)
			{
				Season season = show.seasons.get(j);

				//iterate through all episodes
				for(int k=0;k<season.episodes.size();++k)
				{
					Episode episode = season.episodes.get(k);
					if(episode.airDate != null && episode.airDate.isAfter(date) && episode.airDate.isBeforeNow())
					{
						//print the episode
						//System.out.println(show+" - "+episode+"("+episode.getEpisodeNumber()+')');
						
						//increment the link counter
						//++linkNum;
						episodes.add(episode);
					}
				}
			}
		}
		return episodes.toArray(new Episode[episodes.size()]);
	}

	public String upcomingEpisodes()
	{
		//reset the list
		upcoming.clear();
		timeline();
		if(upcoming.size() < 1)
		{
			return "No upcoming shows at this time.\n";
		}

		//sort the shows list
		Collections.sort(upcoming, new Comparator<UpcomingEpisode>() {
			public int compare(UpcomingEpisode a, UpcomingEpisode b)
			{
				if(a.episode.airDate.isAfter(b.episode.airDate))
					return 1;
				else if(b.episode.airDate.isAfter(a.episode.airDate))
					return -1;
				else
					return 0;
			}
		});

		//set next shows list text
		StringBuilder upcomingShows = new StringBuilder();
		boolean marker=true;
		for(int i=0; i<upcoming.size(); ++i)
		{
			if(upcoming.get(i).episode.airDate.isAfterNow() && marker)
			{
				upcomingShows.append("-------------------------------------\n");
				marker = false;
			}
			upcomingShows.append(upcoming.get(i)+"\n");
		}

		//return the text
		return upcomingShows.toString();
	}

	public String timeline()
	{
		boolean populate = upcoming.isEmpty();
		StringBuilder timeline = new StringBuilder();
		for(int i=0; i<shows.size(); ++i)
		{
			Episode next = shows.get(i).getNextEpisode();
			Episode last = shows.get(i).getLastEpisode();
			Episode nextToWatch = shows.get(i).getNextEpisodeToWatch();
			Episode lastWatched = shows.get(i).getLastEpisodeWatched();

			timeline.append(shows.get(i).showName+'\n');

			if(last != null)
			{
				timeline.append("\tLast episode: "+last+" ("+last.getEpisodeNumber()+')'+'\n');
				timeline.append("\t\t"+last.timeDifference()+'\n');

				//save this upcoming show in the upcoming show list if it isn't too old
				if(populate && last.airDate.isAfter(new DateTime().minusWeeks(2)))
					upcoming.add(new UpcomingEpisode(last, shows.get(i)));
			}
			else
				timeline.append("\tNo aired episodes listed.\n");

			if(next != null)
			{
				timeline.append("\tNext episode: "+next+" ("+next.getEpisodeNumber()+')'+'\n');
				timeline.append("\t\t"+next.timeDifference()+'\n');

				//save this upcoming show in the upcoming show list
				if(populate && next.airDate.isBefore(new DateTime().plusWeeks(2)))
					upcoming.add(new UpcomingEpisode(next, shows.get(i)));
			}
			else
				timeline.append("\tNo upcoming episodes listed.\n");

			if(lastWatched != null)
			{
				timeline.append("\tLast watched episode: "+lastWatched+" ("+lastWatched.getEpisodeNumber()+')'+'\n');
				timeline.append("\t\t"+lastWatched.timeDifference()+'\n');
			}
			else
				timeline.append("\tNo aired episodes seen.\n");

			if(nextToWatch != null)
			{
				timeline.append("\tNext episode to watch: "+nextToWatch+" ("+nextToWatch.getEpisodeNumber()+')'+'\n');
				timeline.append("\t\t"+nextToWatch.timeDifference()+'\n');
			}
			else
				timeline.append("\tNo upcoming episodes to be seen.\n");

			timeline.append('\n');
		}

		return timeline.toString();
	}

	public void updateShows()
	{
		for(int i=0; i<shows.size(); ++i)
		{
			try
			{
				shows.get(i).update();
				System.out.println(shows.get(i).showName+" updated.");
			}
			catch (IOException | InterruptedException ie){}
		}
	}

	private ArrayList<ShowEntry> readShowsFromFile()
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("show_data")));
			@SuppressWarnings("unchecked")
			ArrayList<ShowEntry> shows = (ArrayList<ShowEntry>)ois.readObject();
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

	public void addShowToFile(ShowEntry show)
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