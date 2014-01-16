package showTracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
	//TODO: implement graceful page errors
	//TODO: implement login system for getting and saving of serialized objects into the login database
	//login table stores the list of show IDs and positions in those shows(seriesnum integer)
	//show table stores the actual ShowEntry objects
	//the show entries are updated every day by the users who access them(this happens via a background thread)
	
	//after all the database stuff and caching is done, the GUI work can start.
	//we need a rich interface that is capable of drawing timelines, load images, cleanly paint custom objects, etc.
	//maybe we should move to an android app before we start working on the GUI.
	//an android GUI will be much easier to manipulate, it will be better documented, and it is the end result we want anyway
	
	public static void main(String[] args) throws Exception
	{
		new Main().run();
	}
	
	public void run() throws Exception
	{
		//establish connection to database
		db = new ShowDatabase("", "", "");
		
		//enter account information
		String username = "joe", password = "password";
		
		//get user object from server or create a new user object
		User thisUser = db.getUser(username, password);
		if(thisUser == null)
		{
			thisUser = new User(username, password);
		}
		
		//manage your list:
		//would you like to add any shows to your list?
		//get create and insert any new shows from here, into the database
		/*for(int i=0; i<showList.length; ++i)
		{
			//thread this out here? - don't bother, we are moving to android before we thread anything or mess with the GUI.
			ShowEntry s = getShowFromFile(showList[i]);
			if(s == null)
			{
				s = new ShowEntry(showList[i]);
				if(s != null)
					addShowToFile(s, showList[i]);
			}
			else
			{
				//update this entry? tree drawing needs to be made dynamic so that the
				//list can be populated as it is created and so updates can run
			}
			myShows.add(s);
		}*/
		
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