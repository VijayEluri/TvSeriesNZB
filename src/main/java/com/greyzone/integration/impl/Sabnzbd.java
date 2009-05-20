package com.greyzone.integration.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greyzone.domain.Episode;
import com.greyzone.integration.IntegrationDownloader;
import com.greyzone.settings.ApplicationSettings;

@Service
public class Sabnzbd implements IntegrationDownloader {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	private ApplicationSettings appSettings;
	
	@Override
	public void orderDownloadByIds(List<String> ids) {
		HttpClient client = new HttpClient();
		
		for (String id : ids) {
			GetMethod method = new GetMethod(appSettings.getSabnzbdUrl());
			
			List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
			queryParams.add(new NameValuePair("apikey", appSettings.getSabnzbdApiKey()));
			queryParams.add(new NameValuePair("mode", "addid"));
			queryParams.add(new NameValuePair("name", id));
			

			// Only supply username and password if they are specified in configuration
			if (!StringUtils.isEmpty(appSettings.getSabnzbdUsername()) || !StringUtils.isEmpty(appSettings.getSabnzbdPassword())) {
				queryParams.add(new NameValuePair("ma_username", appSettings.getSabnzbdUsername()));
				queryParams.add(new NameValuePair("ma_password", appSettings.getSabnzbdPassword()));
			}
			
			NameValuePair[] valuePairs = queryParams
					.toArray(new NameValuePair[] {});
			
			method.setQueryString(valuePairs);
			
			try {
				log.debug("Instructing SABnzbd to download newzbin id: " + id + " to: " + appSettings.getSabnzbdUrl());
				client.executeMethod(method);
				
				String result = method.getResponseBodyAsString();
				if (!result.startsWith("ok")) {
					log.error("SABnzbd reported an error, check your configuration! Message from SABnzbd: " + result);
					throw new RuntimeException("SABnzbd integration failed");
				}
			} catch (Exception e) {
				log.error("Failed to contact SABnzbd", e);
				throw new RuntimeException("Could not contact SABnzbd.");
			}
		}
	}

	@Override
	public void orderDownloadByEpisode(Episode ep) {
		if (StringUtils.isEmpty(ep.getIndexId())) {
			log.debug("Could not download episode because no newzbin id was specified");
			return;
		}
		List<String> ids = new ArrayList<String>();
		ids.add(ep.getIndexId());
		orderDownloadByIds(ids);
	}
}
