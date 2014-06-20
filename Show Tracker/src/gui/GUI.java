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
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		verticalBox.setBackground(Color.BLACK);
		frmShowTracker.getContentPane().add(verticalBox, BorderLayout.WEST);
		
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
		verticalBox.add(btnPrintUnseen);
		
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
		verticalBox.add(btnManageShows);
		
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
		verticalBox.add(btnBrowse);
		
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
		verticalBox.add(btnPrintUpcoming);
		
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
		verticalBox.add(btnPrintTimelines);
		
		JButton btnUpdateShows = new JButton("Update Shows");
		btnUpdateShows.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnUpdateShows.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						main.updateShows();
					}
				}.start();
			}
		});
		verticalBox.add(btnUpdateShows);
	}

}
