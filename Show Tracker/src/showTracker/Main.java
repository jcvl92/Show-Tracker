package showTracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

import org.joda.time.DateTime;

public class Main
{
	transient String homeScreenText;
	transient ArrayList<UpcomingEpisode> upcoming = new ArrayList<UpcomingEpisode>();
	ShowDatabase db;
	ShowEntry[] userShows;
	String[] showList = {"rick and morty", "american dad",
			"family guy", "south park", "aqua teen hunger force",
			"squidbillies", "parks and recreation", "adventure time",
			"regular show", "workaholics"};
	//TODO: complete these tasks and resolve all TODOs before moving to android
	//TODO: implement graceful page errors(description not being gathered some of the time)
	//TODO: serialize episodes? - maybe we should find a more robust api(our own?) before implementing this. nah, we can just serialize an interface!
	//show table stores the actual ShowEntry objects
	//the show entries are updated every day by the users who access them(this happens via a background thread)
	//make the users do the legwork in background processes
	//if a show in a user's list needs to be updated, a process is spawned that starts gathering that information and it sends to to the database
	
	public static void main(String[] args) throws Exception
	{
		new Main().run();
	}
	
	public void run() throws Exception
	{
		//establish connection to database
		db = new ShowDatabase("localhost/program_test", "user", "pass");
		
		//enter account information
		Scanner s = new Scanner(System.in);
		System.out.print("Enter your username: ");
		String username = s.next();
		System.out.print("Enter your password: ");
		String password = s.next();
		
		//get user object from server or create a new user object
		User thisUser = db.getUser(username, password);
		if(thisUser == null)
		{
			thisUser = new User(username, password);
			System.out.println("Profile created.");
		}
		else
			System.out.println("Profile loaded.");
		
		//manage your list:
		System.out.println("Would you like to add any shows to your profile?(y/n)");
		String response = s.next();
		if(response.equals("y"))
			for(int i=0; i<showList.length; ++i)
			{
				System.out.println("Enter the name of a show to add.(q to quit)");
				response = s.next();
				
				if(response.equals("q"))
					break;
				
				ShowEntry show = db.getShow(ShowEntry.getID(response));
				if(show == null)
				{
					show = new ShowEntry(response);
					db.updateShow(show);
				}
				
				thisUser.addShow(new ShowStatus(show.showID, show.seasons.get(0).episodes.get(0).information.get("epnum")));
				
				System.out.println(show.showName+" added.");
			}
		
		//update the user in the database
		db.updateUser(thisUser);
		
		//get show entry objects from server
		ShowStatus[] userShowStatuses = thisUser.getShowList();
		userShows = new ShowEntry[userShowStatuses.length];
		for(int i=0; i< userShows.length; ++i)
			userShows[i] = db.getShow(userShowStatuses[i].showID);
		
		//print out our text junk
		timeline();//so that upcomingShows are computed
		System.out.println(upcomingShows());
		//close the scanner
		s.close();
	}
	
	public String upcomingShows()
	{
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
		for(int i=0; i<upcoming.size(); ++i)
			upcomingShows.append(upcoming.get(i).toString()+'\n');
		if(upcoming.size() < 1)
			upcomingShows.append("No upcoming shows at this time.\n");
		
		return upcomingShows.toString();
	}
	
	public String timeline()
	{
		StringBuilder timeline = new StringBuilder();
		for(int i=0; i<userShows.length; ++i)
		{
			Episode next = userShows[i].getNextEpisode();
			Episode last = userShows[i].getLastEpisode();
			
			timeline.append(userShows[i].showName+'\n');
			
			if(last != null)
			{
				timeline.append("\tLast episode: "+last.getTitle()+'\n');
				timeline.append("\t\t"+last.timeDifference()+'\n');
				
				//save this upcoming show in the upcoming show list if it isn't too old
				if(last.airDate.isAfter(new DateTime().minusWeeks(2)))
						upcoming.add(new UpcomingEpisode(last, userShows[i]));
			}
			else
				timeline.append("\t\tNo aired episodes listed.\n");
			
			if(next != null)
			{
				timeline.append("\tNext episode: "+next.getTitle()+'\n');
				timeline.append("\t\t"+next.timeDifference()+'\n');
				
				//save this upcoming show in the upcoming show list
				upcoming.add(new UpcomingEpisode(next, userShows[i]));
			}
			else
				timeline.append("\tNo upcoming episodes listed.\n");
			timeline.append('\n');
		}
		
		return timeline.toString(); 
	}

	@SuppressWarnings("unused")
	private ShowEntry getShowFromFile(String showName)
	{
		//attempt to get the entry from a file
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(".\\data\\"+showName.replaceAll("\\W+", "_"))));   
			ShowEntry show = (ShowEntry)ois.readObject();
			ois.close();
			
			return show;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	@SuppressWarnings("unused")
	private void addShowToFile(ShowEntry show, String showName)
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(".\\data\\"+showName.replaceAll("\\W+", "_"))));   
			oos.writeObject(show);
			oos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}