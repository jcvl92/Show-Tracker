package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JPanel;

import showTracker.Episode;
import showTracker.ShowTracker;

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
    			dotThickness = lineThickness*2,
    			markHeight = lineThickness*2,
    			markThickness = getWidth()/300;
        
        //draw the timeline base line 
        g.fillRect(0, lineY, lineWidth, lineThickness);
        
        //draw the day markers on the line
        for(int i=0; i<markers.length; ++i)
        {
        	if(markers[i] == timelineNow)
        		g.setColor(Color.BLUE);
        	else
        		g.setColor(Color.RED);
        	
        	g.fillRect(timeToXValue(markers[i], lineWidth)-(markThickness/2), lineY, markThickness, markHeight);
        }
        //draw the episode markers
        for(int i=0; i<points.length; ++i)
        {
        	try
        	{
        		g.setColor(new Color(showColors.get(episodes[i].show.showName())));
        	}
        	catch(NullPointerException npe)
        	{
        		showColors.put(episodes[i].show.showName(), colorGenerator.nextInt());
        		g.setColor(new Color(showColors.get(episodes[i].show.showName())));
        	}
        	g.fillOval(timeToXValue(points[i], lineWidth)-(dotThickness/6), lineY-((dotThickness-lineThickness)/2)-1, dotThickness/3, dotThickness);
        	g.setColor(Color.BLACK);
        	g.drawOval(timeToXValue(points[i], lineWidth)-(dotThickness/6), lineY-((dotThickness-lineThickness)/2)-1, dotThickness/3, dotThickness);
        }
	}
	
	private int timeToXValue(long time, int width)
	{
		int res = (int)(((time-timelineBegin)*width)/(timelineEnd-timelineBegin));
		return res<width ? res : width-1;
	}
}