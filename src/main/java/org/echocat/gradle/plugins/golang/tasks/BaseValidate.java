package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.model.*;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.isExecutable;
import static org.apache.commons.lang3.StringUtils.*;
import static org.echocat.gradle.plugins.golang.model.Platform.currentPlatform;

public class BaseValidate extends GolangTaskSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseValidate.class);

    public BaseValidate() {
        setGroup("verification");
        setDescription("Validate the whole Golang setup and the project and resolve missing properties (if required) for base artifacts.");
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @Override
    public void run() {
        final Settings settings = getGlobalSettings();
        final GolangSettings golang = settings.getGolang();
        final ToolchainSettings toolchain = settings.getToolchain();
        final BuildSettings build = settings.getBuild();

        final ProgressLogger progress = startProgress("Validate");
        progress.progress("Check package name...");
        if (isEmpty(golang.getPackageName())) {
            final Object group = getProject().getGroup();
            if (group != null) {
                golang.setPackageName(group.toString());
            }
            if (isEmpty(golang.getPackageName())) {
                throw new IllegalArgumentException("There is no package name configured. (property: 'group' or 'golang.packageName')");
            }
        }

        final List<Platform> platforms = golang.getParsedPlatforms();

        progress.progress("Configure host platform...");
        final Platform hostPlatform = currentPlatform();
        golang.setHostPlatform(hostPlatform);

        progress.progress("Configure GOROOT...");
        configureGorootIfNeeded();
        progress.progress("Configure GOROOT_BOOTSTRAP...");
        configureBootstrapGorootIfNeeded();
        progress.progress("Configure GOPATH...");
        configureGopathIfNeeded();

        progress.completed();

        LOGGER.info("Package:          {}", golang.getPackageName());
        LOGGER.info("Platforms:        {}", join(platforms, ", "));
        LOGGER.info("Host:             {}", hostPlatform);
        LOGGER.info("Go version:       {}", toolchain.getGoversion());
        LOGGER.info("GOROOT:           {}", toolchain.getGoroot());
        LOGGER.info("GOROOT_BOOTSTRAP: {}", toolchain.getBootstrapGoroot());
        LOGGER.info("GOPATH:           {}", build.getGopath());
    }

    protected void configureGorootIfNeeded() {
        final Settings settings = getGlobalSettings();
        final GolangSettings golang = settings.getGolang();
        final ToolchainSettings toolchain = settings.getToolchain();
        final Path goroot = toolchain.getGoroot();
        if (goroot == null) {
            toolchain.setGoroot(golang.getCacheRoot().resolve("sdk").resolve(toolchain.getGoversion()));
        }
    }

    protected void configureBootstrapGorootIfNeeded() {
        final Settings settings = getGlobalSettings();
        final GolangSettings golang = settings.getGolang();
        final ToolchainSettings toolchain = settings.getToolchain();
        final Path bootstrapGoroot = toolchain.getBootstrapGoroot();
        if (bootstrapGoroot == null) {
            final String gorootEnv = System.getenv("GOROOT");
            if (isNotEmpty(gorootEnv)) {
                final Path goBinary = Paths.get(gorootEnv).resolve("bin").resolve("go" + toolchain.getExecutableSuffix());
                if (isExecutable(goBinary)) {
                    toolchain.setBootstrapGoroot(Paths.get(gorootEnv));
                    return;
                }
            }
            toolchain.setBootstrapGoroot(golang.getCacheRoot().resolve("sdk").resolve("bootstrap"));
        }
    }

    protected void configureGopathIfNeeded() {
        final BuildSettings build = getGlobalSettings().getBuild();
        if (TRUE.equals(build.getUseTemporaryGopath())) {
            build.setGopath(getProject().getBuildDir().toPath().resolve("gopath"));
        }
    }

}
