package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.lang3.StringUtils;
import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.GolangSettings;
import org.echocat.gradle.plugins.golang.model.Platform;
import org.echocat.gradle.plugins.golang.model.ToolchainSettings;
import org.echocat.gradle.plugins.golang.utils.Executor;
import org.echocat.gradle.plugins.golang.utils.Executor.ExecutionFailedExceptionProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class Build extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Build.class);

    private static final ExecutionFailedExceptionProducer<RuntimeException> EXCEPTION_PRODUCER = new ExecutionFailedExceptionProducer<RuntimeException>() {
        @Nonnull
        @Override
        public RuntimeException produceFor(@Nonnull Executor executor, @Nonnull String[] commandLine, int errorCode) {
            return new RuntimeException("Build failed (see log).");
        }
    };

    public Build() {
        setGroup("build");
        dependsOn(
            "validate",
            "prepare-toolchain",
            "prepare-sources",
            "test",
            "get"
        );
    }

    @Override
    public void run() throws Exception {
        for (final Platform platform : getGolang().getParsedPlatforms()) {
            executeFor(platform);
        }
    }

    protected void executeFor(@Nonnull Platform platform) throws Exception {
        final GolangSettings settings = getGolang();
        final ToolchainSettings toolchain = getToolchain();
        final BuildSettings build = getBuild();

        final Path expectedPackagePath = settings.packagePathFor(build.getGopath()).toPath();
        final Path projectBasedir = settings.getProjectBasedir().toPath();
        if (!expectedPackagePath.startsWith(projectBasedir)) {
            throw new IllegalStateException("Project '" + settings.getPackageName() + "' is not part of GOPATH (" + build.getGopath() + "). Current location: " + projectBasedir);
        }

        final File outputFilename = build.outputFilenameFor(platform);
        LOGGER.debug("Building {}...", outputFilename);

        final Executor executor = Executor.executor()
            .executable(toolchain.getGoBinary())
            .workingDirectory(build.getGopath())
            .env("GOPATH", build.getGopath())
            .env("GOROOT", toolchain.getGoroot())
            .env("GOOS", platform.getOperatingSystem().getNameInGo())
            .env("GOARCH", platform.getArchitecture().getNameInGo())
            .env("CGO_ENABLED", TRUE.equals(toolchain.getCgoEnabled()) ? "1" : "0");

        executor.arguments("build");
        executor.arguments("-o", outputFilename.toString());
        for (final Map.Entry<String, String> argument : build.additionalArgumentMap().entrySet()) {
            executor.argument(argument.getKey());
            if (argument.getValue() != null) {
                executor.argument(argument.getValue());
            }
        }

        final String ldFlags = build.ldflagsWithDefinitions();
        if (isNotEmpty(ldFlags)) {
            executor.arguments("-ldflags", ldFlags);
        }

        executor.argument(settings.getPackageName());

        executor.execute(EXCEPTION_PRODUCER);

        final String stdoutAsString = executor.getStdoutAsString();
        if (isNotEmpty(stdoutAsString)) {
            for (final String line : StringUtils.split(stdoutAsString, "\n")) {
                LOGGER.info(line);
            }
        }

        //projectHelper().attachArtifact(project(), platformExtensionFor(platform), platform.getNameInGo(), outputFilename);

        LOGGER.info("{} build.", outputFilename);
    }

}

