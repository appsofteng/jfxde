package dev.jfxde.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class Resource {

    private String uriStr;
    private URI uri;
    private String scheme;
    private String extension;

    public Resource(String uriStr) {
        this.uriStr = uriStr == null ? "" : uriStr;
    }

    public URI getUri() {
        if (uri == null) {
            try {
                uri = new URI(uriStr);
            } catch (URISyntaxException e) {
                uri = URI.create("");
            }
        }

        return uri;
    }

    public String getScheme() {

        if (scheme == null) {
            scheme = Objects.requireNonNullElse(getUri().getScheme(), "");

        }
        return scheme;
    }

    public String getExtension() {

        if (extension == null) {

            int i = uriStr.lastIndexOf(".");
            extension = uriStr.substring(i > -1 ? i : uriStr.length(), uriStr.length());
        }

        return extension;
    }
}
