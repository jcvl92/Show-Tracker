package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.OutputStream;
import java.io.PrintStream;

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
		//TODO: make this more fluid like it is in the poster program
		//clear the panel
		panel.removeAll();
		
		//create the pane and size it
		final JTextArea jta = new JTextArea();
		System.setOut(new PrintStream(new OutputStream(){public void write(int n){jta.setText(jta.getText()+(char)n);}}));
		m.updateShows();
		JScrollPane jsp = new JScrollPane(jta);
		jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().width+15, jsp.getPreferredSize().height>500 ? 500 : jsp.getPreferredSize().height));
		
		//set the content of the panel
		panel.add(jsp, BorderLayout.CENTER);
		frame.pack();
	}

	public void printUnseen()
	{
		//clear the panel
		panel.removeAll();
		
		//create the pane and size it
		final JTextArea jta = new JTextArea();
		System.setOut(new PrintStream(new OutputStream(){public void write(int n){jta.setText(jta.getText()+(char)n);}}));
		m.showLinks();
		JScrollPane jsp = new JScrollPane(jta);
		jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().width+15, jsp.getPreferredSize().height>500 ? 500 : jsp.getPreferredSize().height));
		
		//set the content of the panel
		panel.add(jsp, BorderLayout.CENTER);
		frame.pack();
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
		//clear the panel
		panel.removeAll();
		
		//create the pane and size it
		JScrollPane jsp = new JScrollPane(new JTextArea(m.upcomingEpisodes()));
		jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().width+15, jsp.getPreferredSize().height>500 ? 500 : jsp.getPreferredSize().height));
		
		//set the content of the panel
		panel.add(jsp, BorderLayout.CENTER);
		frame.pack();
	}

	public void printTimelines()
	{
		//clear the panel
		panel.removeAll();
		
		//create the pane and size it
		JScrollPane jsp = new JScrollPane(new JTextArea(m.timeline()));
		jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().width+15, jsp.getPreferredSize().height>500 ? 500 : jsp.getPreferredSize().height));
		
		//set the content of the panel
		panel.add(jsp, BorderLayout.CENTER);
		frame.pack();
	}
}
