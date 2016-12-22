package org.echocat.gradle.plugins.golang.utils;

import org.gradle.api.Project;

import javax.annotation.Nonnull;

public interface ProjectEnabled {

    @Nonnull
    public Project getProject();

}
