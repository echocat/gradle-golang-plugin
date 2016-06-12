package org.echocat.gradle.plugins.golang.vcs;

import javax.annotation.Nonnull;

public class VcsFullReference {

    @Nonnull
    private final VcsReference _reference;
    @Nonnull
    private final String _full;

    public VcsFullReference(@Nonnull VcsReference reference, @Nonnull String full) {
        _reference = reference;
        _full = full;
    }

    @Nonnull
    public VcsReference getReference() {
        return _reference;
    }

    @Nonnull
    public String getFull() {
        return _full;
    }

}
