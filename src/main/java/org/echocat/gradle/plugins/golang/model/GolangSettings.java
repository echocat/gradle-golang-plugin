package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.echocat.gradle.plugins.golang.model.Platform.currentPlatform;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.toPath;

public class GolangSettings {

    @Nonnull
    private final Project _project;

    private List<Platform> _platforms;
    private String _packageName;
    private Platform _hostPlatform;
    private Path _cacheRoot;

    @Inject
    public GolangSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            setHostPlatform(currentPlatform());
            setPlatforms(currentPlatform());
            setCacheRoot(Paths.get(System.getProperty("user.home", ".")).resolve(".go"));
        }
    }

    public List<Platform> getPlatforms() {
        return _platforms;
    }

    public void setPlatforms(List<Platform> platforms) {
        _platforms = platforms;
    }

    public void setPlatforms(Platform... platforms) {
        setPlatforms(platforms != null ? Arrays.asList(platforms) : null);
    }

    public void setPlatforms(String platforms) {
        setPlatforms(platforms != null ? Platform.toPlatforms(platforms) : null);
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

    public void setHostPlatform(String hostPlatform) {
        setHostPlatform(hostPlatform != null ? Platform.resolveForGo(hostPlatform) : null);
    }

    public Path getCacheRoot() {
        return _cacheRoot;
    }

    public void setCacheRoot(Path cacheRoot) {
        _cacheRoot = cacheRoot;
    }

    public void setCacheRoot(String cacheRoot) {
        setCacheRoot(toPath(cacheRoot));
    }

    @Nonnull
    public Path packagePathFor(@Nonnull Path gopath) {
        return gopath.resolve("src").resolve(getPackageName()).toAbsolutePath();
    }

    @Nonnull
    public Path getProjectBasedir() {
        try {
            return _project.getProjectDir().getCanonicalFile().toPath();
        } catch (final IOException e) {
            throw new RuntimeException("Could not resolve basedir of project.", e);
        }
    }

}
