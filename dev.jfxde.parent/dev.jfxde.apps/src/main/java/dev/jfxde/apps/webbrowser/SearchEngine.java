package dev.jfxde.apps.webbrowser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SearchEngine {

    private String name;
    private String url;
    private String queryUrl;


    public SearchEngine(String name, String url, String queryUrl) {
        this.name = name;
        this.url = url;
        this.queryUrl = queryUrl;
    }

    public String getName() {
        return name;
    }
    public String getUrl() {
        return url;
    }

    public String getQueryUrl() {
        return queryUrl;
    }

    public String getQueryUrl(String query) {

        try {
            if (query != null && !query.isEmpty()) {
                query = queryUrl + URLEncoder.encode(query, "UTF-8");
            } else {
                query = url;
            }

        } catch (UnsupportedEncodingException e) {

            throw new AssertionError(e);
        }

        return query;
    }

    @Override
    public String toString() {
        return name;
    }
}