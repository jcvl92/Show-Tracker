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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class ShowTracker
{
	//TODO: fix update crashing when no episodes have been seen
	ArrayList<UpcomingEpisode> upcoming = new ArrayList<UpcomingEpisode>();
	final static Scanner scanner = new Scanner(System.in);
	public ArrayList<ShowEntry> shows;

	public ShowTracker()
	{
		//load the shows from the data file
		shows = readShowsFromFile();
		
		//if there are no shows, initialize the array list
		if(shows == null)
			shows = new ArrayList<ShowEntry>();
	}
	
	public static void main(String[] args)
	{
		//create instance
		final ShowTracker st = new ShowTracker();
		
		try
		{
			System.out.println("Welcome to the Show Tracker!\n");
	
			//if there are shows, print the upcoming ones
			if(st.shows != null)
			{
				System.out.println("Upcoming:");
				if(st.showLinks() > 0)
				{
					System.out.print("\nDownload all?(y/n): ");
					if(scanner.nextLine().equals("y"))
					{
						System.out.println();
						st.openMagnetLinks();
						System.out.println();
					}
				}
				else System.out.println("\tNothing.");
			}
			
			//command loop
			System.out.println();
			command:while(true)
			{
				System.out.println("1 - manage show catalog\n"
						+ "2 - print unseen aired episodes\n"
						+ "3 - download unseen episodes\n"
						+ "4 - browse episodes\n"
						+ "5 - print upcoming episodes\n"
						+ "6 - print show timelines\n"
						+ "7 - update show catalog\n"
						+ "8 - exit");
	
				switch(scanner.nextLine())
				{
				case "1":
					st.manageShows();
					break;
				case "2":
					System.out.println();
					st.showLinks();
					System.out.println();
					break;
				case "3":
					System.out.println();
					st.openMagnetLinks();
					System.out.println();
					break;
				case "4":
					st.browse();
					System.out.println();
					break;
				case "5":
					System.out.println('\n'+st.upcomingEpisodes());
					break;
				case "6":
					System.out.print('\n'+st.timeline());
					break;
				case "7":
					System.out.println();
					st.updateShows();
					System.out.println();
					break;
				case "8":
					break command;
				default:
					System.out.println("\nInvalid input, try again.");
					break;
				}
				
				new Thread(){public void run(){st.writeShowsToFile();}}.start();
			}
		}
		catch(Exception e)
		{
			if(st.shows.size()>0)
			{
				System.out.println("There was a problem. Updating show file and exiting.");
				st.updateShows();
				st.writeShowsToFile();
			}
			else
				System.out.println("There was a problem. Exiting without updating.");
		}

		//close the scanner
		scanner.close();
	}

	public void browse()
	{
		try
		{
			while(true)
			{
				//print out the shows
				System.out.println();
				for(int i=0;i<shows.size();++i)
				{
					System.out.println((i+1)+". "+shows.get(i));
				}
				System.out.println("\nSelect a show. (1-"+shows.size()+")(0 to backup)");

				//get the response
				int showResponse = Integer.parseInt(scanner.nextLine())-1;
				//back up if 0
				if(showResponse == -1)
					break;

				//browse the selected show
				ShowEntry show = shows.get(showResponse);
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
					int seasonResponse = Integer.parseInt(scanner.nextLine())-1;
					//back up if 0
					if(seasonResponse == -1)
						break;

					//browse the selected season
					else
					{
						Season season = show.seasons.get(seasonResponse);
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
							int episodeResponse = Integer.parseInt(scanner.nextLine())-1;
							//back up if 0
							if(episodeResponse == -1)
								break;

							//browse the selected episode
							else
							{
								Episode episode = season.episodes.get(episodeResponse);

								while(true)
								{
									System.out.println("\n************************************************************"+
											'\n'+episode.getText()+
											"\n************************************************************"
											+ "\nChoose an action(0 - Backup, 1 - Set as last watched, 2 - Download)");

									int actionResponse = Integer.parseInt(scanner.nextLine());

									if(actionResponse == 0)
										break;
									else if(actionResponse == 1)
									{
										show.seasonPos = seasonResponse;
										show.episodePos = episodeResponse;
										System.out.println("Watch position set to this episode.");
									}
									else if(actionResponse == 2)
										try
										{
											getMagnetLink(show, episode).open();
											System.out.println(episode+"("+episode.getEpisodeNumber()+") opened.");
										}
									catch(Exception e)
									{
										System.out.println(episode+"("+episode.getEpisodeNumber()+") unavailable.");
									}
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e){}
	}

	private void openMagnetLinks()
	{
		//iterate through each show and open magnet links for that show
		for(int i=0;i<shows.size();++i)
		{
			ShowEntry show = shows.get(i);
			//System.out.println(show+":");
			//boolean anyLinks=false;

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
						//anyLinks=true;

						try
						{
							//open the link
							getMagnetLink(show, episode).open();
							System.out.println(show+": "+episode+'('+episode.getEpisodeNumber()+") opened.");

							//advance the watch position to here
							show.seasonPos = j;
							show.episodePos = k;
						}
						catch(Exception e)
						{
							System.out.println("\t"+episode+'('+episode.getEpisodeNumber()+") unavailable.");
							break;
						}
					}
				}
			}

			//if(!anyLinks)
			//	System.out.println("\tNo upcoming shows available.");
		}
	}
	
	public int showLinks()
	{
		int linkNum=0;
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
						System.out.println(show+" - "+episode+"("+episode.getEpisodeNumber()+')');
						
						//increment the link counter
						++linkNum;
					}
				}
			}
		}
		return linkNum;
	}
	
	public MagnetLink getMagnetLink(ShowEntry show, Episode episode) throws IOException
	{
		Element result = Jsoup.connect("http://thepiratebay.se/search/"+show.search+' '+episode.getEpisodeNumber()+"/0/7/0").timeout(30*1000).get().getElementsByClass("detName").first();

		return new MagnetLink(result.text(), result.siblingElements().get(0).attr("href"));
		//return new MagnetLink("",""); //safe mode(links are not gathered)
	}

	public void manageShows()
	{
		String response;

		try
		{
			while(true)
			{
				System.out.println("\nCurrent shows:");
				for(int i=0;i<shows.size();++i)
					System.out.println(i+". "+shows.get(i).showName+
							(shows.get(i).getLastEpisodeWatched()!=null ?
									" ("+shows.get(i).getLastEpisodeWatched().getEpisodeNumber()+')' : ""));

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
	
	private void removeShowFromFile(int index)
	{
		shows.remove(index);
		writeShowsToFile();
	}
}