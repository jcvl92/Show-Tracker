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
import java.util.Scanner;

import org.joda.time.DateTime;

public class Main
{
	static ArrayList<UpcomingEpisode> upcoming = new ArrayList<UpcomingEpisode>();
	static final Scanner scanner = new Scanner(System.in);
	static ArrayList<ShowEntry> shows;
	static String username;
	
	public static void main(String[] args)
	{
		//get the username
		System.out.println("Enter your username: ");
		username = scanner.nextLine();
		
		//load the shows for this user from their data file
		shows = readShowsFromFile();
		
		//if there are no shows, initialize the array list
		if(shows == null)
			shows = new ArrayList<ShowEntry>();
		
		//command loop
		System.out.println();
		command:while(true)
		{
			System.out.println("1 - manage show catalog\n"
					+ "2 - download unseen episodes\n"
					+ "3 - browse episodes\n"
					+ "4 - print upcoming episodes\n"
					+ "5 - print show timelines\n"
					+ "6 - update show catalog\n"
					+ "7 - exit");
			
			//TODO: use BeanShell's JConsole to make the .jar an executable and more user friendly(COLORS!!!!)
			
			switch(scanner.nextLine())
			{
			case "1":
				manageShows();
				break;
			case "2":
				//TODO: magnet link opener and watched show handler through that
				System.out.println("\nnothing here yet.\n");
				break;
			case "3":
				System.out.println();
				browse();
				System.out.println();
				break;
			case "4":
				System.out.println('\n'+upcomingEpisodes());
				break;
			case "5":
				System.out.print('\n'+timeline());
				break;
			case "6":
				System.out.println();
				updateShows();
				System.out.println();
				break;
			case "7":
				break command;
			default:
				System.out.println("\nInvalid input, try again.");
				break;
			}
		}
		
		//close the scanner
		scanner.close();
	}
	
	private static void browse()
	{
		while(true)
		{
			//print out the shows
			for(int i=0;i<shows.size();++i)
			{
				System.out.println((i+1)+". "+shows.get(i));
			}
			System.out.println("\nSelect a show. (1-"+shows.size()+")(0 to backup)");
			
			//get the response
			int response = Integer.parseInt(scanner.nextLine())-1;
			//back up if 0
			if(response == -1)
				return;
			
			//browse the selected show
			try
			{
				browseShow(shows.get(response));
			}
			catch(Exception e)
			{
				return;
			}
		}
	}
	
	private static void browseShow(ShowEntry show)
	{
		while(true)
		{
			//print out the seasons
			System.out.println();
			for(int i=0;i<show.seasons.size();++i)
			{
				System.out.println((i+1)+". "+show.seasons.get(i));
			}
			System.out.println("\nSelect a season. (1-"+show.seasons.size()+")(0 to backup)");
			
			//get the response
			int response = Integer.parseInt(scanner.nextLine())-1;
			//back up if 0
			if(response == -1)
				return;
			//browse the selected season
			else
				browseSeason(show.seasons.get(response));
		}
	}
	
	private static void browseSeason(Season season)
	{
		while(true)
		{
			//print out the episodes
			System.out.println();
			for(int i=0;i<season.episodes.size();++i)
			{
				System.out.println((i+1)+". "+season.episodes.get(i));
			}
			System.out.println("\nSelect an episode. (1-"+season.episodes.size()+")(0 to backup)");
			
			//get the response
			int response = Integer.parseInt(scanner.nextLine())-1;
			//back up if 0
			if(response == -1)
				return;
			//browse the selected episode
			else
				browseEpisode(season.episodes.get(response));
		}
	}
	
	private static void browseEpisode(Episode episode)
	{
		//TODO: getDescription(), setAsLastWatched(), download())
		System.out.println('\n'+episode.getText());
	}
	
	private static void manageShows()
	{
		String response;
		
		try
		{
			while(true)
			{
				System.out.println("\nCurrent shows:");
				for(int i=0;i<shows.size();++i)
					System.out.println(i+". "+shows.get(i).showName+" ("+shows.get(i).getLastEpisodeWatched().getEpisodeNumber()+')');
				
				System.out.println("\nEnter \"add\", \"remove\", \"edit\", or \"quit\".");
				response = scanner.nextLine();
				
				//allow the user to quit
				if(response.equals("quit"))
				{
					System.out.println();
					break;
				}
				
				//add a show
				else if(response.equals("add"))
				{
					System.out.println("Enter a show name(note: the text entered now will be used when searching for magnet links so make sure it is accurate).");
					response = scanner.nextLine();

					try
					{
						ShowEntry show = new ShowEntry(response);
						addShowToFile(show);
						System.out.println("\""+show.showName+"\" added.");
					}
					catch(Exception e)
					{
						System.out.println("\""+response+"\" was not added because "+e);
					}
				}
				
				//remove a show
				else if(response.equals("remove"))
				{
					try
					{
						System.out.print("Select a show(0-"+(shows.size()-1)+"): ");
						removeShowFromFile(Integer.parseInt(scanner.nextLine()));
					}
					catch(Exception e)
					{
						System.out.println("Could not remove entry because "+e);
					}
				}
				
				//edit a show
				else if(response.equals("edit"))
				{
					try
					{
						System.out.print("Select a show(0-"+(shows.size()-1)+"): ");
						shows.get(Integer.parseInt(scanner.nextLine())).manageWatchPosition();;
					}
					catch(Exception e)
					{
						System.out.println("Could not edit entry because "+e);
					}
				}
				
				else
					System.out.println("\nInvalid input, try again.");
			}
		}
		catch(Exception e)
		{
			System.out.println("Error managing shows.");
			return;
		}
	}
	
	private static String upcomingEpisodes()
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
			upcomingShows.append(upcoming.get(i).toString()+'\n');
		}
		
		//return the text
		return upcomingShows.toString();
	}
	
	private static String timeline()
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
	
	private static void updateShows()
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
	
	private static ArrayList<ShowEntry> readShowsFromFile()
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(username+"_data")));
			ArrayList<ShowEntry> shows = (ArrayList<ShowEntry>)ois.readObject();
			ois.close();
			
			return shows;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	private static void addShowToFile(ShowEntry show)
	{
		try
		{
			shows.add(show);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(username+"_data")));   
			oos.writeObject(shows);
			oos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void removeShowFromFile(int index) throws FileNotFoundException, IOException
	{
		shows.remove(index);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("data")));   
		oos.writeObject(shows);
		oos.close();
	}
}