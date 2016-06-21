package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.utils.Executor;
import org.echocat.gradle.plugins.golang.utils.Executor.ExecutionFailedExceptionProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.io.File.createTempFile;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class Test extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

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

    public Test() {
        setGroup("build");
        dependsOn(
            "validate",
            "prepare-toolchain",
            "prepare-sources",
            "get"
        );
    }

    @Override
    public void run() throws Exception {
        final TestingSettings testing = getTesting();
        if (testing.isSkip()) {
            getState().skipped("SKIPPED");
            return;
        }
        final File coverProfile = preHandlePackagesCover();

        final Set<String> packages = selectPackages();
        if (packages.isEmpty()) {
            getState().skipped("NO-TESTS-FOUND");
            return;
        }

        boolean success = true;
        for (final String aPackage : packages) {
            if (!executeTestsFor(aPackage, coverProfile)) {
                success = false;
            }
        }

        postHandlePackagesCover(coverProfile);

        if (!success) {
            throw new RuntimeException("At least one test failed (see log).");
        }
    }

    @Nullable
    protected File preHandlePackagesCover() throws IOException {
        final TestingSettings testing = getTesting();
        File coverProfile = testing.getCoverProfileFile();
        if (coverProfile != null && coverProfile.exists()) {
            forceDelete(coverProfile);
        }
        final File coverProfileHtml = testing.getCoverProfileHtmlFile();
        if (coverProfileHtml != null && coverProfile == null) {
            final File testingDir = new File(getProject().getBuildDir(), "testing");
            forceMkdir(testingDir);
            coverProfile = createTempFile(getProject().getName(), "cover", testingDir);
        }
        return coverProfile;
    }

    protected void postHandlePackagesCover(@Nullable File coverProfile) throws Exception {
        final TestingSettings testing = getTesting();
        final File coverProfileHtml = testing.getCoverProfileHtmlFile();
        if (coverProfileHtml != null && coverProfile != null && coverProfile.exists()) {
            coverToHtml(coverProfile, coverProfileHtml);
            if (testing.getCoverProfileFile() == null) {
                //noinspection ResultOfMethodCallIgnored
                coverProfile.delete();
            }
        }
    }

    protected boolean executeTestsFor(@Nonnull String aPackage, @Nullable File coverProfile) throws Exception {
        LOGGER.debug("Testing {}...", aPackage);

        final GolangSettings settings = getGolang();
        final BuildSettings build = getBuild();
        final ToolchainSettings toolchain = getToolchain();
        final TestingSettings testing = getTesting();

        final Platform platform = settings.getHostPlatform();

        final Executor executor = Executor.executor()
            .executable(toolchain.getGoBinary())
            .workingDirectory(build.getGopath())
            .env("GOPATH", build.getGopath())
            .env("GOROOT", toolchain.getGoroot())
            .env("GOOS", platform.getOperatingSystem().getNameInGo())
            .env("GOARCH", platform.getArchitecture().getNameInGo())
            .env("CGO_ENABLED", TRUE.equals(toolchain.getCgoEnabled()) ? "1" : "0");

        executor.arguments("test");
        for (final Map.Entry<String, String> argument : testing.additionalArgumentMap().entrySet()) {
            executor.argument(argument.getKey());
            if (argument.getValue() != null) {
                executor.argument(argument.getValue());
            }
        }

        final File packageCoverProfile;
        if (coverProfile != null) {
            final File testingDir = new File(getProject().getBuildDir(), "testing");
            forceMkdir(testingDir);
            packageCoverProfile = createTempFile(getProject().getName(), "cover", testingDir);
            executor.arguments("-coverprofile", packageCoverProfile.getPath());
        } else {
            packageCoverProfile = null;
        }

        executor.argument(aPackage);
        executor.arguments(testing.getArguments());

        boolean success;
        try {
            executor.execute(EXCEPTION_PRODUCER);
            success = true;
        } catch (final AtLeastOneTestFailedException ignored) {
            success = false;
        }

        if (coverProfile != null && packageCoverProfile.isFile()) {
            try (final InputStream is = new FileInputStream(packageCoverProfile)) {
                try (final Reader reader = new InputStreamReader(is, "UTF-8")) {
                    try (final BufferedReader br = new BufferedReader(reader)) {
                        try (final OutputStream os = new FileOutputStream(coverProfile, true)) {
                            try (final OutputStreamWriter writer = new OutputStreamWriter(os)) {
                                final String firstLine = br.readLine();
                                if (firstLine != null) {
                                    if (coverProfile.length() <= 0) {
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
            //noinspection ResultOfMethodCallIgnored
            packageCoverProfile.delete();
        }

        LOGGER.info("{} tested.", aPackage);
        return success;
    }

    protected void coverToHtml(@Nonnull File profile, @Nonnull File output) throws Exception {
        final BuildSettings build = getBuild();
        final ToolchainSettings toolchain = getToolchain();

        Executor.executor()
            .executable(toolchain.getGoBinary())
            .workingDirectory(build.getGopath())
            .env("GOPATH", build.getGopath())
            .env("GOROOT", toolchain.getGoroot())
            .arguments("tool", "cover")
            .arguments("-html", profile.getPath())
            .arguments("-o", output.getPath())
            .execute();
    }

    @Nonnull
    protected Set<String> selectPackages() throws Exception {
        final TestingSettings testing = getTesting();
        final Set<String> result = new TreeSet<>();
        final String[] explicitSelected = testing.getPackages();
        if (explicitSelected != null && explicitSelected.length > 0) {
            Collections.addAll(result, explicitSelected);
        } else {
            final GolangSettings golang = getGolang();
            final File projectBasedir = golang.getProjectBasedir();

            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(projectBasedir);
            scanner.setIncludes(testing.getIncludes());
            scanner.setExcludes(testing.getExcludes());
            scanner.scan();
            final String base = golang.getPackageName();
            for (final String file : scanner.getIncludedFiles()) {
                final String path = FilenameUtils.getPathNoEndSeparator(file).replace(File.separatorChar, '/');
                if (isNotEmpty(path)) {
                    result.add(base + "/" + path);
                } else {
                    result.add(base);
                }
            }
            for (final String directory : scanner.getIncludedDirectories()) {
                if (isNotEmpty(directory)) {
                    result.add(base + "/" + directory.replace(File.separatorChar, '/'));
                } else {
                    result.add(base);
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Packages to be tested:");
            for (final String candidate : result) {
                LOGGER.debug("* {}", candidate);
            }
        }
        return result;
    }

}

