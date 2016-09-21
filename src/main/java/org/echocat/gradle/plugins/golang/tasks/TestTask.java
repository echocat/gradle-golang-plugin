package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.utils.Executor;
import org.echocat.gradle.plugins.golang.utils.Executor.ExecutionFailedExceptionProducer;
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
        final TestingSettings testing = getTesting();
        if (Boolean.TRUE.equals(testing.getSkip()) || "true".equalsIgnoreCase(System.getProperty("skipTests"))) {
            getState().skipped("SKIPPED");
            return;
        }
        final Path coverProfile = preHandlePackagesCover();

        final Set<GolangDependency> packages = selectPackages();
        if (packages.isEmpty()) {
            getState().skipped("NO-TESTS-FOUND");
            return;
        }

        getDependencyHandler().get(by("test")
            .withAdditionalConfigurations("build")
            .withAdditionalRequiredPackages(packages)
        );

        final ProgressLogger progress = startProgress("Run tests");

        boolean success = true;
        for (final GolangDependency targetPackage : packages) {
            if (!executeTestsFor(targetPackage, coverProfile, progress)) {
                success = false;
            }
        }

        postHandlePackagesCover(coverProfile, progress);

        progress.completed();
        if (!success) {
            throw new RuntimeException("At least one test failed (see log).");
        }
    }

    @Nullable
    protected Path preHandlePackagesCover() throws IOException {
        final TestingSettings testing = getTesting();
        Path coverProfile = testing.getCoverProfileFile();
        if (coverProfile != null && exists(coverProfile)) {
            delete(coverProfile);
        }
        final Path coverProfileHtml = testing.getCoverProfileHtmlFile();
        if (coverProfileHtml != null && coverProfile == null) {
            final Path testingDir = getProject().getBuildDir().toPath().resolve("testing");
            createDirectoriesIfRequired(testingDir);
            coverProfile = createTempFile(testingDir, getProject().getName() + ".", ".cover");
        }
        return coverProfile;
    }

    protected void postHandlePackagesCover(@Nullable Path coverProfile, @Nonnull ProgressLogger progress) throws Exception {
        progress.progress("Post process of covering profiles...");
        final TestingSettings testing = getTesting();
        final Path coverProfileHtml = testing.getCoverProfileHtmlFile();
        if (coverProfileHtml != null && coverProfile != null && exists(coverProfile)) {
            coverToHtml(coverProfile, coverProfileHtml);
            if (testing.getCoverProfileFile() == null) {
                deleteQuietly(coverProfile);
            }
        }
    }

    protected boolean executeTestsFor(@Nonnull GolangDependency aPackage, @Nullable Path coverProfile, @Nonnull ProgressLogger progress) throws Exception {
        progress.progress("Testing " + aPackage + "...");
        LOGGER.info("Testing {}...", aPackage);

        final GolangSettings settings = getGolang();
        final BuildSettings build = getBuild();
        final ToolchainSettings toolchain = getToolchain();
        final TestingSettings testing = getTesting();

        final Platform platform = settings.getHostPlatform();

        final Executor executor = executor(toolchain.getGoBinary())
            .workingDirectory(build.getGopath())
            .env("GOPATH", build.getGopath())
            .env("GOROOT", toolchain.getGoroot())
            .env("GOOS", platform.getOperatingSystem().getNameInGo())
            .env("GOARCH", platform.getArchitecture().getNameInGo())
            .env("CGO_ENABLED", TRUE.equals(toolchain.getCgoEnabled()) ? "1" : "0");

        executor.argument("test");
        executor.arguments((Object[])testing.getArguments());

        final Path packageCoverProfile;
        if (coverProfile != null) {
            final Path testingDir = getProject().getBuildDir().toPath().resolve("testing");
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

    protected void coverToHtml(@Nonnull Path profile, @Nonnull Path output) throws Exception {
        final BuildSettings build = getBuild();
        final ToolchainSettings toolchain = getToolchain();

        executor(toolchain.getGoBinary())
            .workingDirectory(build.getGopath())
            .env("GOPATH", build.getGopath())
            .env("GOROOT", toolchain.getGoroot())
            .arguments("tool", "cover")
            .arguments("-html", profile)
            .arguments("-o", output)
            .execute();
    }

    @Nonnull
    protected Set<GolangDependency> selectPackages() throws Exception {
        final BuildSettings build = getBuild();
        final TestingSettings testing = getTesting();
        final Set<GolangDependency> result = new TreeSet<>();
        final String[] explicitSelected = testing.getPackages();
        if (explicitSelected != null && explicitSelected.length > 0) {
            for (final String explicit : explicitSelected) {
                result.add(newDependency(explicit)
                    .setType(source)
                    .setLocation(build.getGopathSourceRoot().resolve(explicit))
                );
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
                result.add(newDependency(packageName)
                    .setType(source)
                    .setLocation(build.getGopathSourceRoot().resolve(packageName))
                );
            }
            for (final String directory : scanner.getIncludedDirectories()) {
                final String packageName = isNotEmpty(directory) ? base + "/" + directory.replace(File.separatorChar, '/') : base;
                result.add(newDependency(packageName)
                    .setType(source)
                    .setLocation(build.getGopathSourceRoot().resolve(packageName))
                );
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

}

