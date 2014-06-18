package gui;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import showTracker.*;

//TODO: set a loading bar in the panel at the beginning of each function(is this needed? most things are streamed)
//TODO: add an indicator to the current episode in browse(aterisk*)
//TODO: set all text areas to uneditable
//TODO: edit the download search text
public class Main
{
	JPanel panel;
	JFrame frame;
	ShowTracker m = new ShowTracker();
	
	public Main(JPanel p, JFrame f)
	{
		panel = p;
		frame = f;
	}
	
	/**
	 * 
	 */
	public void updateShows()
	{
		//clear the panel
		panel.removeAll();
		
		//create the pane and arrand text stream
		final JTextArea jta = new JTextArea();
		System.setOut(new PrintStream(new OutputStream(){public void write(int n){jta.setText(jta.getText()+(char)n);}}));
		new Thread(){
			public void run()
			{
				m.updateShows();
			}
		}.start();
		
		//set the content of the panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(jta), gbc);
		panel.revalidate();
	}

	/**
	 * 
	 */
	public void printUnseen()
	{
		//clear the panel
		panel.removeAll();
		
		//create the pane and arrange text stream
		final JTextArea jta = new JTextArea();
		System.setOut(new PrintStream(new OutputStream(){public void write(int n){jta.setText(jta.getText()+(char)n);}}));
		new Thread(){
			public void run()
			{
				m.showLinks();
			}
		}.start();
		
		//set the content of the panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(jta), gbc);
		panel.revalidate();
	}

	public void manageShows()
	{
		//clear the panel
		panel.removeAll();
		
		//create a list of shows(each with a delete and update button)
		final Box showBox = Box.createVerticalBox();
		for(ShowEntry show : m.shows)
		{
			JPanel showPanel = new JPanel();
			showPanel.add(new JTextArea(show.getText()));
			Box buttonBox = Box.createVerticalBox();
			final JButton btnDelete = new JButton("Delete");
			btnDelete.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new Thread()
					{
						public void run()
						{
							btnDelete.setEnabled(false);
						}
					}.start();
				}
			});
			final JButton btnUpdate = new JButton("Update");
			btnUpdate.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new Thread()
					{
						public void run()
						{
							btnUpdate.setEnabled(false);
						}
					}.start();
				}
			});
			buttonBox.add(btnDelete);
			buttonBox.add(btnUpdate);
			showPanel.add(buttonBox);
			
			showBox.add(showPanel);
		}
		//create an "add" button at the bottom of the list
		final JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						//TODO: after entering the text, this should come up with candidates for the search(and they pick one)
						//create a show add panel
						
						//panel.removeAll();
						//panel.add(showBox);
						//panel.revalidate();
						
						//restore the old panel when that is completed
						panel.removeAll();
						panel.add(showBox);
						panel.revalidate();
					}
				}.start();
			}
		});
		showBox.add(btnAdd);
		
		panel.add(showBox);
		panel.repaint();//repaint because you don't use the whole space and you don't want residual drawing there
		panel.revalidate();
	}

	public void downloadUnseen()
	{
		
	}

	public void browse()
	{
		//clear the panel
		panel.removeAll();
		
		//create the tree object
		final JTree tree = new JTree();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("My Shows");
		
		//add each show to the root node
		for(int i=0; i<m.shows.size(); ++i)
		{
			ShowEntry se = m.shows.get(i);
			DefaultMutableTreeNode show = new DefaultMutableTreeNode(se);
			
			//add each season to the show nodes
			for(int j=0; j<se.seasons.size(); ++j)
			{
				Season s = se.seasons.get(j);
				DefaultMutableTreeNode season = new DefaultMutableTreeNode(s);
				
				//add each episode to the season nodes
				for(int k=0; k<s.episodes.size(); ++k)
					season.add(new DefaultMutableTreeNode(s.episodes.get(k)));
				
				show.add(season);
			}
			
			root.add(show);
		}
		
		//create the panel object
		final JPanel tPanel = new JPanel();
		
		//build the listener for the node selection event and set the tree
		TreeSelectionListener tsl = new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				
				//set the panel according to the Episode, Season, or ShowEntry that was selected
				final Object obj = node.getUserObject();
				if(obj.getClass() == Episode.class)
				{
					//if an episode is selected
					tPanel.removeAll();
					
					//GridBagConstraints gbc = new GridBagConstraints();
					//gbc.fill = GridBagConstraints.BOTH;
					
					//create buttons
					Box buttonBox = Box.createVerticalBox();
					
					//last watched button
					final JButton btnLastWatched = new JButton("Set as last watched");
					btnLastWatched.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							new Thread()
							{
								public void run()
								{
									//TODO: fix these
									//((Episode)obj).show.seasonPos = ((Episode)obj).season.getSeasonNumber();
									//((Episode)obj).show.episodePos = ((Episode)obj).getEpisodeNumber();
									btnLastWatched.setEnabled(false);
								}
							}.start();
						}
					});
					buttonBox.add(btnLastWatched);
					
					//download button
					final JButton btnDownload = new JButton("Download");
					btnDownload.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							new Thread()
							{
								public void run()
								{
									try
									{
										//m.getMagnetLink(((Episode)obj).show, ((Episode)obj)).open();
										btnDownload.setText("Opened");
									}
									catch(Exception e)
									{
										btnDownload.setText("Unavailable");
									}
									finally
									{
										btnDownload.setEnabled(false);
									}
								}
							}.start();
						}
					});
					buttonBox.add(btnDownload);
					
					//TODO: give these both constraints
					//tPanel.add(new JScrollPane(new JTextArea(((Episode)obj).getText())));
					tPanel.add(buttonBox);
					tPanel.revalidate();
				}
				else if(obj.getClass() == Season.class)
				{
					//if a season is selected
					tPanel.removeAll();
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.fill = GridBagConstraints.BOTH;
					tPanel.add(new JTextArea(((Season)obj).getText()), gbc);
					tPanel.revalidate();
				}
				else if(obj.getClass() == ShowEntry.class)
				{
					//if a show is selected
					tPanel.removeAll();
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.fill = GridBagConstraints.BOTH;
					tPanel.add(new JTextArea(((ShowEntry)obj).getText()), gbc);
					tPanel.revalidate();
				}
				else
				{
					//if the root is selected
					tPanel.removeAll();
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.fill = GridBagConstraints.BOTH;
					tPanel.add(new JTextArea("Browse your shows."), gbc);
					tPanel.revalidate();
				}
			}
		};
		
		//configure and add the tree
		tree.addTreeSelectionListener(tsl);
		tree.setModel(new DefaultTreeModel(root));
		
		//TODO: fix this for the gridbaglayout
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(tree, gbc);
		panel.add(tPanel);
		panel.revalidate();
	}

	/**
	 * 
	 */
	public void printUpcoming()
	{
		//clear the panel
		panel.removeAll();
		
		//create the pane
		JScrollPane jsp = new JScrollPane(new JTextArea(m.upcomingEpisodes()));
		
		//set the content of the panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(jsp, gbc);
		panel.revalidate();
	}

	/**
	 * 
	 */
	public void printTimelines()
	{
		//clear the panel
		panel.removeAll();
		
		//create the pane
		JScrollPane jsp = new JScrollPane(new JTextArea(m.timeline()));
		
		//set the content of the panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(jsp, gbc);
		panel.revalidate();
	}
}
