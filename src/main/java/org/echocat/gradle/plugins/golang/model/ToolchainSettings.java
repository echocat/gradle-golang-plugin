package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.isExecutable;
import static java.util.regex.Pattern.compile;
import static org.echocat.gradle.plugins.golang.Constants.DEFAULT_DOWNLOAD_URI_ROOT;
import static org.echocat.gradle.plugins.golang.Constants.DEFAULT_GO_VERSION;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.WINDOWS;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.currentOperatingSystem;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;

public class ToolchainSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolchainSettings.class);
    protected static final Pattern VERSION_RESPONSE_PATTERN = compile("go version ([0-9\\.\\-_a-z]+) .+");
    @Nonnull
    private final Project _project;

    private Boolean _forceBuildToolchain;
    private String _goversion;
    private Path _goroot;
    private Boolean _cgoEnabled;
    private Path _bootstrapGoroot;
    private URI _downloadUriRoot;

    @Inject
    public ToolchainSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _goversion = DEFAULT_GO_VERSION;
            _downloadUriRoot = DEFAULT_DOWNLOAD_URI_ROOT;
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

    public Path getGoroot() {
        return _goroot;
    }

    public void setGoroot(Path goroot) {
        _goroot = goroot;
    }

    public Boolean getCgoEnabled() {
        return _cgoEnabled;
    }

    public void setCgoEnabled(Boolean cgoEnabled) {
        _cgoEnabled = cgoEnabled;
    }

    public Path getBootstrapGoroot() {
        return _bootstrapGoroot;
    }

    public void setBootstrapGoroot(Path bootstrapGoroot) {
        _bootstrapGoroot = bootstrapGoroot;
    }

    public URI getDownloadUriRoot() {
        return _downloadUriRoot;
    }

    public void setDownloadUriRoot(URI downloadUriRoot) {
        _downloadUriRoot = downloadUriRoot;
    }

    @Nullable
    public String goBinaryVersionOf(Path goroot) {
        final Path goBinary = goBinaryOf(goroot);
        if (!isExecutable(goBinary)) {
            return null;
        }
        final String stdout;
        try {
            stdout = executor(goBinary)
                .env("GOROOT", goBinary.getParent().toAbsolutePath().toString())
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
    public Path goBinaryOf(Path goroot) {
        if (goroot == null) {
            throw new IllegalArgumentException("There was no goroot provided.");
        }
        return toolchainBinaryOf("go", goroot);
    }

    @Nonnull
    public Path getGoBinary() {
        return toolchainBinary("go");
    }

    @Nonnull
    public Path toolchainBinaryOf(String name, Path goroot) {
        if (goroot == null) {
            throw new IllegalArgumentException("There was no goroot provided.");
        }
        return goroot.resolve("bin").resolve(name + getExecutableSuffix());
    }

    @Nonnull
    public Path toolchainBinary(String name) {
        return toolchainBinaryOf(name, getGoroot());
    }

    @Nonnull
    public Path getGorootSourceRoot() {
        final Path goroot = getGoroot();
        if (goroot == null) {
            throw new IllegalStateException("There is no goroot cofigured.");
        }
        return goroot.resolve("src");
    }

    @Nonnull
    public String getExecutableSuffix() {
        if (currentOperatingSystem() == WINDOWS) {
            return ".exe";
        }
        return "";
    }

}
