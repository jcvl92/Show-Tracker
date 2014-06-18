package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import showTracker.*;

//TODO: merge print unseen and download unseen with a list
//TODO: add an indicator to the current episode in browse(aterisk*)
//TODO: edit the download search text
//TODO: when processing adds, create a transluscent progress wheel(or just add one to the panel)
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
		jta.setEditable(false);
		System.setOut(new PrintStream(new OutputStream(){public void write(int n){jta.setText(jta.getText()+(char)n);}}));
		new Thread()
		{
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
		jta.setEditable(false);
		System.setOut(new PrintStream(new OutputStream(){public void write(int n){jta.setText(jta.getText()+(char)n);}}));
		new Thread()
		{
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
		for(int i=0; i<m.shows.size(); ++i)
		{
			final ShowEntry show = m.shows.get(i);
			JPanel showPanel = new JPanel();
			JTextArea jta = new JTextArea(show.getText());
			jta.setEditable(false);
			showPanel.add(jta);
			Box buttonBox = Box.createVerticalBox();
			final JButton btnDelete = new JButton("Delete");
			final int showNum = i;
			btnDelete.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new Thread()
					{
						public void run()
						{
							m.removeShowFromFile(showNum);
							
							//return to the manage function
							manageShows();
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
							try
							{
								show.update();
								btnUpdate.setText("Updated");
							}
							catch(Exception e)
							{
								btnUpdate.setText("Update failed");
							}
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
		Box addBox = Box.createHorizontalBox();
		final JButton btnAdd = new JButton("Add");
		final JTextPane addName = new JTextPane();
		addName.setPreferredSize(new Dimension(150, addName.getPreferredSize().height));
		addName.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent ke)
			{
				if(ke.getKeyCode() == KeyEvent.VK_ENTER)
				{
					addName.setEnabled(false);
					btnAdd.setText("Adding");
					btnAdd.setEnabled(false);
					new Thread()
					{
						public void run()
						{
							addShow(addName.getText());
							
							//return to the manage function when done
							manageShows();
						}
					}.start();
				}
			}
			public void keyReleased(KeyEvent ke){}
			public void keyTyped(KeyEvent ke){}
		}
		);
		btnAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addName.setEnabled(false);
				btnAdd.setText("Adding");
				btnAdd.setEnabled(false);
				new Thread()
				{
					public void run()
					{
						addShow(addName.getText());
						
						//return to the manage function when done
						manageShows();
					}
				}.start();
			}
		});
		addBox.add(addName);
		addBox.add(btnAdd);
		showBox.add(addBox);
		
		panel.add(showBox);
		panel.repaint();//repaint because you don't use the whole space and you don't want residual drawing there
		panel.revalidate();
		
		//put the focus in the add show field
		addName.requestFocus();
	}

	public void downloadUnseen()
	{
		//clear the panel
		panel.removeAll();
		
		//create the pane and arrange text stream
		final JTextArea jta = new JTextArea();
		jta.setEditable(false);
		System.setOut(new PrintStream(new OutputStream(){public void write(int n){jta.setText(jta.getText()+(char)n);}}));
		new Thread()
		{
			public void run()
			{
				m.openMagnetLinks();
			}
		}.start();
		
		//set the content of the panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(jta), gbc);
		panel.revalidate();
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
					JTextArea jta = new JTextArea(((Season)obj).getText());
					tPanel.add(jta, gbc);
					tPanel.revalidate();
				}
				else if(obj.getClass() == ShowEntry.class)
				{
					//if a show is selected
					tPanel.removeAll();
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.fill = GridBagConstraints.BOTH;
					JTextArea jta = new JTextArea(((ShowEntry)obj).getText());
					tPanel.add(jta, gbc);
					tPanel.revalidate();
				}
				else
				{
					//if the root is selected
					tPanel.removeAll();
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.fill = GridBagConstraints.BOTH;
					JTextArea jta = new JTextArea("Browse your shows.");
					tPanel.add(jta, gbc);
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
		JTextArea jta = new JTextArea(m.upcomingEpisodes());
		JScrollPane jsp = new JScrollPane(jta);
		
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
		JTextArea jta = new JTextArea(m.timeline());
		JScrollPane jsp = new JScrollPane(jta);
		
		//set the content of the panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(jsp, gbc);
		panel.revalidate();
	}

	public void addShow(String showName)
	{
		//TODO: after entering the text, this should come up with candidates for the search(and they pick one)
		//add the show
		try
		{
			ShowEntry show = new ShowEntry(showName);
			m.addShowToFile(show);
			//System.out.println("\""+show.showName+"\" added.");
		}
		catch(Exception e)
		{
			//System.out.println("\""+response+"\" was not added because "+e);
		}
	}
}
