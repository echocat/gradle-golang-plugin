package org.echocat.gradle.plugins.golang.model;

import org.echocat.gradle.plugins.golang.vcs.VcsType;
import org.echocat.gradle.plugins.golang.vcs.isps.DefaultVcsRepositoryProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.compile;
import static org.echocat.gradle.plugins.golang.vcs.VcsType.git;

@Immutable
public class VcsRepositoryProvider {

    @Nonnull
    private static final List<VcsRepositoryProvider> DEFAULTS = unmodifiableList(asList(
        new VcsRepositoryProvider(git, "github.com/", "GitHub", compile("^(?<root>github\\.com/[A-Za-z0-9_.\\-]+/[A-Za-z0-9_.\\-]+)(?<subPath>/[A-Za-z0-9_.\\-]+)*$")),
        new VcsRepositoryProvider(git, "gopkg.in/", "gopkg.in", compile("^(?<root>gopkg\\.in(?<repo>(?:/[A-Za-z0-9_.\\-]+){1,2})\\.v(?<version>[0-9]{1,9}))(?<subPath>/[A-Za-z0-9_.\\-]+)*$")),
        new VcsRepositoryProvider(git, "git.apache.org/", "Git at Apache", compile("^(?<root>git.apache.org/[a-z0-9_.\\-]+\\.git)(?<subPath>/[A-Za-z0-9_.\\-]+)*$")),
        new VcsRepositoryProvider(git, "git.openstack.org/", "OpenStack Git Repository", compile("^(?<root>git\\.openstack\\.org/[A-Za-z0-9_.\\-]+/[A-Za-z0-9_.\\-]+)(\\.git)?(?<subPath>/[A-Za-z0-9_.\\-]+)*$")),
        new VcsRepositoryProvider(git, "hub.jazz.net/git/", "IBM Bluemix DevOps Services", compile("^(?<root>hub\\.jazz\\.net/git/[a-z0-9]+/[A-Za-z0-9_.\\-]+)(?<subPath>/[A-Za-z0-9_.\\-]+)*$"))
    ));
    private static final List<org.echocat.gradle.plugins.golang.vcs.VcsRepositoryProvider> DEFAULT_CONCRETES = toConcrete(DEFAULTS);

    @Nonnull
    public static List<VcsRepositoryProvider> defaults() {
        return DEFAULTS;
    }

    @Nonnull
    public static List<org.echocat.gradle.plugins.golang.vcs.VcsRepositoryProvider> defaultConcretes() {
        return DEFAULT_CONCRETES;
    }

    @Nonnull
    private final String _prefix;
    @Nonnull
    private final VcsType _type;
    @Nonnull
    private final String _name;
    @Nonnull
    private final Pattern _dependencyPattern;

    public VcsRepositoryProvider(@Nonnull VcsType type, @Nonnull String prefix, @Nonnull String name, @Nonnull Pattern dependencyPattern) {
        _prefix = prefix;
        _type = type;
        _name = name;
        _dependencyPattern = dependencyPattern;
    }

    @Nonnull
    public String getPrefix() {
        return _prefix;
    }

    @Nonnull
    public VcsType getType() {
        return _type;
    }

    @Nonnull
    public String getName() {
        return _name;
    }

    @Nonnull
    public Pattern getDependencyPattern() {
        return _dependencyPattern;
    }

    @Override
    public String toString() {
        return getName() + "{prefix: " + getPrefix() + ", type: " + getType() + ", dependencyPattern: " + getDependencyPattern() + "}";
    }

    @Nonnull
    public org.echocat.gradle.plugins.golang.vcs.VcsRepositoryProvider toConcrete() {
        return new DefaultVcsRepositoryProvider(getType(), getPrefix(), getName(), getDependencyPattern());
    }

    @Nonnull
    public static List<org.echocat.gradle.plugins.golang.vcs.VcsRepositoryProvider> toConcrete(@Nullable Iterable<VcsRepositoryProvider> models) {
        final List<org.echocat.gradle.plugins.golang.vcs.VcsRepositoryProvider> result = new ArrayList<>();
        if (models != null) {
            for (final VcsRepositoryProvider model : models) {
                result.add(model.toConcrete());
            }
        }
        return unmodifiableList(result);
    }

}
