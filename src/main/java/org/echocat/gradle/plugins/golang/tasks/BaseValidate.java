package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.GolangSettings;
import org.echocat.gradle.plugins.golang.model.Settings;
import org.echocat.gradle.plugins.golang.model.ToolchainSettings;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.Boolean.FALSE;
import static java.nio.file.Files.isExecutable;
import static org.apache.commons.lang3.StringUtils.*;

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

        progress.progress("Configure GOROOT...");
        configureGorootIfNeeded();
        progress.progress("Configure GOROOT_BOOTSTRAP...");
        configureBootstrapGorootIfNeeded();
        progress.progress("Configure GOPATH...");
        configureGopathIfNeeded();

        progress.completed();

        LOGGER.info("Package:          {}", golang.getPackageName());
        LOGGER.info("Platforms:        {}", join(golang.getPlatforms(), ", "));
        LOGGER.info("Host:             {}", golang.getHostPlatform());
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
                final Path gorootPath = Paths.get(gorootEnv);
                final Path goBinary = gorootPath.resolve("bin").resolve("go" + toolchain.getExecutableSuffix());
                if (isExecutable(goBinary)) {
                    toolchain.setBootstrapGoroot(gorootPath);
                    return;
                }
            }
            toolchain.setBootstrapGoroot(golang.getCacheRoot().resolve("sdk").resolve("bootstrap"));
        }
    }

    protected void configureGopathIfNeeded() {
        final BuildSettings build = getGlobalSettings().getBuild();
        if (!FALSE.equals(build.getUseTemporaryGopath())) {
            build.setGopath(getProject().getBuildDir().toPath().resolve("gopath"));
        }
    }

}
