package org.echocat.gradle.plugins.golang.testing;

import org.echocat.gradle.plugins.golang.model.Settings;
import org.echocat.gradle.plugins.golang.utils.ProjectEnabled;
import org.echocat.gradle.plugins.golang.utils.ProjectsAndSettingsEnabledSupport;
import org.echocat.gradle.plugins.golang.utils.SettingsEnabled;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

public class TestReportOptimizer extends ProjectsAndSettingsEnabledSupport {

    public TestReportOptimizer(@Nonnull Project project, @Nonnull Settings settings) {
        super(project, settings);
    }

    public <T extends SettingsEnabled & ProjectEnabled> TestReportOptimizer(@Nonnull T source) {
        super(source);
    }


}
