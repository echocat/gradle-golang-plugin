package org.echocat.gradle.plugins.golang.vcs.isps;

import org.echocat.gradle.plugins.golang.vcs.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.URI.create;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public abstract class IspBasedVcsRepositoryProviderSupport implements VcsRepositoryProvider {

    @Nonnull
    private final VcsRepositoryFactory _vcsRepositoryFactory = new VcsRepositoryFactory();

    @Nullable
    private final String _prefix;
    @Nonnull
    private final Pattern _dependencyPattern;

    public IspBasedVcsRepositoryProviderSupport(@Nullable String prefix, @Nonnull Pattern dependencyPattern) {
        _prefix = prefix;
        _dependencyPattern = dependencyPattern;
    }

    @Nullable
    @Override
    public VcsRepository tryProvideFor(@Nonnull RawVcsReference rawReference) throws VcsException {
        if (!couldHandle(rawReference)) {
            return null;
        }
        final VcsType vcsType = detectVcsTypeOf(rawReference);
        final VcsReference vcsReference = detectVcsUriOf(rawReference, vcsType);
        return _vcsRepositoryFactory.createFor(vcsReference);
    }

    protected boolean couldHandle(@Nonnull RawVcsReference rawReference) {
        final String name = rawReference.getId();
        return name.startsWith(prefix());
    }

    @Nonnull
    protected abstract VcsType detectVcsTypeOf(@Nonnull RawVcsReference rawReference) throws VcsException;

    @Nonnull
    protected VcsType fixedVcsTypeFor(@Nonnull RawVcsReference rawReference, @Nonnull VcsType fixedType) throws VcsException {
        final VcsType selected = rawReference.getType();
        if (selected != null && selected != fixedType) {
            throw new VcsValidationException("There was the VCS type " + selected + " explicit selected for dependency " + rawReference + " but this VCS could only be of type " + fixedType + ".");
        }
        return fixedType;
    }

    @Nonnull
    protected VcsReference detectVcsUriOf(@Nonnull RawVcsReference rawReference, VcsType vcsType) throws VcsException {
        final URI uri = rawReference.getUri();
        if (uri != null) {
            final Matcher matcher = nameMatcherFor(rawReference);
            final String id = idFor(matcher, rawReference);
            final String ref = refFor(matcher, rawReference);
            return new VcsReference(vcsType, id, uri, ref, rawReference.getUpdatePolicy(), subPathOf(matcher));
        }
        return resolveVcsUriFor(rawReference, vcsType);
    }

    @Nonnull
    protected VcsReference resolveVcsUriFor(@Nonnull RawVcsReference rawReference, @Nonnull VcsType vcsType) throws VcsException {
        final Matcher matcher = nameMatcherFor(rawReference);
        final String id = idFor(matcher, rawReference);
        final String ref = refFor(matcher, rawReference);
        final URI uri = create(vcsUriPrefixFor(rawReference, vcsType) + rootFor(matcher, rawReference) + vcsUriUriSuffixFor(rawReference, vcsType));
        return new VcsReference(vcsType, id, uri, ref, rawReference.getUpdatePolicy(), subPathOf(matcher));
    }

    @Nullable
    protected String subPathOf(Matcher matcher) {
        try {
            return matcher.group("subPath");
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    @Nullable
    protected String refFor(@Nonnull Matcher matcher, @Nonnull RawVcsReference rawReference) throws VcsException {
        return rawReference.getRef();
    }

    @Nonnull
    protected String rootFor(@Nonnull Matcher matcher, @Nonnull RawVcsReference rawReference) throws VcsException {
        return matcher.group("root");
    }

    @Nonnull
    protected String idFor(@Nonnull Matcher matcher, @Nonnull RawVcsReference rawReference) throws VcsException {
        return matcher.group("root");
    }

    @Nonnull
    protected Matcher nameMatcherFor(@Nonnull RawVcsReference rawReference) throws VcsException {
        final Matcher matcher = dependencyPattern().matcher(rawReference.getId());
        if (!matcher.matches()) {
            throw new VcsValidationException("Name of dependency " + rawReference + " is invalid for " + getName() + ".");
        }
        return matcher;
    }

    @SuppressWarnings("UnusedParameters")
    @Nonnull
    protected String vcsUriPrefixFor(@Nonnull RawVcsReference rawReference, @Nonnull VcsType vcsType) throws VcsException {
        return "https://";
    }

    @SuppressWarnings("UnusedParameters")
    @Nonnull
    protected String vcsUriUriSuffixFor(@Nonnull RawVcsReference rawReference, @Nonnull VcsType vcsType) throws VcsException {
        return vcsType.getUriSuffix();
    }

    @Nonnull
    protected String prefix() {
        if (isEmpty(_prefix)) {
            throw new IllegalStateException("A repository that does not specify a prefix should override the couldHandler() method.");
        }
        return _prefix;
    }

    @Nonnull
    protected Pattern dependencyPattern() {
        return _dependencyPattern;
    }

    @Nonnull
    protected VcsRepositoryFactory vcsFactory() {
        return _vcsRepositoryFactory;
    }

    @Nonnull
    protected abstract String getName();

}
