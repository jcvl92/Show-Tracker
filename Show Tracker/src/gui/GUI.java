package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.Box;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.MatteBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class GUI {

	private JFrame frmShowTracker;
	private Main main;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frmShowTracker.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
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
		buttonPanel.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		frmShowTracker.getContentPane().add(buttonPanel, BorderLayout.WEST);
		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		gbl_buttonPanel.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
		buttonPanel.setLayout(gbl_buttonPanel);
		
		JButton btnPrintUnseen = new JButton("Unseen Episodes");
		btnPrintUnseen.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnPrintUnseen.addActionListener(new ActionListener()
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
		GridBagConstraints gbc_btnPrintUnseen = new GridBagConstraints();
		gbc_btnPrintUnseen.insets = new Insets(0, 0, 5, 5);
		gbc_btnPrintUnseen.gridx = 0;
		gbc_btnPrintUnseen.gridy = 0;
		gbc_btnPrintUnseen.fill = GridBagConstraints.BOTH;
		buttonPanel.add(btnPrintUnseen, gbc_btnPrintUnseen);
		
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
		gbc_btnManageShows.insets = new Insets(0, 0, 5, 5);
		gbc_btnManageShows.gridx = 0;
		gbc_btnManageShows.gridy = 1;
		buttonPanel.add(btnManageShows, gbc_btnManageShows);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnBrowse.addActionListener(new ActionListener()
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
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.fill = GridBagConstraints.BOTH;
		gbc_btnBrowse.insets = new Insets(0, 0, 5, 5);
		gbc_btnBrowse.gridx = 0;
		gbc_btnBrowse.gridy = 2;
		buttonPanel.add(btnBrowse, gbc_btnBrowse);
		
		JButton btnPrintUpcoming = new JButton("Print Upcoming");
		btnPrintUpcoming.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnPrintUpcoming.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						main.printUpcoming();
					}
				}.start();
			}
		});
		GridBagConstraints gbc_btnPrintUpcoming = new GridBagConstraints();
		gbc_btnPrintUpcoming.fill = GridBagConstraints.BOTH;
		gbc_btnPrintUpcoming.insets = new Insets(0, 0, 5, 5);
		gbc_btnPrintUpcoming.gridx = 0;
		gbc_btnPrintUpcoming.gridy = 3;
		buttonPanel.add(btnPrintUpcoming, gbc_btnPrintUpcoming);
		
		JButton btnPrintTimelines = new JButton("Print Timelines");
		btnPrintTimelines.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnPrintTimelines.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						main.printTimelines();
					}
				}.start();
			}
		});
		GridBagConstraints gbc_btnPrintTimelines = new GridBagConstraints();
		gbc_btnPrintTimelines.fill = GridBagConstraints.BOTH;
		gbc_btnPrintTimelines.insets = new Insets(0, 0, 0, 5);
		gbc_btnPrintTimelines.gridx = 0;
		gbc_btnPrintTimelines.gridy = 4;
		buttonPanel.add(btnPrintTimelines, gbc_btnPrintTimelines);
	}

}
