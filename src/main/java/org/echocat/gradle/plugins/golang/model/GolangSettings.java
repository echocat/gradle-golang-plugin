package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.echocat.gradle.plugins.golang.model.Platform.toPlatforms;

public class GolangSettings {

    @Nonnull
    private final Project _project;

    private String _platforms;
    private String _packageName;
    private Platform _hostPlatform;
    private File _cacheRoot;

    public GolangSettings(@Nonnull Project project) {
        _project = project;

        _platforms = "linux-386,linux-amd64,windows-386,windows-amd64,darwin-amd64";
        _cacheRoot = new File(System.getProperty("user.home", "."), ".go");
    }

    public String getPlatforms() {
        return _platforms;
    }

    @Nonnull
    public GolangSettings setPlatforms(String platforms) {
        _platforms = platforms;
        return this;
    }

    public Platform getHostPlatform() {
        return _hostPlatform;
    }

    public GolangSettings setHostPlatform(Platform hostPlatform) {
        _hostPlatform = hostPlatform;
        return this;
    }

    public String getPackageName() {
        return _packageName;
    }

    @Nonnull
    public GolangSettings setPackageName(String packageName) {
        _packageName = packageName;
        return this;
    }

    public File getCacheRoot() {
        return _cacheRoot;
    }

    public GolangSettings setCacheRoot(File cacheRoot) {
        _cacheRoot = cacheRoot;
        return this;
    }

    @Nonnull
    public List<Platform> getParsedPlatforms() {
        try {
            return toPlatforms(getPlatforms());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Nonnull
    public File packagePath() {
        final File gopath = build().getGopath();
        try {
            return new File(gopath, "src/" + getPackageName()).getCanonicalFile();
        } catch (final IOException e) {
            throw new RuntimeException("Could not resolve path of package '" + getPackageName() + "' in gopath '" + gopath + ".", e);
        }
    }

    @Nonnull
    public File projectBasedir() {
        try {
            return _project.getProjectDir().getCanonicalFile();
        } catch (final IOException e) {
            throw new RuntimeException("Could not resolve basedir of project.", e);
        }
    }

    @Nonnull
    public BuildSettings build() {
        return ((ExtensionAware) this).getExtensions().getByType(BuildSettings.class);
    }

    @Nonnull
    public ToolchainSettings toolchain() {
        return ((ExtensionAware) this).getExtensions().getByType(ToolchainSettings.class);
    }

    @Nonnull
    public DependenciesSettings dependencies() {
        return ((ExtensionAware) this).getExtensions().getByType(DependenciesSettings.class);
    }

}
