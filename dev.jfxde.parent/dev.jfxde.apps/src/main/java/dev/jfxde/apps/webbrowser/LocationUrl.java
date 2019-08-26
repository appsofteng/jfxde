package dev.jfxde.apps.webbrowser;

import java.net.MalformedURLException;
import java.net.URL;

public class LocationUrl {

	private final String url;
	private String query = "";

	public LocationUrl(String url) {
		url = url == null ? "" : url;
		this.url = url;
	}

	public String getFormattedUrl(SearchEngine searchEngine) {

	    if (url.isEmpty()) {
	        return url;
	    }

		String formattedUrl = url;

		if (!url.contains(":/")) {
			formattedUrl = "http://" + url;
			query = searchEngine.getQueryUrl(url);
		}

		try {
			formattedUrl = new URL(formattedUrl).toString();

			if (formattedUrl.matches(".*\\s+.*")) {
				throw new MalformedURLException();
			}

		} catch (MalformedURLException e) {
			formattedUrl = searchEngine.getQueryUrl(url);
			query = "";
		}

		return formattedUrl;
	}

	public String getQuery() {
		return query;
	}
}
