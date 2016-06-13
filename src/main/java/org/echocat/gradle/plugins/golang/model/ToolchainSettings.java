package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.io.File.separator;
import static java.util.regex.Pattern.compile;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.WINDOWS;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.currentOperatingSystem;
import static org.echocat.gradle.plugins.golang.utils.BeanUtils.copyNonNulls;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;

public class ToolchainSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolchainSettings.class);
    protected static final Pattern VERSION_RESPONSE_PATTERN = compile("go version ([0-9\\.\\-_a-z]+) .+");
    @Nonnull
    private final Project _project;

    private Boolean _forceBuildToolchain;
    private String _goversion;
    private File _goroot;
    private Boolean _cgoEnabled;
    private File _bootstrapGoroot;
    private URI _downloadUriRoot;

    @Inject
    public ToolchainSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _goversion = "go1.6.2";
            _downloadUriRoot = URI.create("https://storage.googleapis.com/golang/");
        }
    }

    public Boolean getForceBuildToolchain() {
        return _forceBuildToolchain;
    }

    public void setForceBuildToolchain(Boolean forceBuildToolchain) {
        _forceBuildToolchain = forceBuildToolchain;
    }

    public String getGoversion() {
        return _goversion;
    }

    public void setGoversion(String goversion) {
        _goversion = goversion;
    }

    public File getGoroot() {
        return _goroot;
    }

    public void setGoroot(File goroot) {
        _goroot = goroot;
    }

    public Boolean getCgoEnabled() {
        return _cgoEnabled;
    }

    public void setCgoEnabled(Boolean cgoEnabled) {
        _cgoEnabled = cgoEnabled;
    }

    public File getBootstrapGoroot() {
        return _bootstrapGoroot;
    }

    public void setBootstrapGoroot(File bootstrapGoroot) {
        _bootstrapGoroot = bootstrapGoroot;
    }

    public URI getDownloadUriRoot() {
        return _downloadUriRoot;
    }

    public void setDownloadUriRoot(URI downloadUriRoot) {
        _downloadUriRoot = downloadUriRoot;
    }

    @Nullable
    public String goBinaryVersionOf(File goroot) {
        final File goBinary = goBinaryOf(goroot);
        if (!goBinary.canExecute()) {
            return null;
        }
        final String stdout;
        try {
            stdout = executor()
                .executable(goBinary)
                .argument("version")
                .execute()
                .getStdoutAsString()
            ;
        } catch (final IOException e) {
            LOGGER.debug(e.getMessage(), e);
            return null;
        }

        final Matcher matcher = VERSION_RESPONSE_PATTERN.matcher(stdout.trim());
        if (!matcher.matches()) {
            throw new IllegalStateException(goBinary + " does not respond as expected. Got: " + stdout);
        }
        return matcher.group(1);
    }

    @Nonnull
    public File goBinaryOf(File goroot) {
        if (goroot == null) {
            throw new IllegalArgumentException("There was no goroot provided.");
        }
        return new File(goroot, separator + "bin" + separator + "go" + executableSuffix());
    }

    @Nonnull
    public File goBinary() {
        return goBinaryOf(getGoroot());
    }

    @Nonnull
    public String executableSuffix() {
        if (currentOperatingSystem() == WINDOWS) {
            return ".exe";
        }
        return "";
    }

    @Nonnull
    public ToolchainSettings merge(@Nonnull ToolchainSettings with) {
        final ToolchainSettings result = new ToolchainSettings(false, _project);
        copyNonNulls(ToolchainSettings.class, this, result);
        copyNonNulls(ToolchainSettings.class, with, result);
        return result;
    }

}
