package org.echocat.gradle.plugins.golang.vcs;

import org.echocat.gradle.plugins.golang.vcs.isps.BitbucketVcsRepositoryProvider;
import org.echocat.gradle.plugins.golang.vcs.isps.GolangOrgVcsRepositoryProvider;
import org.echocat.gradle.plugins.golang.vcs.isps.GoogleGolangOrgVcsRepositoryProvider;
import org.echocat.gradle.plugins.golang.vcs.isps.SuffixDetectingVcsRepositoryProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.echocat.gradle.plugins.golang.model.VcsRepositoryProvider.defaultConcretes;

public class CombinedVcsRepositoryProvider implements VcsRepositoryProvider {

    @Nonnull
    private final Iterable<VcsRepositoryProvider> _delegates;

    public CombinedVcsRepositoryProvider() {
        this(delegatesWithDefaults(null));
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

    @Nonnull
    public static Iterable<VcsRepositoryProvider> delegatesWithDefaults(@Nullable Iterable<VcsRepositoryProvider> delegates) {
        final List<VcsRepositoryProvider> result = new ArrayList<>(defaultConcretes());

        result.add(new GolangOrgVcsRepositoryProvider());
        result.add(new GoogleGolangOrgVcsRepositoryProvider());
        result.add(new BitbucketVcsRepositoryProvider());

        if (delegates != null) {
            for (final VcsRepositoryProvider delegate : delegates) {
                result.add(delegate);
            }
        }

        result.add(new SuffixDetectingVcsRepositoryProvider());

        return unmodifiableList(result);
    }

}
