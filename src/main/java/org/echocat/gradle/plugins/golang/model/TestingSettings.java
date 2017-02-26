package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.nio.file.Path;

import static org.echocat.gradle.plugins.golang.utils.FileUtils.toPath;

public class TestingSettings {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @Nonnull
    private final Project _project;

    private Boolean _skip;

    private String[] _packages;
    private String[] _includes;
    private String[] _excludes;
    private String[] _testArguments;
    private String[] _arguments;

    /**
     * Write a coverage profile to the file after all tests have passed.
     * Sets -cover.
     */
    private Path _coverProfile;
    /**
     * Write a coverage profile as HTML to the file after all tests have passed.
     * Sets -cover.
     */
    private Path _coverProfileHtml;

    /**
     * If set to an non <code>null</code> value the test output will
     * will be stored in the specified file location.
     *
     * Default is: ${buildDir}/testing/test.log
     */
    private Path _log;

    /**
     * If set to an non <code>null</code> value the test output will
     * transformed into a junit report and will be written to this file location.
     *
     * Default is: ${buildDir}/testing/junit_report.xml
     */
    private Path _junitReport;

    @Inject
    public TestingSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _includes = new String[]{
                "**/*_test.go"
            };
            _excludes = new String[]{
                ".git/**", ".svn/**", "build.gradle", "build/**", ".gradle/**", "gradle/**", "vendor/**"
            };
            _log = project.getBuildDir().toPath().resolve("testing").resolve("test.log");
            _junitReport = project.getBuildDir().toPath().resolve("testing").resolve("junit_report.xml");
        }
    }

    public Boolean getSkip() {
        return _skip;
    }

    public void setSkip(Boolean skip) {
        _skip = skip;
    }

    public String[] getPackages() {
        return _packages;
    }

    public void setPackages(String[] packages) {
        _packages = packages;
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

    public String[] getTestArguments() {
        return _testArguments;
    }

    public void setTestArguments(String[] testArguments) {
        _testArguments = testArguments;
    }

    public Path getCoverProfile() {
        return _coverProfile;
    }

    public void setCoverProfile(Path coverProfile) {
        _coverProfile = coverProfile;
    }

    public void setCoverProfile(String coverProfile) {
        setCoverProfile(toPath(coverProfile));
    }

    public Path getCoverProfileHtml() {
        return _coverProfileHtml;
    }

    public void setCoverProfileHtml(Path coverProfileHtml) {
        _coverProfileHtml = coverProfileHtml;
    }

    public void setCoverProfileHtml(String coverProfileHtml) {
        setCoverProfileHtml(toPath(coverProfileHtml));
    }

    public Path getLog() {
        return _log;
    }

    public void setLog(Path log) {
        _log = log;
    }

    public void setLog(String log) {
        setLog(toPath(log));
    }

    public Path getJunitReport() {
        return _junitReport;
    }

    public void setJunitReport(Path junitReport) {
        _junitReport = junitReport;
    }

    public void setJunitReport(String junitReport) {
        setJunitReport(toPath(junitReport));
    }

    public String[] getArguments() {
        return _arguments;
    }

    public void setArguments(String[] arguments) {
        _arguments = arguments;
    }

}
