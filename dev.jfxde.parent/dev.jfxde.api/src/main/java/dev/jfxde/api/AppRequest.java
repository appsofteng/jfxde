package dev.jfxde.api;

public class AppRequest {

    private Resource resource;

    public AppRequest(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }
}
