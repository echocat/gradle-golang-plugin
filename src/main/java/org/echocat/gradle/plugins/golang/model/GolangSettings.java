package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.echocat.gradle.plugins.golang.model.Platform.toPlatforms;

public class GolangSettings {

    @Nonnull
    private final Project _project;

    private String _platforms;
    private String _packageName;
    private Platform _hostPlatform;
    private Path _cacheRoot;

    @Inject
    public GolangSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _platforms = "linux-386,linux-amd64,windows-386,windows-amd64,darwin-amd64";
            _cacheRoot = Paths.get(System.getProperty("user.home", ".")).resolve(".go");
        }
    }

    public String getPlatforms() {
        return _platforms;
    }

    public void setPlatforms(String platforms) {
        _platforms = platforms;
    }

    public void setPlatforms(Platform... platforms) {
        final StringBuilder sb = new StringBuilder();
        if (platforms != null) {
            for (final Platform platform : platforms) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(platform.getNameInGo());
            }
        }
        setPlatforms(sb.toString());
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

    public Path getCacheRoot() {
        return _cacheRoot;
    }

    public void setCacheRoot(Path cacheRoot) {
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
