package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;

public class DependenciesSettings {

    @Nonnull
    private final Project _project;

    private Boolean _forceUpdate;
    private Boolean _deleteUnknownDependencies;
    private File _dependencyCache;

    @Inject
    public DependenciesSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _dependencyCache = new File(project.getProjectDir(), "vendor");
        }
    }

    public Boolean getForceUpdate() {
        return _forceUpdate;
    }

    public void setForceUpdate(Boolean forceUpdate) {
        _forceUpdate = forceUpdate;
    }

    public Boolean getDeleteUnknownDependencies() {
        return _deleteUnknownDependencies;
    }

    public void setDeleteUnknownDependencies(Boolean deleteUnknownDependencies) {
        _deleteUnknownDependencies = deleteUnknownDependencies;
    }

    public File getDependencyCache() {
        return _dependencyCache;
    }

    public void setDependencyCache(File dependencyCache) {
        _dependencyCache = dependencyCache;
    }

}
