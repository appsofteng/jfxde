package dev.jfxde.apps.webbrowser;

import java.util.Set;
import java.util.TreeSet;

public class DataController {

	private Set<String> locations = new TreeSet<>();

	public SearchEngine getDefaultSearchEngine() {

		return new SearchEngine("Google", "http://www.google.com", "http://www.google.com/search?q=");
	}

	public Set<String> getLocations() {
		return locations;
	}
}
