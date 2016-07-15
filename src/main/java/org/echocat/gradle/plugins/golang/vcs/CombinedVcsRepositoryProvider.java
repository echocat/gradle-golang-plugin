package org.echocat.gradle.plugins.golang.vcs;

import org.echocat.gradle.plugins.golang.vcs.isps.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

import static java.util.Arrays.asList;

public class CombinedVcsRepositoryProvider implements VcsRepositoryProvider {

    @Nonnull
    private final Iterable<VcsRepositoryProvider> _delegates;

    public CombinedVcsRepositoryProvider() {
        this(
            new GithubVcsRepositoryProvider(),
            new GopkgInVcsRepositoryProvider(),
            new GolangOrgVcsRepositoryProvider(),
            new BitbucketVcsRepositoryProvider(),
            new HubJazzVcsRepositoryProvider(),
            new GitApacheVcsRepositoryProvider(),
            new GitOpenstackVcsRepositoryProvider()
        );
    }

    public CombinedVcsRepositoryProvider(@Nullable VcsRepositoryProvider... delegates) {
        this(delegates != null ? asList(delegates) : null);
    }

    public CombinedVcsRepositoryProvider(@Nullable Iterable<VcsRepositoryProvider> delegates) {
        _delegates = delegates != null ? delegates : Collections.<VcsRepositoryProvider>emptyList();
    }

    @Nullable
    @Override
    public VcsRepository tryProvideFor(@Nonnull RawVcsReference reference) throws VcsException {
        for (final VcsRepositoryProvider candidate : delegates()) {
            final VcsRepository result = candidate.tryProvideFor(reference);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Nonnull
    protected Iterable<VcsRepositoryProvider> delegates() {
        return _delegates;
    }

}
