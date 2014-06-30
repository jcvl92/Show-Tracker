package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import showTracker.ShowTracker;

public class GUI {

	private JFrame frmShowTracker;
	private static Main main;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frmShowTracker.setVisible(true);
					main.splash();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public GUI() throws IOException
	{
		initialize();
		
		//set the favicon
		frmShowTracker.setIconImage(ImageIO.read(this.getClass().getResource("favicon.png")));
		
		//change the default close operation
		frmShowTracker.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frmShowTracker.addWindowListener(new java.awt.event.WindowAdapter()
		{
		    public void windowClosing(java.awt.event.WindowEvent windowEvent)
		    {
		    	ShowTracker.writing.lock();
		    	System.exit(0);
		    }
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmShowTracker = new JFrame();
		frmShowTracker.setTitle("Show Tracker");

		//set the window size and location to the middle 50% of the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int)screenSize.getWidth();
		int height = (int)screenSize.getHeight();
		frmShowTracker.setBounds(width/4, height/4, width/2, height/2);

		frmShowTracker.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frmShowTracker.getContentPane().add(panel, BorderLayout.CENTER);
		main = new Main(panel, frmShowTracker);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel buttonPanel = new JPanel();
		frmShowTracker.getContentPane().add(buttonPanel, BorderLayout.WEST);
		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		gbl_buttonPanel.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
		buttonPanel.setLayout(gbl_buttonPanel);

		JButton btnUnseenEpisodes = new JButton("Unseen Episodes");
		btnUnseenEpisodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnUnseenEpisodes.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						main.unseenShows();
					}
				}.start();
			}
		});
		GridBagConstraints gbc_btnUnseenEpisodes = new GridBagConstraints();
		gbc_btnUnseenEpisodes.gridx = 0;
		gbc_btnUnseenEpisodes.gridy = 0;
		gbc_btnUnseenEpisodes.fill = GridBagConstraints.BOTH;
		buttonPanel.add(btnUnseenEpisodes, gbc_btnUnseenEpisodes);

		JButton btnManageShows = new JButton("Manage Shows");
		btnManageShows.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnManageShows.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						main.manageShows();
					}
				}.start();
			}
		});
		GridBagConstraints gbc_btnManageShows = new GridBagConstraints();
		gbc_btnManageShows.fill = GridBagConstraints.BOTH;
		gbc_btnManageShows.gridx = 0;
		gbc_btnManageShows.gridy = 1;
		buttonPanel.add(btnManageShows, gbc_btnManageShows);

		JButton btnBrowseShows = new JButton("Browse Shows");
		btnBrowseShows.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnBrowseShows.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						main.browse();
					}
				}.start();
			}
		});
		GridBagConstraints gbc_btnBrowseShows = new GridBagConstraints();
		gbc_btnBrowseShows.fill = GridBagConstraints.BOTH;
		gbc_btnBrowseShows.gridx = 0;
		gbc_btnBrowseShows.gridy = 2;
		buttonPanel.add(btnBrowseShows, gbc_btnBrowseShows);

		JButton btnEpisodeTimeline = new JButton("Episode Timeline");
		btnEpisodeTimeline.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEpisodeTimeline.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						main.timeline();
					}
				}.start();
			}
		});
		GridBagConstraints gbc_btnEpisodeTimeline = new GridBagConstraints();
		gbc_btnEpisodeTimeline.fill = GridBagConstraints.BOTH;
		gbc_btnEpisodeTimeline.gridx = 0;
		gbc_btnEpisodeTimeline.gridy = 3;
		buttonPanel.add(btnEpisodeTimeline, gbc_btnEpisodeTimeline);
	}

}
