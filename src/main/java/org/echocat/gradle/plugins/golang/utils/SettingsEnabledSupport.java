package org.echocat.gradle.plugins.golang.utils;

import org.echocat.gradle.plugins.golang.model.*;

import javax.annotation.Nonnull;

public abstract class SettingsEnabledSupport implements SettingsEnabled {

    @Nonnull
    private final Settings _settings;

    public SettingsEnabledSupport(@Nonnull Settings settings) {
        _settings = settings;
    }

    public SettingsEnabledSupport(@Nonnull SettingsEnabled settingsEnabled) {
        this(settingsEnabled.getSettings());
    }

    @Override
    @Nonnull
    public Settings getSettings() {
        return _settings;
    }

    @Nonnull
    protected GolangSettings getGolang() {
        return getSettings().getGolang();
    }

    @Nonnull
    protected BuildSettings getBuild() {
        return getSettings().getBuild();
    }

    @Nonnull
    protected DependenciesSettings getDependencies() {
        return getSettings().getDependencies();
    }

    @Nonnull
    protected ToolchainSettings getToolchain() {
        return getSettings().getToolchain();
    }

    @Nonnull
    protected TestingSettings getTesting() {
        return getSettings().getTesting();
    }

}
