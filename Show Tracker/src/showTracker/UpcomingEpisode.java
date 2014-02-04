package showTracker;

class UpcomingEpisode
{
	Episode episode;
	ShowEntry show;

	UpcomingEpisode(Episode e, ShowEntry se)
	{
		episode = e;
		show = se;
	}

	@Override
	public String toString()
	{
		String time = episode.timeDifference();
		for(int i=time.length(); i<47; ++i)
			time += ' ';

		return time+show.showName+" - "+episode.getEpisodeNumber()+" - "+episode+(show.isSeen(episode) ? " (seen)" : " (unseen)");
	}
}