package com.greyzone.indexsearch.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.greyzone.cache.NzbsOrgRssFeedCache;
import com.greyzone.domain.tv.Episode;
import com.greyzone.domain.tv.Show;
import com.greyzone.indexsearch.IndexSearcher;
import com.greyzone.settings.ApplicationSettings;
import com.greyzone.util.FuzzyStringUtils;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

public abstract class AbstractRssFeedSearcher implements IndexSearcher {

	@Autowired
	private ApplicationSettings settings;

	@Autowired
	private NzbsOrgRssFeedCache cache;
	
	private final Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public List<Episode> getIndexIds(Show show, List<Episode> episodes) {
		List<Episode> result = new ArrayList<Episode>();
		List<SyndEntry> rssList = getRssFeed(show, episodes);

		try {

			for (Episode episode : episodes) {
				
				// Download for this episode might have already been found
				if (StringUtils.isNotBlank(episode.getNzbFileUri()))
					continue;

				boolean foundLastEntry = false;
				for (SyndEntry entry : rssList) {
					String rssTitle = entry.getTitle();
					if (FuzzyStringUtils.fuzzyMatch(show, episode, show.getQuality(),
							show.getFormat(), rssTitle)) {

						// episode.setNzbFile(getNzbFile(entry.getUri()));
						if (entry.getEnclosures().size() > 0) {
						
							episode.setNzbFileUri(((SyndEnclosure)entry.getEnclosures().get(0)).getUrl());
							result.add(episode);
							
							// Only add once
							foundLastEntry = true;
						} else {
							log.warn("Found a suitable episode in RSS-feed but could not extract NZB url. " + entry.toString());
						}
					}

					if (foundLastEntry)
						break;
				}
				
				if (!foundLastEntry) {
					log.debug("Could not find " + episode + " in rss feed search");
				}
			}

			return result;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract String getRssFeedUrl(Show show, List<Episode> episodes);
	
	private List<SyndEntry> getRssFeed(Show show, List<Episode> episodes) {
		
		String feedUrl = getRssFeedUrl(show, episodes);
		if (cache.isInCache(feedUrl)) {
			return cache.getFromCache(feedUrl);
		}

		try {
			List<SyndEntry> rssList = new ArrayList<SyndEntry>();

			URL rss = new URL(feedUrl);

			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input
					.build(new InputStreamReader(rss.openStream()));

			for (Object entryObj : feed.getEntries()) {
				SyndEntry entry = (SyndEntry) entryObj;
				rssList.add(entry);
			}

			cache.put(settings.getNzbsOrgFeed(), rssList);
			return rssList;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unused")
	private byte[] getNzbFile(String uri) throws Exception {

		URL url = new URL(uri);

		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			is = url.openStream();
			byte[] byteChunk = new byte[4096]; // Or whatever size you want to
												// read in at a time.
			int n;

			while ((n = is.read(byteChunk)) > 0) {
				bais.write(byteChunk, 0, n);
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}

		return bais.toByteArray();

	}
}
