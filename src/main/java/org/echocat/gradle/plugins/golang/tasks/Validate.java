package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.GolangSettings;
import org.echocat.gradle.plugins.golang.model.Platform;
import org.echocat.gradle.plugins.golang.model.ToolchainSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.*;
import static org.echocat.gradle.plugins.golang.model.Platform.currentPlatform;

public class Validate extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validate.class);

    @Override
    public void run() {
        final GolangSettings golang = getGlobalGolang();
        final ToolchainSettings toolchain = getGlobalToolchain();
        final BuildSettings build = getGlobalBuild();

        if (isEmpty(golang.getPackageName())) {
            final Object group = getProject().getGroup();
            if (group != null) {
                golang.setPackageName(group.toString());
            }
            if (isEmpty(golang.getPackageName())) {
                throw new IllegalArgumentException("There is no package name configured. (property: 'golang.packageName')");
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
        final GolangSettings golang = getGlobalGolang();
        final ToolchainSettings toolchain = getGlobalToolchain();
        final File goroot = toolchain.getGoroot();
        if (goroot == null) {
            toolchain.setGoroot(new File(golang.getCacheRoot(), "sdk" + File.separator + toolchain.getGoversion()));
        }
    }

    protected void configureBootstrapGorootIfNeeded() {
        final GolangSettings golang = getGlobalGolang();
        final ToolchainSettings toolchain = getGlobalToolchain();
        final File bootstrapGoroot = toolchain.getBootstrapGoroot();
        if (bootstrapGoroot == null) {
            final String gorootEnv = System.getenv("GOROOT");
            if (isNotEmpty(gorootEnv)) {
                final File goBinary = new File(gorootEnv, File.separator + "bin" + File.separator + "go" + toolchain.getExecutableSuffix());
                if (goBinary.canExecute()) {
                    toolchain.setBootstrapGoroot(new File(gorootEnv));
                    return;
                }
            }
            toolchain.setBootstrapGoroot(new File(golang.getCacheRoot(), "sdk" + File.separator + "bootstrap"));
        }
    }

    protected void configureGopathIfNeeded() {
        final BuildSettings build = getGlobalBuild();
        if (TRUE.equals(build.getUseTemporaryGopath())) {
            build.setGopath(new File(getProject().getBuildDir(), "gopath"));
        }
    }

}
