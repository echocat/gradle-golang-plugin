package org.echocat.gradle.plugins.golang.model;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import org.echocat.gradle.plugins.golang.vcs.VcsType;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.util.CollectionUtils;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.echocat.gradle.plugins.golang.Constants.VENDOR_DIRECTORY_NAME;
import static org.gradle.util.ConfigureUtil.configure;

public class DependenciesSettings {

    private final boolean _root;
    @Nonnull
    private final Project _project;

    private Boolean _forceUpdate;
    private Boolean _deleteUnknownDependencies;
    private Boolean _deleteAllCachedDependenciesOnClean;
    private Path _dependencyCache;

    @Inject
    public DependenciesSettings(boolean root, @Nonnull Project project) {
        _root = root;
        _project = project;
        if (root) {
            _dependencyCache = project.getProjectDir().toPath().resolve(VENDOR_DIRECTORY_NAME);
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

    protected static final Pattern NOTATION_PATTERN = Pattern.compile("(?<group>[a-zA-Z0-9.\\-_/]+)(?::(?<version>[a-zA-Z0-9.\\-_/]+))?");

    @Nonnull
    public Dependency add(@Nonnull String configurationName, @Nonnull Object notation) {
        return add(configurationName, notation, null);
    }

    @Nonnull
    public Dependency add(@Nonnull String configurationName, @Nonnull Object notation, @Nullable Closure<?> configureWith) {
        return doAdd(configurationName, notation, configureWith);
    }

    @Nonnull
    public Dependency create(@Nonnull String configurationName, @Nonnull Object notation) {
        return create(configurationName, notation, null);
    }

    @Nonnull
    public Dependency create(@Nonnull String configurationName, @Nonnull Object notation, @Nullable Closure<?> configureWith) {
        return doAdd(configurationName, notation, configureWith);
    }

    @Nonnull
    protected Dependency doAdd(@Nonnull String configurationName, @Nonnull Object notation, @Nullable Closure<?> configureWith) {
        if (notation instanceof String) {
            return doAdd(configurationName, (String) notation, configureWith);
        }
        if (notation instanceof Map) {
            //noinspection unchecked,rawtypes
            return doAdd(configurationName, (Map) notation, configureWith);
        }
        throw new IllegalArgumentException("Could not handle notation of type: " + notation.getClass().getName());
    }

    @Nonnull
    protected Dependency doAdd(@Nonnull String configurationName, @Nonnull String notation, @Nullable Closure<?> configureWith) {
        final Matcher matcher = NOTATION_PATTERN.matcher(notation);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal dependency notation provided: " + notation);
        }
        final String group = matcher.group("group");
        final String version = matcher.group("version");
        return doAdd(configurationName, new GolangDependency()
                .setGroup(group)
                .setVersion(version)
            , configureWith);
    }

    @Nonnull
    protected Dependency doAdd(@Nonnull String configurationName, @Nonnull Map<String, Object> arguments, @Nullable Closure<?> configureWith) {
        final String group = requiredArgument(arguments, "group");
        final String version = argument(arguments, "version");
        final URI repositoryUri = uriArgument(arguments, "repositoryUri");
        final VcsType repositoryType = vcsTypeArgument(arguments, "repositoryType");
        final UpdatePolicy updatePolicy = updatePolicyArgument(arguments, "updatePolicy");
        return doAdd(configurationName, new GolangDependency()
                .setGroup(group)
                .setVersion(version)
                .setRepositoryUri(repositoryUri)
                .setRepositoryType(repositoryType)
                .setUpdatePolicy(updatePolicy)
            , configureWith);
    }

    @Nonnull
    protected Dependency doAdd(@Nonnull String configurationName, @Nonnull Dependency dependency, @Nullable Closure<?> configureWith) {
        if (!_root) {
            throw new IllegalStateException("Adding of dependencies for golang is currently not support at task level.");
        }
        final Configuration configuration = _project.getConfigurations().getByName(configurationName);
        configuration.getDependencies().add(dependency);
        if (configureWith != null) {
            return configure(configureWith, dependency);
        }
        return dependency;
    }

    @Nullable
    protected String argument(@Nonnull Map<String, Object> arguments, @Nonnull String name) {
        final Object plain = arguments.get(name);
        if (plain == null) {
            return null;
        }
        return plain.toString();
    }

    @Nullable
    protected URI uriArgument(@Nonnull Map<String, Object> arguments, @Nonnull String name) {
        final Object plain = arguments.get(name);
        if (plain == null) {
            return null;
        }
        if (plain instanceof URI) {
            return (URI) plain;
        }
        return URI.create(plain.toString());
    }

    @Nullable
    protected VcsType vcsTypeArgument(@Nonnull Map<String, Object> arguments, @Nonnull String name) {
        final Object plain = arguments.get(name);
        if (plain == null) {
            return null;
        }
        if (plain instanceof VcsType) {
            return (VcsType) plain;
        }
        return VcsType.valueOf(plain.toString());
    }

    @Nonnull
    protected UpdatePolicy updatePolicyArgument(@Nonnull Map<String, Object> arguments, @Nonnull String name) {
        final Object plain = arguments.get(name);
        if (plain instanceof UpdatePolicy) {
            return (UpdatePolicy) plain;
        }
        return UpdatePolicy.valueOf(plain.toString());
    }

    @Nonnull
    protected String requiredArgument(@Nonnull Map<String, Object> arguments, @Nonnull String name) {
        final String result = argument(arguments, name);
        if (result == null) {
            throw new IllegalArgumentException("Required argument '" + name + "' not provided. But got: " + arguments);
        }
        return result;
    }

    @Nullable
    public Object methodMissing(@Nonnull String name, @Nullable Object args) {
        final Object[] argsArray = (Object[]) args;
        final Configuration configuration = _project.getConfigurations().findByName(name);
        if (configuration == null) {
            throw new MissingMethodException(name, this.getClass(), argsArray);
        }
        final List<?> normalizedArgs = CollectionUtils.flattenCollections(argsArray);
        if (normalizedArgs.size() == 2 && normalizedArgs.get(1) instanceof Closure) {
            //noinspection rawtypes
            return doAdd(name, normalizedArgs.get(0), (Closure) normalizedArgs.get(1));
        } else if (normalizedArgs.size() == 1) {
            return doAdd(name, normalizedArgs.get(0), null);
        } else {
            for (final Object arg : normalizedArgs) {
                doAdd(name, arg, null);
            }
            return null;
        }
    }
}
