package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.isExecutable;
import static org.apache.commons.lang3.StringUtils.*;
import static org.echocat.gradle.plugins.golang.model.Platform.currentPlatform;

public class Validate extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validate.class);

    public Validate() {
        setGroup("build");
    }

    @Override
    public void run() {
        final Settings settings = getGlobalSettings();
        final GolangSettings golang = settings.getGolang();
        final ToolchainSettings toolchain = settings.getToolchain();
        final BuildSettings build = settings.getBuild();

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

        final Platform hostPlatform = currentPlatform();
        golang.setHostPlatform(hostPlatform);

        configureGorootIfNeeded();
        configureBootstrapGorootIfNeeded();
        configureGopathIfNeeded();

        LOGGER.info("Package:    {}", golang.getPackageName());
        LOGGER.info("Platforms:  {}", join(platforms, ", "));
        LOGGER.info("Host:       {}", hostPlatform);
        LOGGER.info("Go version: {}", toolchain.getGoversion());
        LOGGER.info("GOROOT:     {}", toolchain.getGoroot() + " (GOROOT_BOOTSTRAP: " + toolchain.getBootstrapGoroot() + ")");
        LOGGER.info("GOPATH:     {}", build.getGopath());
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
