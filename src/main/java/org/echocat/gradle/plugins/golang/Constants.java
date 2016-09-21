package org.echocat.gradle.plugins.golang;

import java.net.URI;

public interface Constants {

    public static final String DEFAULT_GO_VERSION = "go1.7.1";
    public static final String DEFAULT_PLATFORMS = "linux-386,linux-amd64,windows-386,windows-amd64,darwin-amd64";
    public static final URI DEFAULT_DOWNLOAD_URI_ROOT = URI.create("https://storage.googleapis.com/golang/");

    public static final String VCS_REPOSITORY_INFO_FILE_NAME = ".vcs-repository-info";
    String VENDOR_DIRECTORY_NAME = "vendor";
}
