package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.io.File.pathSeparator;
import static java.io.File.separator;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class BuildSettings {

    private File _gopath;
    private boolean _useTemporaryGopath;

    /**
     * Force rebuilding of packages that are already up-to-date.
     */
    @Argument("-a")
    private boolean _forceRebuild;
    /**
     * The number of programs, such as build commands or test binaries, that can be run in parallel.
     * The default is the number of CPUs available, except on darwin/arm which defaults to 1.
     */
    @Argument("-p")
    private Integer _parallelRuns;
    /**
     * Enable data race detection.
     * Supported only on linux/amd64, freebsd/amd64, darwin/amd64 and windows/amd64.
     */
    @Argument("-race")
    private boolean _raceDetection;
    /**
     * Enable interoperation with memory sanitizer.
     * Supported only on linux/amd64, and only with Clang/LLVM as the host C compiler.
     */
    @Argument("-msan")
    private boolean _interoperationWithMemorySanitizer;
    @Argument("-v")
    private boolean _printCompiledPackages;
    /**
     * Print the name of the temporary work directory and do not delete it when exiting.
     */
    @Argument("-work")
    private boolean _printWorkDirectory;
    @Argument("-x")
    private boolean _printCommands;
    @Argument("-asmflags")
    private String _asmFlags;
    @Argument("-buildmode")
    private String _buildmode;
    @Argument("-compiler")
    private String _compiler;
    @Argument("-gccgoflags")
    private String _gccgoFlags;
    @Argument("-gcflags")
    private String _gcFlags;
    private String _ldFlags;
    @Argument("-linkshared")
    private boolean _linkShared;
    @Argument("-pkgdir")
    private File _pkgDir;
    @Argument("-tags")
    private String _tags;
    @Argument("-toolexec")
    private String _toolexec;

    private String _outputFilenamePattern;
    private Properties _definitions;

    public BuildSettings(@Nonnull Project project) {
        final String gopath = System.getenv("GOPATH");
        if (isNotEmpty(gopath)) {
            _gopath = new File(gopath);
        }
        _outputFilenamePattern = project.getBuildDir() + File.separator + "out" + File.separator + project.getProjectDir().getName() + "-%{platform}%{extension}";
    }

    public File getGopath() {
        return _gopath;
    }

    public BuildSettings setGopath(File gopath) {
        _gopath = gopath;
        return this;
    }

    public boolean isUseTemporaryGopath() {
        return _useTemporaryGopath;
    }

    public BuildSettings setUseTemporaryGopath(boolean useTemporaryGopath) {
        _useTemporaryGopath = useTemporaryGopath;
        return this;
    }

    public boolean isForceRebuild() {
        return _forceRebuild;
    }

    public BuildSettings setForceRebuild(boolean forceRebuild) {
        _forceRebuild = forceRebuild;
        return this;
    }

    public Integer getParallelRuns() {
        return _parallelRuns;
    }

    public BuildSettings setParallelRuns(Integer parallelRuns) {
        _parallelRuns = parallelRuns;
        return this;
    }

    public boolean isRaceDetection() {
        return _raceDetection;
    }

    public BuildSettings setRaceDetection(boolean raceDetection) {
        _raceDetection = raceDetection;
        return this;
    }

    public boolean isInteroperationWithMemorySanitizer() {
        return _interoperationWithMemorySanitizer;
    }

    public BuildSettings setInteroperationWithMemorySanitizer(boolean interoperationWithMemorySanitizer) {
        _interoperationWithMemorySanitizer = interoperationWithMemorySanitizer;
        return this;
    }

    public boolean isPrintCompiledPackages() {
        return _printCompiledPackages;
    }

    public BuildSettings setPrintCompiledPackages(boolean printCompiledPackages) {
        _printCompiledPackages = printCompiledPackages;
        return this;
    }

    public boolean isPrintWorkDirectory() {
        return _printWorkDirectory;
    }

    public BuildSettings setPrintWorkDirectory(boolean printWorkDirectory) {
        _printWorkDirectory = printWorkDirectory;
        return this;
    }

    public boolean isPrintCommands() {
        return _printCommands;
    }

    public BuildSettings setPrintCommands(boolean printCommands) {
        _printCommands = printCommands;
        return this;
    }

    public String getAsmFlags() {
        return _asmFlags;
    }

    public BuildSettings setAsmFlags(String asmFlags) {
        _asmFlags = asmFlags;
        return this;
    }

    public String getBuildmode() {
        return _buildmode;
    }

    public BuildSettings setBuildmode(String buildmode) {
        _buildmode = buildmode;
        return this;
    }

    public String getCompiler() {
        return _compiler;
    }

    public BuildSettings setCompiler(String compiler) {
        _compiler = compiler;
        return this;
    }

    public String getGccgoFlags() {
        return _gccgoFlags;
    }

    public BuildSettings setGccgoFlags(String gccgoFlags) {
        _gccgoFlags = gccgoFlags;
        return this;
    }

    public String getGcFlags() {
        return _gcFlags;
    }

    public BuildSettings setGcFlags(String gcFlags) {
        _gcFlags = gcFlags;
        return this;
    }

    public String getLdFlags() {
        return _ldFlags;
    }

    public BuildSettings setLdFlags(String ldFlags) {
        _ldFlags = ldFlags;
        return this;
    }

    public boolean isLinkShared() {
        return _linkShared;
    }

    public BuildSettings setLinkShared(boolean linkShared) {
        _linkShared = linkShared;
        return this;
    }

    public File getPkgDir() {
        return _pkgDir;
    }

    public BuildSettings setPkgDir(File pkgDir) {
        _pkgDir = pkgDir;
        return this;
    }

    public String getTags() {
        return _tags;
    }

    public BuildSettings setTags(String tags) {
        _tags = tags;
        return this;
    }

    public String getToolexec() {
        return _toolexec;
    }

    public BuildSettings setToolexec(String toolexec) {
        _toolexec = toolexec;
        return this;
    }

    public String getOutputFilenamePattern() {
        return _outputFilenamePattern;
    }

    public BuildSettings setOutputFilenamePattern(String outputFilenamePattern) {
        _outputFilenamePattern = outputFilenamePattern;
        return this;
    }

    public Properties getDefinitions() {
        return _definitions;
    }

    public BuildSettings setDefinitions(Properties definitions) {
        _definitions = definitions;
        return this;
    }

    @Nonnull
    public File outputFilenameFor(@Nonnull Platform platform)  {
        return new File(replacePlaceholdersFor(platform, getOutputFilenamePattern()));
    }

    @Nonnull
    public String ldflagsWithDefinitions() {
        final StringBuilder sb = new StringBuilder();
        final String ldFlags = getLdFlags();
        if (isNotEmpty(ldFlags)) {
            sb.append(ldFlags);
        }
        final Properties definitions = getDefinitions();
        if (definitions != null) {
            for (final String name : definitions.stringPropertyNames()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                final String plainValue = definitions.getProperty(name, "");
                final String escapedValue = plainValue
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    ;
                sb.append("-X \\\"").append(name).append('=').append(escapedValue).append("\\\"");
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
        if (platform.getOperatingSystem() == OperatingSystem.WINDOWS) {
            return ".exe";
        }
        return "";
    }

    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface Argument {
        public String value();
    }

    @Nonnull
    public Map<String, String> additionalArgumentMap() {
        final Map<String, String> result = new HashMap<>();
        for (final Field field : BuildSettings.class.getDeclaredFields()) {
            final BuildSettings.Argument argument = field.getAnnotation(BuildSettings.Argument.class);
            if (argument != null) {
                final Object plainValue;
                try {
                    field.setAccessible(true);
                    plainValue = field.get(this);
                } catch (final Exception e) {
                    throw new IllegalStateException("Could not get value of field " + field + ".", e);
                }
                if (plainValue instanceof Boolean) {
                    if ((Boolean)plainValue) {
                        result.put(argument.value(), null);
                    }
                } else if (plainValue instanceof Integer) {
                    result.put(argument.value(), plainValue.toString());
                } else if (plainValue instanceof String) {
                    if (isNotEmpty((String)plainValue)) {
                        result.put(argument.value(), plainValue.toString());
                    }
                } else if (plainValue != null) {
                    throw new IllegalArgumentException("The type of field " + field + " is currently not supported.");
                }
            }
        }
        return result;
    }
    
}
