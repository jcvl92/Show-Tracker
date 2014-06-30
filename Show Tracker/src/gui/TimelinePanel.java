package gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import showTracker.Episode;
import showTracker.ShowTracker;

//TODO: add an image to the information if one is available
//TODO: add a legend that autoscrolls if there is room for it
public class TimelinePanel extends JPanel implements MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;
	final int PAST_DAYS = 15, FUTURE_DAYS = 15;
	long timelineToday, timelineBegin, timelineEnd;
	long[] points, markers = new long[PAST_DAYS+FUTURE_DAYS+1];
	Episode[] episodes;
	HashMap<String, Integer> showColors = new HashMap<String, Integer>();
	HashMap<Ellipse2D, Integer> circles = new HashMap<Ellipse2D, Integer>();
	ImageIcon spinner = new ImageIcon(this.getClass().getResource("loading spinner.gif"));
	Random colorGenerator = new Random();
	boolean waiting = false;
	int selected = -1;

	public TimelinePanel()
	{
		super();
		
		//create the mouselisteners
		addMouseListener(this);
		addMouseMotionListener(this);
		
		//set the background
		setBackground(Color.WHITE);

		//set up the timeline variables
		timelineToday = System.currentTimeMillis();
		timelineToday = timelineToday - timelineToday%(1000L*60*60*24);
		timelineBegin = timelineToday-(1000L*60*60*24*PAST_DAYS);
		timelineEnd = timelineToday+(1000L*60*60*24*FUTURE_DAYS);

		//set up the day markers
		for(int i=-PAST_DAYS; i<=FUTURE_DAYS; ++i)
			markers[i+PAST_DAYS] = timelineToday+(1000*60*60*24*i);

		//get the episodes to go on the timeline
		episodes = ShowTracker.getTimelineEpisodes(timelineBegin, timelineEnd);
		points = new long[episodes.length];
		for(int i=0; i<episodes.length; ++i)
			points[i] = episodes[i].getAirDate().getMillis() + TimeZone.getDefault().getRawOffset();
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
		
		//set the font size as a function of the width of the episode panel
		g.setFont(new Font("TimesRoman", Font.PLAIN, getWidth()*3/100));
		
		//draw the timeline base line
		g.fillRect(0, lineY, lineWidth, lineThickness);

		//draw the day markers on the line
		for(int i=-PAST_DAYS; i<=FUTURE_DAYS; ++i)
		{
			int X = timeToXValue(markers[i+PAST_DAYS], lineWidth);
			if(markers[i+PAST_DAYS] == timelineToday)
			{
				g.setColor(Color.BLACK);
				g.drawString("Today", X-(g.getFontMetrics().stringWidth("Today")/2), lineY+markHeight*3/4+g.getFontMetrics().getAscent());
				g.setColor(Color.BLUE);
				g.fillRect(X-(markThickness/2), lineY, markThickness*2, markHeight*3/4);
			}
			else if(i%7 == 0)
			{
				String weeks = i/7+" week"+(Math.abs(i/7)>1 ? "s" : "");
				g.setColor(Color.BLACK);
				int x = X-(g.getFontMetrics().stringWidth(weeks)/2);
				if(x < 0)
					x = 0;
				else if(x+g.getFontMetrics().stringWidth(weeks) > getWidth())
					x = getWidth() - g.getFontMetrics().stringWidth(weeks);
				g.drawString(weeks, x, lineY+markHeight*3/4+g.getFontMetrics().getAscent());
				g.setColor(Color.GREEN);
				g.fillRect(X-(markThickness/2), lineY, markThickness*2, markHeight*3/4);
			}
			else
			{
				g.setColor(Color.RED);
				g.fillRect(X-(markThickness/2), lineY, markThickness, markHeight*3/4);
			}
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
		circles = new HashMap<Ellipse2D, Integer>();
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
			
			//create the circle and save it with the associated episode number
			Ellipse2D circle = new Ellipse2D.Double(X-(dotThickness/2), Y-dotThickness-markHeight+lineThickness, dotThickness, dotThickness);
			circles.put(circle, i);
			
			//draw ball
			((Graphics2D)g).fill(circle);
			
			//draw ball outline
			g.setColor(Color.BLACK);
			((Graphics2D)g).draw(circle);
		}
		
		//draw the selected episode box
		if(selected >= 0)
		{
			Episode episode = episodes[selected];
			Ellipse2D selectedCircle = null;
			for(Entry<Ellipse2D, Integer> entry : circles.entrySet())
				if(entry.getValue().equals(selected))
				{
					selectedCircle = entry.getKey();
					break;
				}
			
			//highlight the circle
			g.setColor(Color.BLACK);
			((Graphics2D)g).fill(selectedCircle);
			g.setColor(Color.WHITE);
			((Graphics2D)g).draw(selectedCircle);
			
			if(waiting)
				spinner.paintIcon(this, g, getWidth()/2-spinner.getIconWidth()/2, lineY/2-spinner.getIconHeight()/2);
			else
			{
				//get the preloaded episode information
				String[] text = episode.getText().split(" ");
				String[] title = (episode.show+" - "+episode+':').split(" ");
				
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
				ArrayList<String> titles = new ArrayList<String>();
				for(int j=0; j<title.length; ++j)
				{
					if(titles.size()==0 || 
							g.getFontMetrics().stringWidth(
									titles.get(titles.size()-1) + title[j] + (titles.get(titles.size()-1).length()==0 ? "" : " ")
									) > getWidth()*8/10)
						titles.add(title[j]);
					else
						titles.set(titles.size()-1, titles.get(titles.size()-1)+' '+title[j]);
				}
				
				//highlight the circle
				g.setColor(Color.BLACK);
				((Graphics2D)g).fill(selectedCircle);
				g.setColor(Color.WHITE);
				((Graphics2D)g).draw(selectedCircle);
				
				//draw a box to hold the episode information
				g.setColor(Color.BLACK);
				g.fillRoundRect(getWidth()/20, getHeight()/20, getWidth()*9/10, getHeight()*6/10, 10, 10);
				g.setColor(Color.GREEN);
				g.drawRoundRect(getWidth()/20, getHeight()/20, getWidth()*9/10, getHeight()*6/10, 10, 10);
				
				//draw wrapped episode title
				g.setColor(Color.WHITE);
				for(int j=0; j<titles.size(); ++j)
					g.drawString(titles.get(j), getWidth()*1/10, getHeight()/10+g.getFontMetrics().getAscent()+(g.getFontMetrics().getHeight()*j));
				
				//draw wrapped episode information
				g.setColor(Color.LIGHT_GRAY);
				for(int j=0; j<texts.size(); ++j)
					g.drawString(texts.get(j), getWidth()/10, getHeight()/10+g.getFontMetrics().getAscent()+(g.getFontMetrics().getHeight()*(j+titles.size())));
			}
		}
		else
			g.drawString("Click an episode to view it's information.", (getWidth()-g.getFontMetrics().stringWidth("Click an episode to view it's information."))/2, lineY/2);
	}

	private int timeToXValue(long time, int width)
	{
		int res = (int)(((time-timelineBegin)*width)/(timelineEnd-timelineBegin));
		return res<width ? (res) : width-1;
	}
	
	public void mouseClicked(MouseEvent e)
	{
		//iterate though each mapped circle and check to see if it was clicked
		if(e.getButton() == 1)
			for(final Entry<Ellipse2D, Integer> entry : circles.entrySet())
			    if(entry.getKey().contains(e.getPoint()))
			    {
			    	//show the spinner while waiting to gather the episode information
			    	waiting = true;
			    	selected = entry.getValue();
			    	repaint();
			    	new Thread()
					{
						public void run()
						{
							//gather episode information and stop waiting
							episodes[entry.getValue()].getText();
							waiting = false;
							repaint();
						}
					}.start();
			    	break;
			    }
	}
	
	public void mouseMoved(MouseEvent e)
	{
		boolean setHand = false;
		for(final Entry<Ellipse2D, Integer> entry : circles.entrySet())
		    if(entry.getKey().contains(e.getPoint()) && entry.getValue() != selected)
		    {
		    	setHand = true;
		    	break;
		    }
		if(setHand)
			this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		else
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseDragged(MouseEvent e){}
}
