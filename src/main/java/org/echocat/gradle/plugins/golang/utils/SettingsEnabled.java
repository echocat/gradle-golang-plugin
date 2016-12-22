package org.echocat.gradle.plugins.golang.utils;

import org.echocat.gradle.plugins.golang.model.Settings;

import javax.annotation.Nonnull;

public interface SettingsEnabled {

    @Nonnull
    public Settings getSettings();

}
