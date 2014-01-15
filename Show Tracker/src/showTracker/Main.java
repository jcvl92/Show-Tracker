package showTracker;

import java.awt.Font;
import java.util.ArrayList;

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
	String homeScreenText = "Welcome!";
	ArrayList<ShowEntry> myShows;
	
	Main(JTextArea t, JTree tr)
	{
		text = t;
		tree = tr;
		
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
	}
	
	public void run()
	{
		//TODO: last episode aired ___, next episode airing in ____
		//TODO: timeline of episodes
		//enter account information
		
		//load list of shows
		String[] showList = {"rick and morty", "american dad",
				"family guy", "south park", "squidbillies",
				"parks and recreation"};
		myShows = new ArrayList<ShowEntry>();
		for(int i=0; i<showList.length; ++i)
			myShows.add(new ShowEntry(showList[i]));
		
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
		
		//build the listener for the node selection event
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
}