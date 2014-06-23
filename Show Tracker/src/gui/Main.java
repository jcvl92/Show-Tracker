package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import showTracker.*;

//TODO: add update all and delete all buttons to the manage shows section
//TODO: add offline support
//TODO: add the option to edit the download search text(have an edit button in the manage shows section)
//TODO: lock down panels where appropiate(like downloading and adding)
//TODO: adding a show needs to ask about the episodes that have been seen? or just mark all as seen that are before now
//TODO: add show pictures and desicriptions to the manage shows list
public class Main
{
	public static boolean DL_ON = false;
	boolean unseenVal = true;
	Thread paneler = null;
	JPanel panel;
	JFrame frame;
	ShowTracker m = new ShowTracker();
	
	public Main(JPanel p, JFrame f)
	{
		panel = p;
		frame = f;
	}
	
	public void splash()
	{
		//create the splash screen
		try
		{
			panel.add(new JPanel()
			{
				private static final long serialVersionUID = 1L;
				private Image image = ImageIO.read(this.getClass().getResource("splash screen.png"));
				protected void paintComponent(Graphics g) {
					int sourceWidth = image.getWidth(null),
			        	sourceHeight = image.getHeight(null),
			        	destinationWidth = this.getWidth(),
			        	destinationHeight = this.getHeight();
			        
					super.paintComponent(g);
			        g.drawImage(image, 0, 0, destinationWidth, destinationHeight, 0, 0, sourceWidth, sourceHeight, null);
			        g.dispose();
			    }
			}, BorderLayout.CENTER);
		}
		catch(IOException e){}
	}
	
	public void unseenShows()
	{
		//clear the panel
		panel.removeAll();
		
		//create the JPanel for the pop-in
		final JPanel popIn = new JPanel(new BorderLayout());
		popIn.setVisible(false);
		
		//reset the unseenVal so that select/deselect is consistent
		unseenVal = true;
		
		//get the table data
		final ArrayList<Episode> episodes = m.getUnseenEpisodes();
		final Object[][] data = new Object[episodes.size()][5];
		for(int i=0; i<episodes.size(); ++i)
			data[i] = new Object[]{episodes.get(i).show.toString(), episodes.get(i).getEpisodeNumber(), episodes.get(i).title(), episodes.get(i).getDate(), true};
		
		//make a table from the data(overloading the table model to make checkboxes work)
		final JTable jt = new JTable(new DefaultTableModel(data, new String[]{"Show Name", "Episode Number", "Episode Title", "Date Aired", "Download"})
		{
			private static final long serialVersionUID = 1L;
			public Class<?> getColumnClass(int columnIndex)
			{
				return getValueAt(0, columnIndex).getClass();
			}
			public int getColumnCount()
			{
				return columnIdentifiers.size();
			}
			public int getRowCount()
			{
				return dataVector.size();
			}
			public Object getValueAt(int row, int column)
			{
				try
				{
					return ((Vector<?>)dataVector.get(row)).get(column);
				}
				catch(Exception e)
				{
					return -1;
				}
			}
			public boolean isCellEditable(int row, int column)
			{
				//only allow editing of the checkbox cells
				if(((Vector<?>)dataVector.get(row)).get(column).getClass().equals(Boolean.class))
					return true;
				return false;
			}
		});
		//disable reordering
		jt.getTableHeader().setReorderingAllowed(false);
		//disable multiple selection
		jt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//center text strings
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		jt.setDefaultRenderer(String.class, centerRenderer);
		//create a listener to construct a panel with episode information on row select
		jt.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(final ListSelectionEvent event)
			{
				if(!event.getValueIsAdjusting())
				{
					new Thread()
					{
						@SuppressWarnings("deprecation")
						public void run()
						{
							//if this was a remove operation, just remove the panel and return
							if(jt.getSelectedRow() == -1)
							{
								popIn.removeAll();
								popIn.setVisible(false);
								return;
							}
							
							//quit another paneling operation if one exists
							if(paneler != null)
								paneler.stop();
							paneler = this;
							
							//show the loading panel
							popIn.removeAll();
							popIn.add(new JLabel(new ImageIcon(this.getClass().getResource("loading spinner.gif")), JLabel.CENTER));
							popIn.setVisible(true);
							popIn.revalidate();
							
							//create the text section
							final Episode episode = episodes.get(jt.getSelectedRow());
							JTextArea jta = new JTextArea(episode.getText());
							jta.setEditable(false);
							jta.setLineWrap(true);
							jta.setWrapStyleWord(true);
							
							//create buttons
							JPanel buttonBox = new JPanel();
							buttonBox.setLayout(new GridBagLayout());
							
							//last watched button
							final JButton btnLastWatched = new JButton("Set as "+(episode.isWatched() ? "unwatched" : "watched"));
							btnLastWatched.addActionListener(new ActionListener()
							{
								public void actionPerformed(ActionEvent e)
								{
									new Thread()
									{
										public void run()
										{
											btnLastWatched.setEnabled(false);
											episode.setWatched(!episode.isWatched());
											ShowTracker.writeShowsToFile();
											((DefaultTableModel)jt.getModel()).removeRow(jt.getSelectedRow());
										}
									}.start();
								}
							});
							GridBagConstraints gbc_btnLastWatched = new GridBagConstraints();
							gbc_btnLastWatched.fill = GridBagConstraints.BOTH;
							gbc_btnLastWatched.gridy = 0;
							buttonBox.add(btnLastWatched, gbc_btnLastWatched);
							
							//download button
							final JButton btnDownload = new JButton("Download episode");
							btnDownload.addActionListener(new ActionListener()
							{
								public void actionPerformed(ActionEvent e)
								{
									new Thread()
									{
										public void run()
										{
											btnDownload.setEnabled(false);
											if(episode.download())
											{
												episode.setWatched(!episode.isWatched());
												ShowTracker.writeShowsToFile();
												((DefaultTableModel)jt.getModel()).removeRow(jt.getSelectedRow());
											}
											else
											{
												btnDownload.setText("Unavailable");
												if(!((String)jt.getValueAt(jt.getSelectedRow(), jt.getColumn("Date Aired").getModelIndex())).contains(" - unavailable"))
													jt.setValueAt(jt.getValueAt(jt.getSelectedRow(), jt.getColumn("Date Aired").getModelIndex())+" - unavailable", jt.getSelectedRow(), jt.getColumn("Date Aired").getModelIndex());
											}
										}
									}.start();
								}
							});
							if(((String)jt.getValueAt(jt.getSelectedRow(), jt.getColumn("Date Aired").getModelIndex())).contains(" - unavailable"))
							{
								btnDownload.setEnabled(false);
								btnDownload.setText("Unavailable");
							}
							GridBagConstraints gbc_btnDownload = new GridBagConstraints();
							gbc_btnDownload.fill = GridBagConstraints.BOTH;
							gbc_btnDownload.gridy = 1;
							buttonBox.add(btnDownload, gbc_btnDownload);
							
							//add the components
							popIn.removeAll();
							popIn.add(new JPanel()
							{
								private static final long serialVersionUID = 1L;
								private ImageIcon image = episode.getImage();
								protected void paintComponent(Graphics g) {
									super.paintComponent(g);
									if(image != null)
									{
										int sourceWidth = image.getIconWidth(),
								        	sourceHeight = image.getIconHeight(),
								        	destinationWidth = this.getWidth(),
								        	destinationHeight = (int)((double)sourceHeight/((double)sourceWidth/(double)destinationWidth));
								        
										this.setPreferredSize(new Dimension(destinationWidth, destinationHeight));
								        g.drawImage(image.getImage(), 0, 0, destinationWidth, destinationHeight, 0, 0, sourceWidth, sourceHeight, null);
								        g.dispose();
								        popIn.revalidate();
									}
									else
										setVisible(false);
							    }
							}, BorderLayout.PAGE_START);
							popIn.add(new JScrollPane(jta), BorderLayout.CENTER);
							popIn.add(buttonBox, BorderLayout.PAGE_END);
							
							//revalidate to redraw/realign the panel
							popIn.revalidate();
						}
					}.start();
				}
			}
		});
		
		//add the table to the panel
		panel.add(new JScrollPane(jt), BorderLayout.CENTER);
		
		//set up the buttons panel
		JPanel buttonPanel = new JPanel();
		FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
		fl.setHgap(0);fl.setVgap(0);
		buttonPanel.setLayout(fl);
		
		//add the download button
		final JButton btnDownload = new JButton("Download Selected");
		final JButton btnSelect = new JButton("Select/Deselect All");
		btnDownload.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						btnDownload.setText("Downloading Selected");
						btnDownload.setEnabled(false);
						btnSelect.setEnabled(false);
						for(int i=0; i<jt.getRowCount();)
						{
							if((boolean)jt.getValueAt(i, jt.getColumn("Download").getModelIndex()))
							{
								if(episodes.get(i).download())
								{
									episodes.get(i).setWatched(true);
									episodes.remove(i);
									((DefaultTableModel)jt.getModel()).removeRow(i);
								}
								else
								{
									if(!((String)jt.getValueAt(i, jt.getColumn("Date Aired").getModelIndex())).contains(" - unavailable"))
										jt.setValueAt(jt.getValueAt(i, jt.getColumn("Date Aired").getModelIndex())+" - unavailable", i, jt.getColumn("Date Aired").getModelIndex());
									++i;
								}
							}
							else
								++i;
						}
						btnDownload.setText("Download Selected");
						btnDownload.setEnabled(true);
						btnSelect.setEnabled(true);
					}
				}.start();
			}
		});
		buttonPanel.add(btnDownload);
		
		//add the select/deselect button
		btnSelect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						unseenVal = !unseenVal;
						for(int i=0; i<jt.getRowCount(); ++i)
							jt.setValueAt(unseenVal, i, jt.getColumn("Download").getModelIndex());
					}
				}.start();
			}
		});
		buttonPanel.add(btnSelect);
		
		//add the buttons panel to the panel
		panel.add(buttonPanel, BorderLayout.PAGE_END);
		
		//add the pop-in panel to the panel
		panel.add(popIn, BorderLayout.LINE_END);
		
		//revalidate the panel to align the components
		panel.revalidate();
	}

	public void manageShows()
	{
		//clear the panel
		panel.removeAll();
		
		//create a list of shows(each with a delete and update button)
		final Box showBox = Box.createVerticalBox();
		for(int i=0; i<ShowTracker.shows.size(); ++i)
		{
			//create the panel and set up the layout
			JPanel showPanel = new JPanel();
			showPanel.setBorder(new LineBorder(Color.BLACK));
			showPanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			
			//create the show description text area
			final Show show = ShowTracker.shows.get(i);
			JTextArea jta = new JTextArea(show.toString());
			jta.setEditable(false);
			showPanel.add(jta, gbc);
			
			//create the box for the buttons
			Box buttonBox = Box.createVerticalBox();
			
			//delete button
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
							synchronized(m)
							{
								ShowTracker.removeShowFromFile(showNum);
								
								//return to the manage function
								manageShows();
							}
						}
					}.start();
				}
			});
			
			//update button
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
								synchronized(m)
								{
									btnUpdate.setText("Updating");
									show.update();
									ShowTracker.writeShowsToFile();
									btnUpdate.setText("Updated");
								}
							}
							catch(Exception e)
							{
								btnUpdate.setText("Update failed");
							}
						}
					}.start();
				}
			});
			
			//add the buttons to the button box
			buttonBox.add(btnDelete);
			buttonBox.add(btnUpdate);
			
			//center content within panel
			showPanel.add(buttonBox, gbc);
			
			showBox.add(showPanel);
		}
		panel.add(new JScrollPane(showBox), BorderLayout.CENTER);
		
		//create an "add" button at the bottom of the list
		final Box addBox = Box.createHorizontalBox();
		final JButton btnAdd = new JButton("Add");
		final JTextPane addName = new JTextPane();
		addName.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent ke)
			{
				if(ke.getKeyCode() == KeyEvent.VK_ENTER)
				{
					addName.setEnabled(false);
					btnAdd.setText("Searching");
					btnAdd.setEnabled(false);
					new Thread()
					{
						public void run()
						{
							addShow(panel, addName.getText());
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
						addShow(panel, addName.getText());
					}
				}.start();
			}
		});
		addBox.add(addName);
		addBox.add(btnAdd);
		
		panel.add(addBox, BorderLayout.PAGE_END);
		panel.repaint();//repaint because you don't use the whole space and you don't want residual drawing there
		panel.revalidate();
		
		//put the focus in the add show field
		addName.requestFocus();
	}

	public void browse()
	{
		//clear the panel
		panel.removeAll();
		
		//create the tree object
		final JTree tree = new JTree();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("My Shows");
		
		//add each show to the root node
		for(int i=0; i<ShowTracker.shows.size(); ++i)
		{
			Show se = ShowTracker.shows.get(i);
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
		final JPanel popIn = new JPanel(new BorderLayout());
		
		//build the listener for the node selection event and set the tree
		TreeSelectionListener tsl = new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				new Thread()
				{
					@SuppressWarnings("deprecation")
					public void run()
					{
						Object obj = ((DefaultMutableTreeNode)tree.getLastSelectedPathComponent()).getUserObject();
						
						if(obj.getClass().equals(Episode.class))
						{
							final Episode episode = (Episode)obj;
							
							if(!episode.getAirDate().isBeforeNow())
							{
								popIn.removeAll();
								popIn.setVisible(false);
								return;
							}
							
							//quit another paneling operation if one exists
							if(paneler != null)
								paneler.stop();
							paneler = this;
							
							//show the loading spinner
							popIn.removeAll();
							popIn.add(new JLabel(new ImageIcon(this.getClass().getResource("loading spinner.gif")), JLabel.CENTER));
							popIn.setVisible(true);
							popIn.revalidate();
							
							//create the text section
							JTextArea jta = new JTextArea(episode.getText());
							jta.setEditable(false);
							jta.setLineWrap(true);
							jta.setWrapStyleWord(true);
							
							//create buttons
							JPanel buttonBox = new JPanel();
							buttonBox.setLayout(new GridBagLayout());
							
							//last watched button
							final JButton btnLastWatched = new JButton("Set as "+(episode.isWatched() ? "unwatched" : "watched"));
							btnLastWatched.addActionListener(new ActionListener()
							{
								public void actionPerformed(ActionEvent e)
								{
									new Thread()
									{
										public void run()
										{
											episode.setWatched(!episode.isWatched());
											btnLastWatched.setText("Set as "+(episode.isWatched() ? "unwatched" : "watched"));
											ShowTracker.writeShowsToFile();
										}
									}.start();
								}
							});
							GridBagConstraints gbc_btnLastWatched = new GridBagConstraints();
							gbc_btnLastWatched.fill = GridBagConstraints.BOTH;
							gbc_btnLastWatched.gridy = 0;
							buttonBox.add(btnLastWatched, gbc_btnLastWatched);
							
							//download button
							final JButton btnDownload = new JButton("Download episode");
							btnDownload.addActionListener(new ActionListener()
							{
								public void actionPerformed(ActionEvent e)
								{
									new Thread()
									{
										public void run()
										{
											btnDownload.setEnabled(false);
											if(episode.download())
											{
												episode.setWatched(!episode.isWatched());
												btnLastWatched.setText("Set as "+(episode.isWatched() ? "unwatched" : "watched"));
												ShowTracker.writeShowsToFile();
											}
											else
											{
												btnDownload.setText("Unavailable");
											}
										}
									}.start();
								}
							});
							GridBagConstraints gbc_btnDownload = new GridBagConstraints();
							gbc_btnDownload.fill = GridBagConstraints.BOTH;
							gbc_btnDownload.gridy = 1;
							buttonBox.add(btnDownload, gbc_btnDownload);
							
							//add the components
							popIn.removeAll();
							popIn.add(new JPanel()
							{
								private static final long serialVersionUID = 1L;
								private ImageIcon image = episode.getImage();
								protected void paintComponent(Graphics g) {
									super.paintComponent(g);
									if(image != null)
									{
										int sourceWidth = image.getIconWidth(),
								        	sourceHeight = image.getIconHeight(),
								        	destinationWidth = this.getWidth(),
								        	destinationHeight = (int)((double)sourceHeight/((double)sourceWidth/(double)destinationWidth));
								        
										this.setPreferredSize(new Dimension(destinationWidth, destinationHeight));
								        g.drawImage(image.getImage(), 0, 0, destinationWidth, destinationHeight, 0, 0, sourceWidth, sourceHeight, null);
								        g.dispose();
								        popIn.revalidate();
									}
									else
										setVisible(false);
							    }
							}, BorderLayout.PAGE_START);
							popIn.add(new JScrollPane(jta), BorderLayout.CENTER);
							popIn.add(buttonBox, BorderLayout.PAGE_END);
						}
						
						//revalidate to redraw/realign the panel
						popIn.revalidate();
					}
				}.start();
			}
		};
		
		//configure and add the tree
		tree.addTreeSelectionListener(tsl);
		tree.setModel(new DefaultTreeModel(root));
		panel.add(new JScrollPane(tree), BorderLayout.CENTER);
		panel.add(popIn, BorderLayout.LINE_END);
		panel.revalidate();
	}
	
	private void addShow(final JPanel pane, String showName)
	{
		//set up the contents of the popup
		final JPanel contents = new JPanel(new BorderLayout());//Box.createVerticalBox();
		
		//add the show
		try
		{
			ArrayList<HashMap<String, String>> entries = Show.search(showName);
			
			//add the search text
			JTextArea search = new JTextArea("Search: \""+showName+"\". Select your show:");
			search.setEditable(false);
			search.setFont(search.getFont().deriveFont(Font.BOLD));
			contents.add(search, BorderLayout.PAGE_START);
			
			//create the box for the entries
			Box entriesBox = Box.createVerticalBox();
			
			//iterate through the entries and add a jpanel for each
			for(int i=0; i< entries.size(); ++i)
			{
				final HashMap<String, String> showEntry = entries.get(i);
				
				Box entryBox = Box.createHorizontalBox();
				entryBox.setBorder(new LineBorder(Color.BLACK));
				
				//show name
				JTextArea showText = new JTextArea(showEntry.get("name"));
				showText.setEditable(false);
				showText.setLineWrap(true);
				showText.setWrapStyleWord(true);
				entryBox.add(showText);
				
				//selection button
				JButton select = new JButton("Select");
				select.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						//create the loading spinner
						contents.removeAll();
						contents.add(new JLabel(new ImageIcon(this.getClass().getResource("loading spinner.gif")), JLabel.CENTER));
						contents.revalidate();
						
						new Thread()
						{
							public void run()
							{
								try
								{
									Show show = Show.getShow(showEntry);
									selectSeen(pane, show);
								}
								catch(Exception e)
								{
									manageShows();
								}
							}
						}.start();
					}
				});
				entryBox.add(select);
				
				//add the entry
				entriesBox.add(entryBox);
			}
			
			//add the entries list to the top of a panel that fills with empty space
			JPanel entriesList = new JPanel(new BorderLayout());
			entriesList.add(entriesBox, BorderLayout.PAGE_START);
			contents.add(new JScrollPane(entriesList));
		}
		catch(Exception e){}
		
		pane.removeAll();
		pane.add(contents);
		pane.revalidate();
	}
	
	private void selectSeen(JPanel pane, final Show show)
	{
		//promt if they have seen any episodes, if not, just return
		//the prompt will be a text area with large font and large buttons below it
		//design: vertical box - top:text area with prompt - bottom:two buttons in a horizontal box
		Box contents = Box.createVerticalBox();
		
		//create the prompt
		JTextArea jta = new JTextArea("Have you seen any episodes of "+show+"?");
		jta.setFont(jta.getFont().deriveFont(Font.BOLD));
		contents.add(jta);
		
		//create the buttons
		Box buttonBox = Box.createHorizontalBox();
		JButton yes = new JButton("Yes");
		yes.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//TODO: finish this
				//draw a table of the episodes using getUnseen
				//the user will then select an entry(the pop-in will show up here)
				//that entry will have everything after it set as unwatched
				ShowTracker.addShowToFile(show);
				manageShows();
			}
		});
		buttonBox.add(yes);
		JButton no = new JButton("No");
		no.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ShowTracker.addShowToFile(show);
				manageShows();
			}
		});
		buttonBox.add(no);
		contents.add(buttonBox);
		
		pane.removeAll();
		pane.add(contents);
		pane.revalidate();
	}
}