package org.echocat.gradle.plugins.golang.vcs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface VcsRepositoryProvider {

    @Nullable
    public VcsRepository tryProvideFor(@Nonnull RawVcsReference rawReference) throws VcsException;

}
