package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import showTracker.ShowTracker;

//TODO: set a loading bar in the panel at the beginning of each function
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
	
	public void updateShows()
	{
		
	}

	public void printUnseen()
	{
		
	}

	public void manageShows()
	{
		
	}

	public void downloadUnseen()
	{
		
	}

	public void browse()
	{
		
	}

	public void printUpcoming()
	{
		
	}

	public void printTimelines()
	{
		//clear the panel
		panel.removeAll();
		
		//create the pane and size it
		JScrollPane jsp = new JScrollPane(new JTextArea(m.timeline()));
		jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().width+15, 500));
		
		//set the content of the panel
		panel.add(jsp, BorderLayout.CENTER);
		frame.pack();
	}
}
