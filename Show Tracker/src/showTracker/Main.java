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
	String homeScreenText;
	ArrayList<UpcomingEpisode> upcoming = new ArrayList<UpcomingEpisode>();
	public static boolean linksOn = false;
	Scanner scanner = new Scanner(System.in);
	User thisUser;
	ShowDatabase db;
	ShowEntry[] userShows;
	//TODO: move scraping to use wikipedia(that is probably free, we can't afford to populate a giant database)
	//TODO: read from local files before reading from the DB(updating system is required for this)
	
	public static void main(String[] args) throws Exception
	{
		new Main().run();
	}
	
	public void run() throws Exception
	{
		//establish connection to database
		db = new ShowDatabase("localhost/test", "root", "password");
		
		//ask the user if we need to turn on linking
		System.out.println("Turn on magnet link gatherer?(y/n) - (longer load time and NSFW traffic)");
		linksOn = scanner.nextLine().equals("y");
		
		//attempt to log in/register
		if((thisUser = login()) == null)
			return;
		
		//manage their list:
		System.out.println("Would you like to add any shows to your profile?(y/n)");
		String response = scanner.nextLine();
		if(response.equals("y"))
		{
			manageShows();
			
			//update the user in the database
			db.updateUser(thisUser);
		}
		
		//get show entry objects from server
		ShowStatus[] userShowStatuses = thisUser.getShowList();
		userShows = new ShowEntry[userShowStatuses.length];
		for(int i=0; i< userShows.length; ++i)
			//note: if a showid is in a user's list, it is guaranteed to be in the show database
			userShows[i] = db.getShow(userShowStatuses[i].showID);
		
		//print out our text stuff
		timeline();//so that upcomingShows are computed
		System.out.println('\n'+upcomingShows());
		
		//close the scanner
		scanner.close();
	}
	
	public void manageShows()
	{
		String response;
		
		for(int i=0; i<thisUser.getShowList().length; ++i)
			System.out.println(thisUser.getShowList()[i].showName);
		try
		{
			while(true)
			{
				System.out.println("Enter the name of a show to add.(q to quit)");
				
				response = scanner.nextLine();
				
				//allow the user to quit
				if(response.equals("q"))
					break;
				
				//try and get the show entry from the database
				ShowEntry show = db.getShow(response);
				
				//if that was unsuccessful, create a new entry and add it to the database
				if(show == null)
				{
					//get the show
					show = new ShowEntry(response);
					db.updateShow(show);
					
					//add the show to this user's account
					thisUser.addShow(new ShowStatus(show));
					
					System.out.println(show.showName+" entry created.");
				}
				else
				{
					//skip this if it is in the list already
					if(thisUser.isInList(show.showID))
					{
						System.out.println("That show is already in your preferences!");
					}
					//or add it into their account 
					else
					{
						//add the show to this user's account
						thisUser.addShow(new ShowStatus(show));
						
						System.out.println(show.showName+" added.");
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error managing shows.");
			return;
		}
	}
	
	public User login()
	{
		try
		{
			System.out.print("Enter your username: ");
			String username = scanner.nextLine();
			System.out.print("Enter your password: ");
			String password = scanner.nextLine();
			
			//get user object from server or create a new user object
			User thisUser = db.getUser(username, password);
			if(thisUser == null)
			{
				thisUser = new User(username, password);
				System.out.println("Profile created.");
			}
			else
				System.out.println("Profile loaded.");
			
			return thisUser;
		}
		catch(Exception e)
		{
			System.out.println("Error logging in.");
			
			return null;
		}
	}
	
	public String upcomingShows()
	{
		if(upcoming.size() < 1)
			return "No upcoming shows at this time.\n";
		
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
		
		//return the text
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