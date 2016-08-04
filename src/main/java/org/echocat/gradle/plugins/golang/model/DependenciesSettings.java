package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.nio.file.Path;

public class DependenciesSettings {

    @Nonnull
    private final Project _project;

    private Boolean _forceUpdate;
    private Boolean _deleteUnknownDependencies;
    private Boolean _deleteAllCachedDependenciesOnClean;
    private Path _dependencyCache;

    @Inject
    public DependenciesSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _dependencyCache = project.getProjectDir().toPath().resolve("vendor");
            _deleteUnknownDependencies = true;
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

    public Boolean getDeleteAllCachedDependenciesOnClean() {
        return _deleteAllCachedDependenciesOnClean;
    }

    public void setDeleteAllCachedDependenciesOnClean(Boolean deleteAllCachedDependenciesOnClean) {
        _deleteAllCachedDependenciesOnClean = deleteAllCachedDependenciesOnClean;
    }

    public Path getDependencyCache() {
        return _dependencyCache;
    }

    public void setDependencyCache(Path dependencyCache) {
        _dependencyCache = dependencyCache;
    }

}
