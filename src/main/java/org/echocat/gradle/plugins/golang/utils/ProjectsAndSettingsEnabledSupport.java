package org.echocat.gradle.plugins.golang.utils;

import org.echocat.gradle.plugins.golang.model.Settings;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

public abstract class ProjectsAndSettingsEnabledSupport extends SettingsEnabledSupport {

    @Nonnull
    private final Project _project;

    public ProjectsAndSettingsEnabledSupport(@Nonnull Project project, @Nonnull Settings settings) {
        super(settings);
        _project = project;
    }

    public <T extends SettingsEnabled & ProjectEnabled> ProjectsAndSettingsEnabledSupport(@Nonnull T source) {
        this(source.getProject(), source.getSettings());
    }

    @Nonnull
    public Project getProject() {
        return _project;
    }
}
