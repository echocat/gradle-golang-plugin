package org.echocat.gradle.plugins.golang.vcs;

import org.echocat.gradle.plugins.golang.vcs.git.GitVcsRepository;

import javax.annotation.Nonnull;

import static org.echocat.gradle.plugins.golang.vcs.VcsType.git;

public class VcsRepositoryFactory {

    @Nonnull
    public VcsRepository createFor(@Nonnull VcsReference reference) throws VcsException {
        final VcsType type = reference.getType();
        if (type == git) {
            return createGitFor(reference);
        }
        throw new VcsValidationException("Could not handle vcs of type " + type + ".");
    }

    @Nonnull
    protected GitVcsRepository createGitFor(@Nonnull VcsReference reference) throws VcsException {
        return new GitVcsRepository(reference);
    }

}
