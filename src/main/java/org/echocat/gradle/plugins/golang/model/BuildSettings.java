package org.echocat.gradle.plugins.golang.model;

import org.echocat.gradle.plugins.golang.utils.BeanUtils;
import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.io.File.pathSeparator;
import static java.io.File.separator;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class BuildSettings {

    @Nonnull
    private final Project _project;
    private File _gopath;
    private Boolean _useTemporaryGopath;
    private String[] _includes;
    private String[] _excludes;

    /**
     * Force rebuilding of packages that are already up-to-date.
     */
    @Argument("-a")
    private Boolean _forceRebuild;
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
    private Boolean _raceDetection;
    /**
     * Enable interoperation with memory sanitizer.
     * Supported only on linux/amd64, and only with Clang/LLVM as the host C compiler.
     */
    @Argument("-msan")
    private Boolean _interoperationWithMemorySanitizer;
    @Argument("-v")
    private Boolean _printCompiledPackages;
    /**
     * Print the name of the temporary work directory and do not delete it when exiting.
     */
    @Argument("-work")
    private Boolean _printWorkDirectory;
    @Argument("-x")
    private Boolean _printCommands;
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
    private Boolean _linkShared;
    @Argument("-pkgdir")
    private File _pkgDir;
    @Argument("-tags")
    private String _tags;
    @Argument("-toolexec")
    private String _toolexec;

    private String _outputFilenamePattern;
    private Map<String, String> _definitions;

    @Inject
    public BuildSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            final String gopath = System.getenv("GOPATH");
            if (isNotEmpty(gopath)) {
                _gopath = new File(gopath);
            }
            _outputFilenamePattern = project.getBuildDir() + File.separator + "out" + File.separator + project.getProjectDir().getName() + "-%{platform}%{extension}";
            _excludes = new String[]{
                ".git/**", ".svn/**", "build.gradle", "build/**", ".gradle/**", "gradle/**"
            };
        }
    }

    public File getGopath() {
        return _gopath;
    }

    public void setGopath(File gopath) {
        _gopath = gopath;
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

    public Boolean getForceRebuild() {
        return _forceRebuild;
    }

    public void setForceRebuild(Boolean forceRebuild) {
        _forceRebuild = forceRebuild;
    }

    public Integer getParallelRuns() {
        return _parallelRuns;
    }

    public void setParallelRuns(Integer parallelRuns) {
        _parallelRuns = parallelRuns;
    }

    public Boolean getRaceDetection() {
        return _raceDetection;
    }

    public void setRaceDetection(Boolean raceDetection) {
        _raceDetection = raceDetection;
    }

    public Boolean getInteroperationWithMemorySanitizer() {
        return _interoperationWithMemorySanitizer;
    }

    public void setInteroperationWithMemorySanitizer(Boolean interoperationWithMemorySanitizer) {
        _interoperationWithMemorySanitizer = interoperationWithMemorySanitizer;
    }

    public Boolean getPrintCompiledPackages() {
        return _printCompiledPackages;
    }

    public void setPrintCompiledPackages(Boolean printCompiledPackages) {
        _printCompiledPackages = printCompiledPackages;
    }

    public Boolean getPrintWorkDirectory() {
        return _printWorkDirectory;
    }

    public void setPrintWorkDirectory(Boolean printWorkDirectory) {
        _printWorkDirectory = printWorkDirectory;
    }

    public Boolean getPrintCommands() {
        return _printCommands;
    }

    public void setPrintCommands(Boolean printCommands) {
        _printCommands = printCommands;
    }

    public String getAsmFlags() {
        return _asmFlags;
    }

    public void setAsmFlags(String asmFlags) {
        _asmFlags = asmFlags;
    }

    public String getBuildmode() {
        return _buildmode;
    }

    public void setBuildmode(String buildmode) {
        _buildmode = buildmode;
    }

    public String getCompiler() {
        return _compiler;
    }

    public void setCompiler(String compiler) {
        _compiler = compiler;
    }

    public String getGccgoFlags() {
        return _gccgoFlags;
    }

    public void setGccgoFlags(String gccgoFlags) {
        _gccgoFlags = gccgoFlags;
    }

    public String getGcFlags() {
        return _gcFlags;
    }

    public void setGcFlags(String gcFlags) {
        _gcFlags = gcFlags;
    }

    public String getLdFlags() {
        return _ldFlags;
    }

    public void setLdFlags(String ldFlags) {
        _ldFlags = ldFlags;
    }

    public Boolean getLinkShared() {
        return _linkShared;
    }

    public void setLinkShared(Boolean linkShared) {
        _linkShared = linkShared;
    }

    public File getPkgDir() {
        return _pkgDir;
    }

    public void setPkgDir(File pkgDir) {
        _pkgDir = pkgDir;
    }

    public String getTags() {
        return _tags;
    }

    public void setTags(String tags) {
        _tags = tags;
    }

    public String getToolexec() {
        return _toolexec;
    }

    public void setToolexec(String toolexec) {
        _toolexec = toolexec;
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

    @Nonnull
    public File outputFilenameFor(@Nonnull Platform platform) {
        return new File(replacePlaceholdersFor(platform, getOutputFilenamePattern()));
    }

    @Nonnull
    public String ldflagsWithDefinitions() {
        final StringBuilder sb = new StringBuilder();
        final String ldFlags = getLdFlags();
        if (isNotEmpty(ldFlags)) {
            sb.append(ldFlags);
        }
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
                if (field.getType().equals(Boolean.class)) {
                    if (plainValue != null && (Boolean) plainValue) {
                        result.put(argument.value(), null);
                    }
                } else if (field.getType().equals(Integer.class)) {
                    if (plainValue != null) {
                        result.put(argument.value(), plainValue.toString());
                    }
                } else if (field.getType().equals(String.class)) {
                    if (isNotEmpty((String) plainValue)) {
                        result.put(argument.value(), plainValue.toString());
                    }
                } else if (plainValue != null) {
                    throw new IllegalArgumentException("The type of field " + field + " is currently not supported.");
                }
            }
        }
        return result;
    }

    @Nonnull
    public BuildSettings merge(@Nonnull BuildSettings with) {
        final BuildSettings result = new BuildSettings(false, _project);
        BeanUtils.copyNonNulls(BuildSettings.class, this, result);
        BeanUtils.copyNonNulls(BuildSettings.class, with, result);
        return result;
    }

}
