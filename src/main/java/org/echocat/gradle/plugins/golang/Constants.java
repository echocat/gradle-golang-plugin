package org.echocat.gradle.plugins.golang;

public interface Constants {

    public static final String GO_BOOTSTRAP_VERSION = "go1.5.2";

    public static final String DEFAULT_GO_VERSION = "go1.6.2";
    public static final String DEFAULT_PLATFORMS = "linux-386,linux-amd64,windows-386,windows-amd64,darwin-amd64";
    public static final String DEFAULT_DOWNLOAD_URI_ROOT = "https://storage.googleapis.com/golang/";

    public static final String PROPERTY_HOST_PLATFORM = "golang.hostPlatform";
    public static final String PROPERTY_GOVERSION = "golang.goversion";
    public static final String PROPERTY_GOROOT = "golang.goroot";
    public static final String PROPERTY_GOPATH = "golang.gopath";
    public static final String PROPERTY_USE_TEMPORARY_GOPATH = "golang.useTemporaryGopath";
    public static final String PROPERTY_CGO_ENABLED = "golang.cgoEnabled";
    public static final String PROPERTY_PLATFORMS = "golang.platforms";
    public static final String PROPERTY_PACKAGE_NAME = "golang.packageName";
    public static final String PROPERTY_FORCE_UPDATE = "golang.forceUpdate";
    public static final String PROPERTY_SHOW_ENVIRONMENT_INFO = "golang.showEnvironmentInfo";
    public static final String PROPERTY_DELETE_UNKNOWN_DEPENDENCIES = "golang.deleteUnknownDependencies";
    public static final String PROPERTY_DEPENDENCY_CACHE = "golang.dependencyCache";

    public static final String PROPERTY_OUTPUT_FILENAME_PATTERN = "golang.outputFilenamePattern";
    public static final String PROPERTY_FORCE_REBUILD = "golang.forceRebuild";

    public static final String PROPERTY_BOOTSTRAP_GOROOT = "golang.bootstrapRoot";
    public static final String PROPERTY_CACHE_ROOT = "golang.cacheRoot";
    public static final String PROPERTY_DOWNLOAD_URI_ROOT = "golang.downloadUriRoot";
    public static final String PROPERTY_FORCE_BUILD_TOOLCHAIN = "golang.forceBuildToolchain";

    public static final String VCS_REPOSITORY_INFO_FILE_NAME = ".vcs-repository-info";
}
