package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JPanel;

import showTracker.Episode;
import showTracker.ShowTracker;

//TODO: change markers to green if the mark is x weeks from now
public class TimelinePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	final int PAST_DAYS = 15, FUTURE_DAYS = 15;
	long timelineNow, timelineBegin, timelineEnd;
	long[] points;
	Episode[] episodes;
	long[] markers = new long[PAST_DAYS+FUTURE_DAYS+1];
	HashMap<String, Integer> showColors = new HashMap<String, Integer>();
	Random colorGenerator = new Random();

	public TimelinePanel()
	{
		super();

		setBackground(Color.LIGHT_GRAY);

		//set up the timeline variables
		timelineNow = System.currentTimeMillis();
		timelineBegin = timelineNow-(1000*60*60*24*PAST_DAYS);
		timelineEnd = timelineNow+(1000*60*60*24*FUTURE_DAYS);

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
			
			//draw ball
			g.fillOval(X-(dotThickness/2), Y-dotThickness-markHeight+lineThickness, dotThickness, dotThickness);
			
			g.setColor(Color.BLACK);
			//draw ball outline
			g.drawOval(X-(dotThickness/2), Y-dotThickness-markHeight+lineThickness, dotThickness, dotThickness);
		}
	}

	private int timeToXValue(long time, int width)
	{
		int res = (int)(((time-timelineBegin)*width)/(timelineEnd-timelineBegin));
		return res<width ? res : width-1;
	}
}