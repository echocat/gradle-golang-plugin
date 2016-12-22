package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestingSettings {

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
    private String _coverProfile;
    /**
     * Write a coverage profile as HTML to the file after all tests have passed.
     * Sets -cover.
     */
    private String _coverProfileHtml;

    /**
     * If set to an non <code>null</code> value the test output will
     * will be stored in the specified file location.
     *
     * Default is: ${buildDir}/testing/test.log
     */
    private String _log;

    /**
     * If set to an non <code>null</code> value the test output will
     * transformed into a junit report and will be written to this file location.
     *
     * Default is: ${buildDir}/testing/junit_report.xml
     */
    private String _junitReport;

    @Inject
    public TestingSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _includes = new String[] {
                "**/*_test.go"
            };
            _excludes = new String[]{
                ".git/**", ".svn/**", "build.gradle", "build/**", ".gradle/**", "gradle/**", "vendor/**"
            };
            _log = project.getBuildDir() + "/testing/test.log";
            _junitReport = project.getBuildDir() + "/testing/junit_report.xml";
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

    public String getCoverProfile() {
        return _coverProfile;
    }

    public void setCoverProfile(String coverProfile) {
        _coverProfile = coverProfile;
    }

    public Path getCoverProfileFile() {
        final String plain = _coverProfile;
        return plain != null ? Paths.get(plain) : null;
    }

    public String getCoverProfileHtml() {
        return _coverProfileHtml;
    }

    public void setCoverProfileHtml(String coverProfileHtml) {
        _coverProfileHtml = coverProfileHtml;
    }

    public Path getCoverProfileHtmlFile() {
        final String plain = _coverProfileHtml;
        return plain != null ? Paths.get(plain) : null;
    }

    public String getLog() {
        return _log;
    }

    public void setLog(String log) {
        _log = log;
    }

    public Path getLogPath() {
        final String plain = _log;
        return plain != null ? Paths.get(plain) : null;
    }

    public String getJunitReport() {
        return _junitReport;
    }

    public void setJunitReport(String junitReport) {
        _junitReport = junitReport;
    }

    public Path getJunitReportPath() {
        final String plain = _junitReport;
        return plain != null ? Paths.get(plain) : null;
    }

    public String[] getArguments() {
        return _arguments;
    }

    public void setArguments(String[] arguments) {
        _arguments = arguments;
    }

}
