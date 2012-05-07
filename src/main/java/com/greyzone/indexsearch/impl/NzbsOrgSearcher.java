package com.greyzone.indexsearch.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greyzone.domain.tv.Episode;
import com.greyzone.domain.tv.Show;
import com.greyzone.settings.ApplicationSettings;

@Service("NzbsOrgSearcher")
public class NzbsOrgSearcher extends AbstractRssFeedSearcher {

	@Autowired
	private ApplicationSettings settings;

	@Override
	protected String getRssFeedUrl(Show show, List<Episode> episodes) {
		String feedUrl = "http://nzbs.org/api?t=tvsearch" +
				"&" + createKeyValuePair("rid", show.getId()) +
				"&" + createKeyValuePair("cat", settings.getNzbsOrgCategory()) +
				"&" + createKeyValuePair("num", "100") +
				"&" + createKeyValuePair("apikey", settings.getNzbsOrgApiKey());
		
		System.out.println("Search feed: " + feedUrl);
		return feedUrl;
	}
	
	private String createKeyValuePair(String key, String value) {
		return key + "=" + value;
	}
}
