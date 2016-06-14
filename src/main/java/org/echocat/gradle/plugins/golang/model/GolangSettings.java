package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.echocat.gradle.plugins.golang.model.Platform.toPlatforms;
import static org.echocat.gradle.plugins.golang.utils.BeanUtils.copyNonNulls;

public class GolangSettings {

    @Nonnull
    private final Project _project;

    private String _platforms;
    private String _packageName;
    private Platform _hostPlatform;
    private File _cacheRoot;

    @Inject
    public GolangSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _platforms = "linux-386,linux-amd64,windows-386,windows-amd64,darwin-amd64";
            _cacheRoot = new File(System.getProperty("user.home", "."), ".go");
        }
    }

    public String getPlatforms() {
        return _platforms;
    }

    public void setPlatforms(String platforms) {
        _platforms = platforms;
    }

    public String getPackageName() {
        return _packageName;
    }

    public void setPackageName(String packageName) {
        _packageName = packageName;
    }

    public Platform getHostPlatform() {
        return _hostPlatform;
    }

    public void setHostPlatform(Platform hostPlatform) {
        _hostPlatform = hostPlatform;
    }

    public File getCacheRoot() {
        return _cacheRoot;
    }

    public void setCacheRoot(File cacheRoot) {
        _cacheRoot = cacheRoot;
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
    public File packagePathFor(@Nonnull File gopath) {
        try {
            return new File(gopath, "src/" + getPackageName()).getCanonicalFile();
        } catch (final IOException e) {
            throw new RuntimeException("Could not resolve path of package '" + getPackageName() + "' in gopath '" + gopath + ".", e);
        }
    }

    @Nonnull
    public File getProjectBasedir() {
        try {
            return _project.getProjectDir().getCanonicalFile();
        } catch (final IOException e) {
            throw new RuntimeException("Could not resolve basedir of project.", e);
        }
    }

    @Nonnull
    public GolangSettings merge(@Nonnull GolangSettings with) {
        final GolangSettings result = new GolangSettings(false, _project);
        copyNonNulls(GolangSettings.class, this, result);
        copyNonNulls(GolangSettings.class, with, result);
        return result;
    }

}
