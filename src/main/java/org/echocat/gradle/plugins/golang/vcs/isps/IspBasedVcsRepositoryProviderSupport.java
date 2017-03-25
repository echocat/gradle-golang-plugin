package org.echocat.gradle.plugins.golang.vcs.isps;

import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsException;
import org.echocat.gradle.plugins.golang.vcs.VcsValidationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class IspBasedVcsRepositoryProviderSupport extends VcsRepositoryProviderSupport {

    @Nullable
    private final String _prefix;

    public IspBasedVcsRepositoryProviderSupport(@Nullable String prefix, @Nonnull Pattern dependencyPattern) {
        super(dependencyPattern);
        _prefix = prefix;
    }

    @Override
    protected boolean couldHandle(@Nonnull RawVcsReference rawReference) {
        final String name = rawReference.getId();
        return name.startsWith(prefix());
    }

    @Override
    @Nonnull
    protected Matcher nameMatcherFor(@Nonnull RawVcsReference rawReference) throws VcsException {
        final Matcher matcher = dependencyPattern().matcher(rawReference.getId());
        if (!matcher.matches()) {
            throw new VcsValidationException("Name of dependency " + rawReference + " is invalid for " + getName() + ".");
        }
        return matcher;
    }

    @Nonnull
    protected String prefix() {
        if (isEmpty(_prefix)) {
            throw new IllegalStateException("A repository that does not specify a prefix should override the couldHandler() method.");
        }
        return _prefix;
    }

    @Nonnull
    protected abstract String getName();

}
