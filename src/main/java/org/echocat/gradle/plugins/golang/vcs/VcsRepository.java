package org.echocat.gradle.plugins.golang.vcs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

public interface VcsRepository {

    @Nonnull
    public VcsReference getReference();

    public boolean isWorking() throws VcsException;

    @Nullable
    public VcsFullReference updateIfRequired(@Nonnull Path baseDirectory) throws VcsException;

    @Nonnull
    public VcsFullReference forceUpdate(@Nonnull Path baseDirectory) throws VcsException;

}
