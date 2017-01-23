package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.testing.CoverageReportOptimizer;
import org.echocat.gradle.plugins.golang.testing.report.GolangTestOutputBasedReportObserver;
import org.echocat.gradle.plugins.golang.testing.report.ReportObserver;
import org.echocat.gradle.plugins.golang.testing.report.ReportObserver.Notifier;
import org.echocat.gradle.plugins.golang.testing.report.ReportTransformer;
import org.echocat.gradle.plugins.golang.testing.report.junit.TestSuites;
import org.echocat.gradle.plugins.golang.utils.Executor;
import org.echocat.gradle.plugins.golang.utils.Executor.ExecutionFailedExceptionProducer;
import org.echocat.gradle.plugins.golang.utils.StdStreams;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.echocat.gradle.plugins.golang.DependencyHandler.GetTask.by;
import static org.echocat.gradle.plugins.golang.model.GolangDependency.Type.source;
import static org.echocat.gradle.plugins.golang.model.GolangDependency.newDependency;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.*;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.delete;
import static org.echocat.gradle.plugins.golang.utils.IOUtils.closeQuietly;
import static org.echocat.gradle.plugins.golang.utils.IOUtils.tee;
import static org.echocat.gradle.plugins.golang.utils.StdStreams.Impl.stdStreams;
import static org.gradle.api.internal.tasks.TaskExecutionOutcome.SKIPPED;

public class TestTask extends GolangTaskSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestTask.class);

    private static final ExecutionFailedExceptionProducer<AtLeastOneTestFailedException> EXCEPTION_PRODUCER = new ExecutionFailedExceptionProducer<AtLeastOneTestFailedException>() {
        @Nonnull
        @Override
        public AtLeastOneTestFailedException produceFor(@Nonnull Executor executor, @Nonnull String[] commandLine, int errorCode) {
            return new AtLeastOneTestFailedException();
        }
    };

    protected static class AtLeastOneTestFailedException extends RuntimeException {
        public AtLeastOneTestFailedException() {}
    }

    public TestTask() {
        setGroup("support");
        dependsOn(
            "validate",
            "prepareToolchain",
            "prepareSources",
            "getTools"
        );
    }

    @Override
    public void run() throws Exception {
        final CoverageReportOptimizer coverageReportOptimizer = new CoverageReportOptimizer(this);
        final TestingSettings testing = getTesting();
        if (Boolean.TRUE.equals(testing.getSkip()) || "true".equalsIgnoreCase(System.getProperty("skipTests"))) {
            getState().setOutcome(SKIPPED);
            return;
        }
        final Path coverProfile = coverageReportOptimizer.preHandlePackagesCover();

        final Set<GolangDependency> packages = selectPackages();
        if (packages.isEmpty()) {
            getState().setOutcome(SKIPPED);
            return;
        }

        getDependencyHandler().get(by("testGolang")
            .withAdditionalConfigurations("buildGolang")
            .withAdditionalRequiredPackages(packages)
        );

        final ProgressLogger progress = startProgress("Run tests");
        final ObserverNotifier notifier = new ObserverNotifier(progress);

        boolean success = true;
        try (final ReportObserver observer = observerFor(getGolang().getPackageName(), notifier)) {
            try (final StdStreams streams = wrapIfRequired(observer)) {
                for (final GolangDependency targetPackage : packages) {
                    notifier.packageName(targetPackage.getGroup());
                    progress.progress("Run " + targetPackage + "...");
                    if (!executeTestsFor(targetPackage, coverProfile, streams)) {
                        success = false;
                    }
                }
            }
            coverageReportOptimizer.postHandlePackagesCover(coverProfile, progress);
            observer.close();
            storeAsJunitReportIfRequired(observer);
        }

        progress.completed();
        if (!success) {
            throw new RuntimeException("At least one test failed (see log).");
        }
    }

    @Nonnull
    protected StdStreams wrapIfRequired(@Nonnull ReportObserver observer) throws IOException {
        final Path logPath = getTesting().getLogPath();
        if (logPath == null) {
            return observer;
        }
        ensureParentOf(logPath);
        final OutputStream logOutputStream = newOutputStream(logPath);
        boolean success = false;
        try {
            final StdStreams logStreams = stdStreams(logOutputStream);
            final StdStreams result = tee(observer, logStreams);
            success = true;
            return result;
        } finally {
            if (!success) {
                closeQuietly(logOutputStream);
            }
        }
    }

    protected void storeAsJunitReportIfRequired(@Nonnull ReportObserver observer) throws IOException {
        final Path path = getTesting().getJunitReportPath();
        if (path != null) {
            final TestSuites junitReport = new ReportTransformer().transformToJunit(observer.getReport());
            ensureParentOf(path);
            try (final OutputStream os = newOutputStream(path)) {
                try (final Writer writer = new OutputStreamWriter(os, "UTF-8")) {
                    junitReport.marshall(writer);
                }
            }
        }
    }

    protected boolean executeTestsFor(@Nonnull GolangDependency aPackage, @Nullable Path coverProfile, @Nonnull StdStreams streams) throws Exception {
        LOGGER.info("TestingGolang {}...", aPackage);

        final GolangSettings settings = getGolang();
        final BuildSettings build = getBuild();
        final ToolchainSettings toolchain = getToolchain();
        final TestingSettings testing = getTesting();

        final Platform platform = settings.getHostPlatform();

        final Path junitReportPath = testing.getJunitReportPath();

        final Executor executor = executor(toolchain.getGoBinary(), streams)
            .workingDirectory(build.getFirstGopath())
            .env("GOPATH", build.getGopathAsString())
            .env("GOROOT", toolchain.getGoroot())
            .env("GOOS", platform.getOperatingSystem().getNameInGo())
            .env("GOARCH", platform.getArchitecture().getNameInGo())
            .env("CGO_ENABLED", TRUE.equals(toolchain.getCgoEnabled()) ? "1" : "0");

        executor.arguments("testGolang", "-v");
        executor.arguments((Object[])testing.getArguments());

        final Path packageCoverProfile;
        if (coverProfile != null) {
            final Path testingDir = getProject().getBuildDir().toPath().resolve("testingGolang");
            createDirectoriesIfRequired(testingDir);
            packageCoverProfile = createTempFile(testingDir, getProject().getName() + ".", ".cover");
            executor.arguments("-coverprofile", packageCoverProfile);
        } else {
            packageCoverProfile = null;
        }

        executor.argument(aPackage.getGroup());
        executor.arguments((Object[])testing.getTestArguments());

        boolean success;
        try {
            executor.execute(EXCEPTION_PRODUCER);
            success = true;
        } catch (final AtLeastOneTestFailedException ignored) {
            success = false;
        }

        if (coverProfile != null && isRegularFile(packageCoverProfile)) {
            try (final InputStream is = newInputStream(packageCoverProfile)) {
                try (final Reader reader = new InputStreamReader(is, "UTF-8")) {
                    try (final BufferedReader br = new BufferedReader(reader)) {
                        ensureParentOf(coverProfile);
                        try (final OutputStream os = newOutputStream(coverProfile, CREATE, APPEND)) {
                            try (final OutputStreamWriter writer = new OutputStreamWriter(os)) {
                                final String firstLine = br.readLine();
                                if (firstLine != null) {
                                    if (size(coverProfile) <= 0) {
                                        writer.write(firstLine);
                                        writer.write('\n');
                                    }
                                    IOUtils.copy(br, writer);
                                }
                            }
                        }
                    }
                }
            }
            try {
                delete(packageCoverProfile);
            } catch (final IOException ignored) {}
        }

        //noinspection UseOfSystemOutOrSystemErr
        System.out.println(aPackage.getGroup() + " tested.");
        return success;
    }

    @Nonnull
    protected ReportObserver observerFor(@Nonnull String packageName, ObserverNotifier notifier) {
        final ReportObserver result = new GolangTestOutputBasedReportObserver(packageName);
        result.registerNotifier(notifier);
        return result;
    }

    @Nonnull
    protected Set<GolangDependency> selectPackages() throws Exception {
        final BuildSettings build = getBuild();
        final TestingSettings testing = getTesting();
        final Set<GolangDependency> result = new TreeSet<>();
        final String[] explicitSelected = testing.getPackages();
        if (explicitSelected != null && explicitSelected.length > 0) {
            for (final String explicit : explicitSelected) {
                for (final Path gopath : build.getGopathSourceRoot()) {
                    result.add(newDependency(explicit)
                        .setType(source)
                        .setLocation(gopath.resolve(explicit))
                    );
                }
            }
        } else {
            final GolangSettings golang = getGolang();
            final Path projectBasedir = golang.getProjectBasedir();

            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(projectBasedir.toFile());
            scanner.setIncludes(testing.getIncludes());
            scanner.setExcludes(testing.getExcludes());
            scanner.scan();
            final String base = golang.getPackageName();
            for (final String file : scanner.getIncludedFiles()) {
                final String path = FilenameUtils.getPathNoEndSeparator(file).replace(File.separatorChar, '/');
                final String packageName = isNotEmpty(path) ? base + "/" + path : base;
                for (final Path gopath : build.getGopathSourceRoot()) {
                    result.add(newDependency(packageName)
                        .setType(source)
                        .setLocation(gopath.resolve(packageName))
                    );
                }
            }
            for (final String directory : scanner.getIncludedDirectories()) {
                final String packageName = isNotEmpty(directory) ? base + "/" + directory.replace(File.separatorChar, '/') : base;
                for (final Path gopath : build.getGopathSourceRoot()) {
                    result.add(newDependency(packageName)
                        .setType(source)
                        .setLocation(gopath.resolve(packageName))
                    );
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Packages to be tested:");
            for (final GolangDependency candidate : result) {
                LOGGER.debug("* {}", candidate.getGroup());
            }
        }
        return result;
    }

    protected static class ObserverNotifier implements Notifier {

        @Nonnull
        private final ProgressLogger _progressLogger;
        private String _packageName;

        public ObserverNotifier(@Nonnull ProgressLogger progressLogger) {
            _progressLogger = progressLogger;
        }

        @Override
        public void onTestStarted(@Nonnull String name) {
            _progressLogger.progress("Run " + _packageName + "::" + name + "...");
        }

        public void packageName(@Nonnull String packageName) {
            _packageName = packageName;
        }
    }

}
