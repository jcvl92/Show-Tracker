package gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

import showTracker.Episode;
import showTracker.ShowTracker;

//this panel shows you shows from 2 weeks ago and 2 weeks ahead
public class TimelinePanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	final int PAST_DAYS = 14, FUTURE_DAYS = 14;
	long timelineNow, timelineBegin, timelineEnd;
	long[] points;
	Episode[] episodes;
	long[] markers = new long[PAST_DAYS+FUTURE_DAYS+1];
	
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
    			markThickness = 1;
        
        //draw the timeline base line 
        g.fillRect(0, lineY, lineWidth, lineThickness);
        
        //draw the dots on the line
        for(int i=0; i<markers.length; ++i)
        {
        	if(markers[i] == timelineNow)
        		g.setColor(Color.BLUE);
        	else
        		g.setColor(Color.RED);
        	g.fillRect(timeToXValue(markers[i], lineWidth)-(markThickness/2), lineY, markThickness, markHeight);
        }
        g.setColor(Color.GREEN);
        for(int i=0; i<points.length; ++i)
        	g.fillOval(timeToXValue(points[i], lineWidth)-(dotThickness/2), lineY-((dotThickness-lineThickness)/2)-1, dotThickness, dotThickness);
	}
	
	private int timeToXValue(long time, int width)
	{
		int res = (int)(((time-timelineBegin)*width)/(timelineEnd-timelineBegin));
		return res<width ? res : width-1;
	}
}