package dev.jfxde.apps.webbrowser;

import java.util.HashSet;
import java.util.Set;

public class DataController {

	private Set<String> locations = new HashSet<>();

	public SearchEngine getDefaultSearchEngine() {

		return new SearchEngine("Google", "http://www.google.com", "http://www.google.com/search?q=");
	}

	public Set<String> getLocations() {
		return locations;
	}
}
