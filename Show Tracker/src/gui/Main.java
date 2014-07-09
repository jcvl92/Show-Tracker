package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

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
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import showTracker.Episode;
import showTracker.Season;
import showTracker.Show;
import showTracker.ShowTracker;

//TODO: when adding, disable all the buttons like in update all(side panel buttons and in panel buttons)
public class Main
{
	public static boolean DL_ON = false;
	boolean unseenVal = true;
	Thread paneler = null;
	JPanel panel;
	JFrame frame;
	static JButton[] navButtons;
	ShowTracker m = new ShowTracker();
	ReentrantLock rl = new ReentrantLock();
	static HashMap<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();

	public Main(JPanel p, JFrame f)
	{
		panel = p;
		frame = f;
	}
	
	public void setButtons(JButton ... buttons)
	{
		navButtons = buttons;
	}
	
	private static void enableButtons(boolean b)
	{
		for(JButton button: navButtons)
		{
			button.setEnabled(b);
		}
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
							destinationWidth = getWidth(),
							destinationHeight = getHeight();

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
			data[i] = new Object[]{episodes.get(i).show.toString(), episodes.get(i).getSENumber(), episodes.get(i).title(), episodes.get(i).getDate(), true};

		//make a table from the data(overloading the table model to make checkboxes work)
		final JTable jt = new JTable(new DefaultTableModel(data, new String[]{"Show Name", "Episode Number", "Episode Title", "Date Aired", "Download"})
		{
			private static final long serialVersionUID = 1L;
			public Class<?> getColumnClass(int columnIndex)
			{
				if(columnIndex == 4)
					return Boolean.class;
				else
					return String.class;
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
				if(rl.isLocked())
					return false;
				
				//only allow editing of the checkbox cells
				if(column == 4)//((Vector<?>)dataVector.get(row)).get(column).getClass().equals(Boolean.class))
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
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
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
							try
							{
								rl.lock();
							
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
								popIn.add(new JLabel(new ImageIcon(this.getClass().getResource("loading spinner.gif")), SwingConstants.CENTER));
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
												episodes.remove(jt.getSelectedRow());
												((DefaultTableModel)jt.getModel()).removeRow(jt.getSelectedRow());
												episode.setWatched(!episode.isWatched());
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
												try
												{
													rl.lock();
													btnDownload.setEnabled(false);
													if(episode.download())
													{
														episodes.remove(jt.getSelectedRow());
														((DefaultTableModel)jt.getModel()).removeRow(jt.getSelectedRow());
														episode.setWatched(!episode.isWatched());
													}
													else
													{
														btnDownload.setText("Unavailable");
														if(!((String)jt.getValueAt(jt.getSelectedRow(), jt.getColumn("Date Aired").getModelIndex())).contains(" - unavailable"))
															jt.setValueAt(jt.getValueAt(jt.getSelectedRow(), jt.getColumn("Date Aired").getModelIndex())+" - unavailable", jt.getSelectedRow(), jt.getColumn("Date Aired").getModelIndex());
													}
												}
												finally
												{
													rl.unlock();
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
										if(image.getImage() != null)
										{
											int destinationWidth = getWidth(),
													destinationHeight = (int)(image.getIconHeight()/((double)image.getIconWidth()/(double)destinationWidth));
	
											setPreferredSize(new Dimension(destinationWidth, destinationHeight));
											g.drawImage(getScaledInstance(image, destinationWidth, destinationHeight), 0, 0, null);
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
							finally
							{
								rl.unlock();
							}
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

		//add the download all button
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
						try
						{
							rl.lock();
							Main.enableButtons(false);
							jt.clearSelection();
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
							Main.enableButtons(true);
						}
						finally
						{
							rl.unlock();
						}
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

		//map of shows to update buttons for the update all button to manipulate
		final HashMap<Show, JButton> uData = new HashMap<Show, JButton>();
		final ArrayList<JButton> deleteButtons = new ArrayList<JButton>();
		
		//create the add show components for referencing
		final JButton btnAdd = new JButton("Add");
		final JTextPane addName = new JTextPane();
		
		//create the header with the update all button
		JPanel header = new JPanel(new BorderLayout());
		JTextArea headerText = new JTextArea("Your Shows:");
		headerText.setFont(new Font("Times New Roman", Font.BOLD, 20));
		header.add(headerText);
		final JButton btnUpdateAll = new JButton("Update All");
		btnUpdateAll.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						Main.enableButtons(false);
						btnUpdateAll.setEnabled(false);
						btnUpdateAll.setText("Updating All");
						synchronized(m)
						{
							//disable the delete buttons
							for(JButton delete: deleteButtons)
							{
								delete.setEnabled(false);
							}
							
							//disable the update buttons
							for(Entry<Show, JButton> update: uData.entrySet())
							{
								update.getValue().setEnabled(false);
							}
							
							//disable the add show button and text field
							addName.setEnabled(false);
							btnAdd.setEnabled(false);
							
							for(int i=0; i<ShowTracker.shows.size(); ++i)
							{
								Show show = ShowTracker.shows.get(i);
								JButton button = uData.get(show);
								try
								{
									synchronized(m)
									{
										Color c = button.getBackground();
										button.setBackground(Color.LIGHT_GRAY);
										button.setText("Updating");
										show.update();
										button.setBackground(c);
										button.setText("Updated");
									}
								}
								catch(Exception e)
								{
									button.setText("Update failed");
								}
							}
							
							//reenable the delete buttons and add show components
							for(JButton delete: deleteButtons)
							{
								delete.setEnabled(true);
							}
							addName.setEnabled(true);
							btnAdd.setEnabled(true);
							
							btnUpdateAll.setText("Updated All");
						}
						Main.enableButtons(true);
					}
				}.start();
			}
		});
		header.add(btnUpdateAll, BorderLayout.LINE_END);
		panel.add(header, BorderLayout.PAGE_START);

		//create a list of shows(each with a delete and update button)
		final Box showBox = Box.createVerticalBox();
		for(int i=0; i<ShowTracker.shows.size(); ++i)
		{
			//create the panel and set up the layout
			final JPanel showPanel = new JPanel();
			showPanel.setBackground(Color.WHITE);
			showPanel.setBorder(new LineBorder(Color.BLACK));
			showPanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;

			//create the show name text area
			final Show show = ShowTracker.shows.get(i);
			JTextArea showName = new JTextArea(show.toString());
			showName.setFont(new Font("Times New Roman", Font.PLAIN, 20));
			showName.setEditable(false);

			//create a box containing the show name and search text editor
			Box showText = Box.createVerticalBox();

			//add the prompt
			JTextArea searchTextEditPrompt = new JTextArea("Search String:");
			searchTextEditPrompt.setEditable(false);
			showText.add(searchTextEditPrompt);

			//create the editable text
			final JTextArea searchTextEdit = new JTextArea(show.getSearchText());
			searchTextEdit.setBorder(new LineBorder(Color.BLACK));
			searchTextEdit.setBackground(Color.LIGHT_GRAY);
			showText.add(searchTextEdit);

			//create the button to save the text
			final JButton btnSearchTextSave = new JButton("Save");
			btnSearchTextSave.setPreferredSize(new Dimension(showText.getPreferredSize().width, btnSearchTextSave.getPreferredSize().height));
			btnSearchTextSave.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					new Thread()
					{
						public void run()
						{
							btnSearchTextSave.setEnabled(false);
							btnSearchTextSave.setText("Saving");
							synchronized(m)
							{
								show.setSearchText(searchTextEdit.getText());
							}
							btnSearchTextSave.setText("Saved");
						}
					}.start();
				}
			});
			JPanel searchTextSaveStretcher = new JPanel(new GridBagLayout());
			searchTextSaveStretcher.add(btnSearchTextSave);
			showText.add(searchTextSaveStretcher);

			//create the box for the buttons
			Box buttonBox = Box.createVerticalBox();

			//delete button
			final JButton btnDelete = new JButton("Delete");
			deleteButtons.add(btnDelete);
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
								ShowTracker.removeShow(showNum);

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
									Color c = btnUpdate.getBackground();
									btnUpdate.setBackground(Color.LIGHT_GRAY);
									btnUpdate.setText("Updating");
									show.update();
									btnUpdate.setBackground(c);
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

			//add the update button to update data
			uData.put(show, btnUpdate);

			//add the buttons to the button box
			buttonBox.add(btnDelete);
			buttonBox.add(btnUpdate);

			//add content to panel
			showPanel.add(showName, gbc);
			showPanel.add(new JPanel()
			{
				private static final long serialVersionUID = 1L;
				private ImageIcon image = show.getImage();
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if(image != null)
					{
						int destinationHeight = 75,
								destinationWidth = (int)(image.getIconWidth()/((double)image.getIconHeight()/(double)destinationHeight));

						setPreferredSize(new Dimension(destinationWidth, destinationHeight));
						g.drawImage(getScaledInstance(image, destinationWidth, destinationHeight), 0, 0, null);
						g.dispose();
						showPanel.revalidate();
					}
					else
						setVisible(false);
				}
			}, gbc);
			showPanel.add(showText, gbc);
			showPanel.add(buttonBox, gbc);
			showBox.add(showPanel);
		}
		panel.add(new JScrollPane(showBox), BorderLayout.CENTER);

		//create an "add" button at the bottom of the list
		final Box addBox = Box.createHorizontalBox();
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
		});
		btnAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
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
		});
		addBox.add(addName);
		addBox.add(btnAdd);

		panel.add(addBox, BorderLayout.PAGE_END);
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
						
						popIn.setVisible(false);
						
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
							popIn.add(new JLabel(new ImageIcon(this.getClass().getResource("loading spinner.gif")), SwingConstants.CENTER));
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
									if(image.getImage() != null)
									{
										int destinationWidth = getWidth(),
												destinationHeight = (int)(image.getIconHeight()/((double)image.getIconWidth()/(double)destinationWidth));

										setPreferredSize(new Dimension(destinationWidth, destinationHeight));
										g.drawImage(getScaledInstance(image, destinationWidth, destinationHeight), 0, 0, null);
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

	public void timeline()
	{
		panel.removeAll();

		//create a drawing panel
		panel.add(new TimelinePanel(), BorderLayout.CENTER);

		panel.revalidate();
	}

	private void addShow(final JPanel pane, String showName)
	{
		//set up the contents of the popup
		final JPanel contents = new JPanel(new BorderLayout());
		contents.setBackground(Color.WHITE);
		
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
						contents.add(new JLabel(new ImageIcon(this.getClass().getResource("loading spinner.gif")), SwingConstants.CENTER));
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

	private void selectSeen(final JPanel pane, final Show show)
	{
		Box contents = Box.createVerticalBox();

		//create the JPanel for the pop-in
		final JPanel popIn = new JPanel(new BorderLayout());
		popIn.setVisible(false);

		//create the prompt
		JTextArea jta = new JTextArea("Have you seen any episodes of "+show+"?");
		jta.setFont(jta.getFont().deriveFont(Font.BOLD, 20));
		contents.add(jta);

		//create the buttons
		Box buttonBox = Box.createHorizontalBox();
		JButton yes = new JButton("Yes");
		yes.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						//get the table data
						final ArrayList<Episode> episodes = show.getAiredEpisodes();
						final Object[][] data = new Object[episodes.size()][5];
						for(int i=0; i<episodes.size(); ++i)
							data[i] = new Object[]{episodes.get(i).getSENumber(), episodes.get(i).title(), episodes.get(i).getDate()};

						//make a table from the data
						final JTable jt = new JTable(data, new String[]{"Episode Number", "Episode Title", "Date Aired"})
						{
							private static final long serialVersionUID = 1L;
							public boolean isCellEditable(int row, int column)
							{
								return false;
							}
						};
						//disable reordering
						jt.getTableHeader().setReorderingAllowed(false);
						//disable multiple selection
						jt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						//center text strings
						DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
						centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
						jt.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
						jt.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
						jt.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
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
											//quit another paneling operation if one exists
											if(paneler != null)
												paneler.stop();
											paneler = this;

											//show the loading panel
											popIn.removeAll();
											popIn.add(new JLabel(new ImageIcon(this.getClass().getResource("loading spinner.gif")), SwingConstants.CENTER));
											popIn.setVisible(true);
											popIn.revalidate();

											//create the text section
											final Episode episode = episodes.get(jt.getSelectedRow());
											JTextArea jta = new JTextArea(episode.getText());
											jta.setEditable(false);
											jta.setLineWrap(true);
											jta.setWrapStyleWord(true);

											//add the components
											popIn.removeAll();
											popIn.add(new JPanel()
											{
												private static final long serialVersionUID = 1L;
												private ImageIcon image = episode.getImage();
												protected void paintComponent(Graphics g) {
													super.paintComponent(g);
													if(image.getImage() != null)
													{
														int destinationWidth = new ImageIcon(this.getClass().getResource("loading spinner.gif")).getIconWidth(),
																destinationHeight = (int)(image.getIconHeight()/((double)image.getIconWidth()/(double)destinationWidth));

														setPreferredSize(new Dimension(destinationWidth, destinationHeight));
														g.drawImage(getScaledInstance(image, destinationWidth, destinationHeight), 0, 0, null);
														g.dispose();
														popIn.revalidate();
													}
													else
														setVisible(false);
												}
											}, BorderLayout.PAGE_START);
											popIn.add(new JScrollPane(jta), BorderLayout.CENTER);

											//revalidate to redraw/realign the panel
											popIn.revalidate();
										}
									}.start();
								}
							}
						});

						//create the select button
						JButton btnSelect = new JButton("Select Episode");
						btnSelect.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								for(int i=jt.getSelectedRow(); i<episodes.size(); ++i)
									episodes.get(i).setWatched(true);
								new Thread()
								{
									public void run()
									{
										ShowTracker.addShow(show);
									}
								}.start();
								manageShows();
							}
						});

						//add the contents to the pane
						pane.removeAll();
						pane.add(new JTextArea("Select the last episode you have seen:"), BorderLayout.PAGE_START);
						pane.add(new JScrollPane(jt), BorderLayout.CENTER);
						pane.add(popIn, BorderLayout.LINE_END);
						pane.add(btnSelect, BorderLayout.PAGE_END);
						pane.revalidate();
					}
				}.start();
			}
		});
		buttonBox.add(yes);
		JButton no = new JButton("No");
		no.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ShowTracker.addShow(show);
				manageShows();
			}
		});
		buttonBox.add(no);
		contents.add(buttonBox);

		//set up the centered panel
		JPanel addPanel = new JPanel();
		addPanel.setBackground(Color.WHITE);
		addPanel.setBorder(new LineBorder(Color.BLACK));
		addPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		addPanel.add(contents);

		//add it to the pane
		pane.removeAll();
		pane.add(addPanel);
		pane.revalidate();
	}

	public static BufferedImage getScaledInstance(ImageIcon image,
			int targetWidth,
			int targetHeight)
	{
		if(imageCache.containsKey(image.toString()+'|'+targetWidth+'|'+targetHeight))
			return imageCache.get(image.toString()+'|'+targetWidth+'|'+targetHeight);
		
		BufferedImage img = new BufferedImage(
				image.getIconWidth(),
				image.getIconHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics g1 = img.createGraphics();
		image.paintIcon(null, g1, 0,0);
		g1.dispose();
		
		BufferedImage ret = img;
		int w = img.getWidth(), h = img.getHeight();
		
		do
		{
			if(w != targetWidth)
			{
				w /= 2;
				if (w < targetWidth)
				{
					w = targetWidth;
				}
			}
			
			if(h != targetHeight)
			{
				h /= 2;
				if (h < targetHeight)
				{
					h = targetHeight;
				}
			}
			
			BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();
			
			ret = tmp;
		} while(w != targetWidth || h != targetHeight);
		
		imageCache.put(image.toString()+'|'+targetWidth+'|'+targetHeight, ret);
		
		return ret;
	}
}