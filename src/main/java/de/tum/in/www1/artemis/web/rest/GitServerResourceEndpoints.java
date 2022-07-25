package de.tum.in.www1.artemis.web.rest;

public final class GitServerResourceEndpoints {

    public static final String ROOT = "/git";

    public static final String INITPROJECT = "/init-project";

    public static final String GETPROJECT = "/project/:projectKey";

    private GitServerResourceEndpoints() {
    }
}