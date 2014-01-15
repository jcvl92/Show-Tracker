package showTracker;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import showTracker.ShowEntry.Episode;
import showTracker.ShowEntry.Season;

public class Main
{
	private JTextArea text;
	private JTree tree;
	String homeScreenText;
	ArrayList<ShowEntry> myShows = new ArrayList<ShowEntry>();
	ArrayList<UpcomingEpisode> upcoming = new ArrayList<UpcomingEpisode>();
	
	Main(JTextArea t, JTree tr)
	{
		text = t;
		tree = tr;
		
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
	}
	
	public void run()
	{
		//enter account information
		
		//load list of shows
		String[] showList = {"rick and morty", "american dad",
				"family guy", "south park", "aqua teen hunger force",
				"squidbillies", "parks and recreation"};
		
		for(int i=0; i<showList.length; ++i)
			myShows.add(new ShowEntry(showList[i]));
		
		//set the timeline information
		StringBuilder timeline = new StringBuilder();
		for(int i=0; i<myShows.size(); ++i)
		{
			Episode next = myShows.get(i).getNextEpisode();
			Episode last = myShows.get(i).getLastEpisode();
			
			timeline.append(myShows.get(i).showName+'\n');
			if(next != null)
			{
				timeline.append("\tNext episode: "+next.getTitle()+"\n");
				timeline.append("\t\t"+next.timeDifference());
				
				//save this upcoming show in the upcoming show list
				upcoming.add(new UpcomingEpisode(next, myShows.get(i)));
			}
			else
				timeline.append("\tNo upcoming episodes listed.\n");
			
			if(last != null)
			{
				timeline.append("\tLast episode: "+last.getTitle()+"\n");
				timeline.append("\t\t"+last.timeDifference());
			}
			else
				timeline.append("\t\tNo aired episodes listed.\n");
			timeline.append("\n");
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
		
		//remove anything more than 2 weeks from now
		
		//set next shows list
		StringBuilder upcomingShows = new StringBuilder();
		for(int i=0; i<upcoming.size(); ++i)
		{
			//if(within two weeks)
			upcomingShows.append(upcoming.get(i).toString()+'\n');
		}
		if(upcoming.size() < 1)
		{
			upcomingShows.append("No upcoming shows at this time.\n");
		}
		
		//set the home screen text
		homeScreenText = upcomingShows+"\n\n"+timeline;
		
		//set the initial text to the homeScreenText
		text.setText(homeScreenText);
		
		//set the contents of the JTree
		//set the root node
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("My Shows");
		
		//add each show to the root node
		for(int i=0; i<myShows.size(); ++i)
		{
			ShowEntry se = myShows.get(i);
			DefaultMutableTreeNode show = new DefaultMutableTreeNode(se);
			
			//add each season to the show nodes
			for(int j=0; j<se.seasons.size(); ++j)
			{
				Season s = se.seasons.get(j);
				DefaultMutableTreeNode season = new DefaultMutableTreeNode(s);
				
				//add each episode to the season nodes
				for(int k=0; k<s.episodes.size(); ++k)
				{
					Episode e = s.episodes.get(k);
					DefaultMutableTreeNode episode = new DefaultMutableTreeNode(e);
					
					season.add(episode);
				}
				
				show.add(season);
			}
			
			root.add(show);
		}
		
		//build the listener for the node selection event and set the tree
		TreeSelectionListener tsl = new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

				//set the text from the Episode, Season, or ShowEntry that was selected
				try
				{
					Object obj = node.getUserObject();
					if(obj.getClass() == Episode.class)
						text.setText(((Episode)obj).getText());
					else if(obj.getClass() == Season.class)
						text.setText(((Season)obj).getText());
					else if(obj.getClass() == ShowEntry.class)
						text.setText(((ShowEntry)obj).getText());
					else
						text.setText(homeScreenText);
				}
				catch(Exception _e)
				{
					text.setText(homeScreenText);
				}
			}
		};
		tree.addTreeSelectionListener(tsl);
		tree.setModel(new DefaultTreeModel(root));
	}

	private class UpcomingEpisode
	{
		Episode episode;
		ShowEntry show;
		
		UpcomingEpisode(Episode e, ShowEntry se)
		{
			episode = e;
			show = se;
		}
		
		public String toString()
		{
			return episode.timeDifference()+" - "+show.showName+episode.toString();
		}
	}
}