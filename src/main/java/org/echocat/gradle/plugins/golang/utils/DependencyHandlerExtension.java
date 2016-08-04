package org.echocat.gradle.plugins.golang.utils;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.MethodClosure;
import org.echocat.gradle.plugins.golang.model.GolangDependency;
import org.echocat.gradle.plugins.golang.model.UpdatePolicy;
import org.echocat.gradle.plugins.golang.vcs.VcsType;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyHandlerExtension {

    protected static final Pattern NOTATION_PATTERN = Pattern.compile("(?<group>[a-zA-Z0-9.\\-_/]+)(?::(?<version>[a-zA-Z0-9.\\-_/]+))?");

    @Nonnull
    private final ConfigurationContainer _configurationContainer;

    public DependencyHandlerExtension(@Nonnull ConfigurationContainer configurationContainer) {
        _configurationContainer = configurationContainer;
    }

    @Nonnull
    public Dependency build(@Nonnull String notation) {
        return dependency("build", notation);
    }

    @Nonnull
    public Dependency build(@Nonnull Map<String, Object> arguments) {
        return dependency("build", arguments);
    }

    @Nonnull
    public Dependency test(@Nonnull String notation) {
        return dependency("test", notation);
    }

    @Nonnull
    public Dependency test(@Nonnull Map<String, Object> arguments) {
        return dependency("test", arguments);
    }

    @Nonnull
    public Dependency tool(@Nonnull String notation) {
        return dependency("tool", notation);
    }

    @Nonnull
    public Dependency tool(@Nonnull Map<String, Object> arguments) {
        return dependency("tool", arguments);
    }

    @Nonnull
    public Dependency dependency(@Nonnull String configurationName, @Nonnull String notation) {
        final Matcher matcher = NOTATION_PATTERN.matcher(notation);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal dependency notation provided: " + notation);
        }
        final String group = matcher.group("group");
        final String version = matcher.group("version");
        return doAdd(configurationName, new GolangDependency()
            .setGroup(group)
            .setVersion(version)
        );
    }

    @Nonnull
    public Dependency dependency(@Nonnull String configurationName, @Nonnull Map<String, Object> arguments) {
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
        );
    }

    @Nonnull
    protected Dependency doAdd(@Nonnull String configurationName, @Nonnull Dependency dependency) {
        final Configuration configuration = _configurationContainer.getByName(configurationName);
        configuration.getDependencies().add(dependency);
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

    @Nullable
    protected UpdatePolicy updatePolicyArgument(@Nonnull Map<String, Object> arguments, @Nonnull String name) {
        final Object plain = arguments.get(name);
        if (plain == null) {
            return null;
        }
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

    @Nonnull
    public Map<String, Closure<Dependency>> getDependencyMethodsAsClosures() {
        final Map<String, Closure<Dependency>> results = new HashMap<>();
        for (final Method candidate : getClass().getMethods()) {
            if (Dependency.class.equals(candidate.getReturnType())) {
                //noinspection unchecked
                results.put(candidate.getName(), new MethodClosure(this, candidate.getName()));
            }
        }
        return results;
    }

}
