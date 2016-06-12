package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;

public class DependenciesSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependenciesSettings.class);

    private boolean _forceUpdate;
    private boolean _deleteUnknownDependencies;
    private File _dependencyCache;

    public DependenciesSettings(@Nonnull Project project) {
        _dependencyCache = new File(project.getProjectDir(), "vendor");
    }

    public boolean isForceUpdate() {
        return _forceUpdate;
    }

    public DependenciesSettings setForceUpdate(boolean forceUpdate) {
        _forceUpdate = forceUpdate;
        return this;
    }

    public boolean isDeleteUnknownDependencies() {
        return _deleteUnknownDependencies;
    }

    public DependenciesSettings setDeleteUnknownDependencies(boolean deleteUnknownDependencies) {
        _deleteUnknownDependencies = deleteUnknownDependencies;
        return this;
    }

    public File getDependencyCache() {
        return _dependencyCache;
    }

    public DependenciesSettings setDependencyCache(File dependencyCache) {
        _dependencyCache = dependencyCache;
        return this;
    }

}
