package org.echocat.gradle.plugins.golang.vcs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public interface VcsRepository {

    @Nonnull
    public VcsReference getReference();

    public boolean isWorking() throws VcsException;

    @Nullable
    public VcsFullReference updateIfRequired(@Nonnull File baseDirectory) throws VcsException;

    @Nonnull
    public VcsFullReference forceUpdate(@Nonnull File baseDirectory) throws VcsException;

}
