package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;
import org.gradle.internal.impldep.com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

import static java.io.File.pathSeparator;
import static java.io.File.separator;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.notExists;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class BuildSettings {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @Nonnull
    private final Project _project;

    private Paths _gopath;
    private Boolean _useTemporaryGopath;
    private String[] _includes;
    private String[] _excludes;
    private String[] _arguments;
    private String _outputFilenamePattern;
    private Map<String, String> _definitions;

    @Inject
    public BuildSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            final String gopath = System.getenv("GOPATH");
            if (isNotEmpty(gopath)) {
                setGopath(gopath);
            } else {
                final Path tempGopath = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir", "tmp")).resolve("gopath");
                if (notExists(tempGopath)) {
                    try {
                        createDirectories(tempGopath);
                    } catch (final IOException e) {
                        throw new RuntimeException("Could not create temporary gopath: " + tempGopath, e);
                    }
                }
                setGopath(tempGopath);
            }
            _outputFilenamePattern = project.getBuildDir().toPath().resolve("out").resolve(project.getName()) + "-%{platform}%{extension}";
            _excludes = new String[]{
                ".git/**", ".svn/**", "build.gradle", "build/**", ".gradle/**", "gradle/**"
            };
        }
    }

    @Nullable
    public Paths getGopath() {
        return _gopath;
    }

    /**
     * @return the full gopath as string
     * @deprecated Use {@link #getGopath()}.{@link Paths#toString() toString()} instead.
     */
    @Deprecated
    public String getGopathAsString() {
        final Paths gopath = getGopath();
        if (gopath == null) {
            throw new NullPointerException("null for gopath set.");
        }
        return gopath.toString();
    }

    @Nonnull
    public Path getFirstGopath() {
        final Paths gopath = getGopath();
        if (gopath == null) {
            throw new NullPointerException("null for gopath set.");
        }
        final Path firstElement = gopath.firstElement();
        if (firstElement == null) {
            throw new NullPointerException("empty for gopath set.");
        }
        return firstElement;
    }

    public void setGopath(Paths gopath) {
        if (gopath == null) {
            throw new NullPointerException("null for gopath provided.");
        }
        if (gopath.isEmpty()) {
            throw new NullPointerException("empty gopath provided.");
        }
        _gopath = gopath;
    }

    public void setGopath(Iterable<Path> gopath) {
        setGopath(gopath != null ? new Paths(gopath) : null);
    }

    public void setGopath(Path gopath) {
        setGopath(gopath != null ? new Paths(gopath) : null);
    }

    public void setGopath(Path... gopath) {
        setGopath(gopath != null ? new Paths(gopath) : null);
    }

    public void setGopath(String gopath) {
        setGopath(gopath != null ? new Paths(gopath) : null);
    }

    public Boolean getUseTemporaryGopath() {
        return _useTemporaryGopath;
    }

    public void setUseTemporaryGopath(Boolean useTemporaryGopath) {
        _useTemporaryGopath = useTemporaryGopath;
    }

    public String[] getIncludes() {
        return _includes;
    }

    public void setIncludes(String[] includes) {
        _includes = includes;
    }

    public String[] getExcludes() {
        return _excludes;
    }

    public void setExcludes(String[] excludes) {
        _excludes = excludes;
    }

    public String getOutputFilenamePattern() {
        return _outputFilenamePattern;
    }

    public void setOutputFilenamePattern(String outputFilenamePattern) {
        _outputFilenamePattern = outputFilenamePattern;
    }

    public Map<String, String> getDefinitions() {
        return _definitions;
    }

    public void setDefinitions(Map<String, String> definitions) {
        _definitions = definitions;
    }

    public String[] getArguments() {
        return _arguments;
    }

    public void setArguments(String[] arguments) {
        _arguments = arguments;
    }

    @Nonnull
    public Path outputFilenameFor(@Nonnull Platform platform) {
        return java.nio.file.Paths.get(replacePlaceholdersFor(platform, getOutputFilenamePattern()));
    }

    @Nonnull
    public String getDefinitionsEscaped() {
        final StringBuilder sb = new StringBuilder();
        final Map<String, String> definitions = getDefinitions();
        if (definitions != null) {
            for (final Entry<String, String> entry : definitions.entrySet()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                final Object plainValue = entry.getValue();
                final String escapedValue = plainValue != null ? plainValue.toString()
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    : "";
                sb.append("-X \"").append((Object) entry.getKey()).append('=').append(escapedValue).append("\"");
            }
        }
        return sb.toString();
    }

    @Nonnull
    public String replacePlaceholdersFor(@Nonnull Platform platform, @Nonnull String input) {
        return input
            .replace("%{platform}", platform.getNameInGo())
            .replace("%{extension}", platformExtensionFor(platform))
            .replace("%{separator}", separator)
            .replace("%{pathSeparator}", pathSeparator)
            ;
    }

    @Nonnull
    public String platformExtensionFor(@Nonnull Platform platform) {
        if (Objects.equals(platform.getOperatingSystem(), OperatingSystem.WINDOWS)) {
            return ".exe";
        }
        return "";
    }

    @Nonnull
    public String getCurrentPlatformExtension() {
        return platformExtensionFor(Platform.currentPlatform());
    }

    @Nonnull
    public Paths getGopathSourceRoot() {
        final Paths originals = getGopath();
        final List<Path> results = new ArrayList<>(originals != null ? originals.size() : 0);
        if (originals != null) {
            for (final Path original : originals) {
                results.add(original.resolve("src"));
            }
        }
        return new Paths(results);
    }

    @Nonnull
    public Path getFirstGopathSourceRoot() {
        return getFirstGopath().resolve("src");
    }

    @Nonnull
    public List<String> getResolvedArguments() {
        final List<String> result = new ArrayList<>();
        final String[] arguments = getArguments();
        final StringBuilder ldFlagsValue = new StringBuilder();
        if (arguments != null) {
            final Iterator<String> i = Iterators.forArray(arguments);
            while (i.hasNext()) {
                final String argument = i.next();
                if ("-ldflags".equals(argument)) {
                    if (i.hasNext()) {
                        ldFlagsValue.append(i.next());
                    }
                } else {
                    result.add(argument);
                }
            }
        }
        final String definitionsEscaped = getDefinitionsEscaped();
        if (ldFlagsValue.length() > 0 && !definitionsEscaped.isEmpty()) {
            ldFlagsValue.append(' ');
        }
        ldFlagsValue.append(definitionsEscaped);
        if (ldFlagsValue.length() > 0) {
            result.add("-ldflags");
            result.add(ldFlagsValue.toString());
        }
        return result;
    }

}
