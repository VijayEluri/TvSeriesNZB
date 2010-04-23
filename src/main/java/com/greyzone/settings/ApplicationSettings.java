package com.greyzone.settings;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service("ApplicationSettings")
public class ApplicationSettings {

    public static String     FILENAME_CONFIGURATION = "tvseriesnzb.conf";

    private final Properties conf;
    private final Logger     log                    = Logger.getLogger(this.getClass());

    private boolean          dryRun;
    private String           showsFileName          = "shows.xml";
    private String           moviesFileName         = "movies.xml";

    public ApplicationSettings() {
        conf = new Properties();

        try {
            log.debug("Reading configuration: " + FILENAME_CONFIGURATION);
            FileInputStream fis = new FileInputStream(FILENAME_CONFIGURATION);
            conf.load(fis);
            fis.close();
        } catch (Exception e) {
            log.error("Could not read or parse configuration: " + FILENAME_CONFIGURATION, e);
        }
    }

    public String getSabnzbdUrl() {
        return getProperty("sabnzbd.url", "http://localhost:8080/sabnzbd/api");
    }

    public String getSabnzbdApiKey() {
        return getProperty("sabnzbd.apikey", "NOT_SPECIFIED:sabnzbd.apikey");
    }

    public String getSabnzbdUsername() {
        return getProperty("sabnzbd.username", "");
    }

    public String getSabnzbdPassword() {
        return getProperty("sabnzbd.password", "");
    }

    public String getNewzbinUsername() {
        return getProperty("newzbin.username", "");
    }

    public String getNewzbinPassword() {
        return getProperty("newzbin.password", "");
    }

    public String getNzbsOrgFeed() {
        return getProperty("nzbsorg.feed", "");
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public void setShowsFileName(String showsFileName) {
        this.showsFileName = showsFileName;
    }

    public String getShowsFileName() {
        return showsFileName;
    }

    public String getMoviesFileName() {
        return moviesFileName;
    }

    public void setMoviesFileName(String moviesFileName) {
        this.moviesFileName = moviesFileName;
    }

    private String getProperty(String key, String defaultValue) {

        String property = conf.getProperty(key);

        if (property == null) {
            log.warn("Could not find property: " + key + " in configuration. Using default instead: "
                    + defaultValue);
            return defaultValue;
        }

        return property;
    }
}
