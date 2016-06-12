package org.echocat.gradle.plugins.golang.vcs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;

public enum VcsType {
    git(".git"),
    bzr(".bzr"),
    hg(".hg"),
    svn(".svn"),
    manual("");

    @Nonnull
    private final String _uriSuffix;
    @Nonnull
    private final Set<String> _schemes;

    VcsType(@Nonnull String uriSuffix, @Nullable String... plainSchemes) {
        _uriSuffix = uriSuffix;
        final Set<String> schemes = new LinkedHashSet<>();
        if (plainSchemes != null) {
            addAll(schemes, plainSchemes);
        }
        _schemes = unmodifiableSet(schemes);
    }

    @Nonnull
    public String getUriSuffix() {
        return _uriSuffix;
    }

    @Nonnull
    public Set<String> getSchemes() {
        return _schemes;
    }

    @Nonnull
    public String getName() {
        return name();
    }

    @Override
    public String toString() {
        return getName();
    }

}
