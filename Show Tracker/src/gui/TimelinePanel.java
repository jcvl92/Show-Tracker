package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import showTracker.Episode;
import showTracker.ShowTracker;

//TODO: change markers to green if the mark is x weeks from now
//TODO: change marks to be at midnight
//TODO: draw the episode color legend
//TODO: highlighted selection doesn't move during a window resize redrawing
public class TimelinePanel extends JPanel implements MouseListener
{
	private static final long serialVersionUID = 1L;
	final int PAST_DAYS = 15, FUTURE_DAYS = 15;
	long timelineNow, timelineBegin, timelineEnd;
	long[] points;
	Episode[] episodes;
	long[] markers = new long[PAST_DAYS+FUTURE_DAYS+1];
	HashMap<String, Integer> showColors = new HashMap<String, Integer>();
	HashMap<Ellipse2D, Episode> circles = new HashMap<Ellipse2D, Episode>();
	ImageIcon spinner = new ImageIcon(this.getClass().getResource("loading spinner.gif"));
	Random colorGenerator = new Random();
	boolean waiting = false;
	Ellipse2D selected = null;

	public TimelinePanel()
	{
		super();
		
		//create the mouselistener
		addMouseListener(this);
		
		//set the background
		setBackground(Color.WHITE);

		//set up the timeline variables
		timelineNow = System.currentTimeMillis();
		timelineBegin = timelineNow-(1000L*60*60*24*PAST_DAYS);
		timelineEnd = timelineNow+(1000L*60*60*24*FUTURE_DAYS);

		//set up the day markers
		for(int i=-PAST_DAYS; i<=FUTURE_DAYS; ++i)
		{
			markers[i+PAST_DAYS] = timelineNow+(1000*60*60*24*i);
		}

		//get the episodes to go on the timeline
		episodes = ShowTracker.getTimelineEpisodes(timelineBegin, timelineEnd);
		points = new long[episodes.length];
		for(int i=0; i<episodes.length; ++i)
			points[i] = episodes[i].getAirDate().toDate().getTime();
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		int lineY = getHeight()*4/5,
				lineWidth = getWidth(),
				lineThickness = getHeight()*1/30,
				dotThickness = lineThickness*3/4,
				markHeight = lineThickness*2,
				markThickness = getWidth()/400;

		//draw the timeline base line
		g.fillRect(0, lineY, lineWidth, lineThickness);

		//draw the day markers on the line
		for(int i=0; i<markers.length; ++i)
		{
			if(markers[i] == timelineNow)
				g.setColor(Color.BLUE);
			else
				g.setColor(Color.RED);

			g.fillRect(timeToXValue(markers[i], lineWidth)-(markThickness/2), lineY, markThickness, markHeight*3/4);
		}
		
		//draw the episode lines
		int X=-1, Y=lineY;
		g.setColor(Color.BLACK);
		for(int i=0; i<points.length; ++i)
		{
			int newX = timeToXValue(points[i], lineWidth);
			if(X != -1 && X-newX < dotThickness && X-newX > -dotThickness)
				Y = Y-dotThickness;
			else
				Y = lineY;
			
			X = newX;
			//draw line
			g.fillRect(X-(markThickness/2), Y-(markHeight-lineThickness), markThickness, markHeight-(Y-lineY));
		}
		
		//draw the episode circles
		X=-1;
		for(int i=0; i<points.length; ++i)
		{
			//set the color
			try
			{
				g.setColor(new Color(showColors.get(episodes[i].show.showName())));
			}
			catch(NullPointerException npe)
			{
				showColors.put(episodes[i].show.showName(), colorGenerator.nextInt());
				g.setColor(new Color(showColors.get(episodes[i].show.showName())));
			}
			int newX = timeToXValue(points[i], lineWidth);
			if(X != -1 && X-newX < dotThickness && X-newX > -dotThickness)
				Y = Y-dotThickness;
			else
				Y = lineY;
			X = newX;
			
			//create the circle and save it with the associated episode
			Ellipse2D circle = new Ellipse2D.Double(X-(dotThickness/2), Y-dotThickness-markHeight+lineThickness, dotThickness, dotThickness);
			circles.put(circle, episodes[i]);
			
			//draw ball
			((Graphics2D)g).fill(circle);
			
			//draw ball outline
			g.setColor(Color.BLACK);
			((Graphics2D)g).draw(circle);
		}
			
		if(selected != null)
		{
			Episode episode = circles.get(selected);
			//highlight the circle
			g.setColor(Color.BLACK);
			((Graphics2D)g).fill(selected);
			g.setColor(Color.WHITE);
			((Graphics2D)g).draw(selected);
			
			if(waiting)
				spinner.paintIcon(this, g, getWidth()/2-spinner.getIconWidth()/2, lineY/2-spinner.getIconHeight()/2);
			else
			{
				//get the preloaded episode information
				String[] text = episode.getText().split(" ");
				
				//wrap the text by word
				ArrayList<String> texts = new ArrayList<String>();
				for(int j=0; j<text.length; ++j)
				{
					if(texts.size()==0 || 
							g.getFontMetrics().stringWidth(
									texts.get(texts.size()-1) + text[j] + (texts.get(texts.size()-1).length()==0 ? "" : " ")
									) > getWidth()*8/10)
						texts.add(text[j]);
					else
						texts.set(texts.size()-1, texts.get(texts.size()-1)+' '+text[j]);
				}
				
				//highlight the circle
				g.setColor(Color.BLACK);
				((Graphics2D)g).fill(selected);
				g.setColor(Color.WHITE);
				((Graphics2D)g).draw(selected);
				
				//draw a box to hold the episode information
				g.setColor(Color.BLACK);
				g.fillRoundRect(getWidth()*1/20, getHeight()*1/20, getWidth()*9/10, getHeight()*6/10, 10, 10);
				g.setColor(Color.GREEN);
				g.drawRoundRect(getWidth()*1/20, getHeight()*1/20, getWidth()*9/10, getHeight()*6/10, 10, 10);
				
				//draw episode title
				g.setColor(Color.WHITE);
				g.drawString(episode.show+" - "+episode+':', getWidth()*1/10, getHeight()*1/10+g.getFontMetrics().getHeight());
				
				//draw wrapped episode information
				for(int j=0; j<texts.size(); ++j)
					g.drawString(texts.get(j), getWidth()*1/10, getHeight()*1/10+g.getFontMetrics().getHeight()*2+(g.getFontMetrics().getHeight()*j));
			}
		}
	}

	private int timeToXValue(long time, int width)
	{
		int res = (int)(((time-timelineBegin)*width)/(timelineEnd-timelineBegin));
		return res<width ? (res+1) : width-1;
	}
	
	public void mouseClicked(MouseEvent e)
	{
		//iterate though each mapped circle and check to see if it was clicked
		if(e.getButton() == 1)
			for(final Entry<Ellipse2D, Episode> entry : circles.entrySet())
			    if(entry.getKey().contains(e.getPoint()))
			    {
			    	//show the spinner while waiting to gather the episode information
			    	waiting = true;
			    	selected = entry.getKey();
			    	repaint();
			    	new Thread()
					{
						public void run()
						{
							//gather episode information and stop waiting
							entry.getValue().getText();
							waiting = false;
							repaint();
						}
					}.start();
			    	break;
			    }
	}
	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
}
