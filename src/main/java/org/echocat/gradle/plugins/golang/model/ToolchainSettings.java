package org.echocat.gradle.plugins.golang.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.io.File.separator;
import static java.util.regex.Pattern.compile;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.WINDOWS;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.currentOperatingSystem;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;

public class ToolchainSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolchainSettings.class);
    protected static final Pattern VERSION_RESPONSE_PATTERN = compile("go version ([0-9\\.\\-_a-z]+) .+");

    private boolean _forceBuildToolchain;
    private String _goversion;
    private File _goroot;
    private boolean _cgoEnabled;
    private File _bootstrapGoroot;
    private URI _downloadUriRoot;

    public ToolchainSettings() {
        _goversion = "go1.6.2";
        _downloadUriRoot = URI.create("https://storage.googleapis.com/golang/");
    }

    public String getGoversion() {
        return _goversion;
    }

    @Nonnull
    public ToolchainSettings setGoversion(String goversion) {
        _goversion = goversion;
        return this;
    }

    public File getGoroot() {
        return _goroot;
    }

    @Nonnull
    public ToolchainSettings setGoroot(File goroot) {
        _goroot = goroot;
        return this;
    }

    public boolean isCgoEnabled() {
        return _cgoEnabled;
    }

    @Nonnull
    public ToolchainSettings setCgoEnabled(boolean cgoEnabled) {
        _cgoEnabled = cgoEnabled;
        return this;
    }

    public File getBootstrapGoroot() {
        return _bootstrapGoroot;
    }

    public ToolchainSettings setBootstrapGoroot(File bootstrapGoroot) {
        _bootstrapGoroot = bootstrapGoroot;
        return this;
    }

    public URI getDownloadUriRoot() {
        return _downloadUriRoot;
    }

    public ToolchainSettings setDownloadUriRoot(URI downloadUriRoot) {
        _downloadUriRoot = downloadUriRoot;
        return this;
    }

    public boolean isForceBuildToolchain() {
        return _forceBuildToolchain;
    }

    public ToolchainSettings setForceBuildToolchain(boolean forceBuildToolchain) {
        _forceBuildToolchain = forceBuildToolchain;
        return this;
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

}
