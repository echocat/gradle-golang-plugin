package org.echocat.gradle.plugins.golang.vcs.isps;

import org.apache.commons.lang3.StringUtils;
import org.echocat.gradle.plugins.golang.model.IntegerVersion;
import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsException;
import org.echocat.gradle.plugins.golang.vcs.VcsIllegalReferenceException;
import org.echocat.gradle.plugins.golang.vcs.VcsType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.echocat.gradle.plugins.golang.vcs.VcsType.git;

public class GopkgInVcsRepositoryProvider extends IspBasedVcsRepositoryProviderSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GopkgInVcsRepositoryProvider.class);

    protected static final Pattern REF_PATTERN = compile("^refs/(?:heads|tags)/v(?<version>[0-9]{1,9}(?:\\.[0-9]{1,9}(?:\\.[0-9]{1,9})?)?)$");

    public GopkgInVcsRepositoryProvider() {
        super("gopkg.in/", compile("^(?<root>gopkg\\.in(?<repo>(?:/[A-Za-z0-9_.\\-]+){1,2})\\.v(?<version>[0-9]{1,9}))(?<subPath>/[A-Za-z0-9_.\\-]+)*$"));
    }

    @Nonnull
    @Override
    protected VcsType detectVcsTypeOf(@Nonnull RawVcsReference rawReference) throws VcsException {
        return fixedVcsTypeFor(rawReference, git);
    }

    @Nonnull
    @Override
    protected String rootFor(@Nonnull Matcher matcher, @Nonnull RawVcsReference rawReference) throws VcsException {
        final StringBuilder sb = new StringBuilder();
        sb.append("github.com/");
        final String[] parts = StringUtils.split(matcher.group("repo"), "/", 3);
        if (parts.length == 1) {
            sb.append("go-").append(parts[0]).append('/').append(parts[0]);
        } else {
            sb.append(parts[0]).append('/').append(parts[1]);
        }
        return sb.toString();
    }

    @Nullable
    @Override
    protected String refFor(@Nonnull Matcher matcher, @Nonnull RawVcsReference rawReference) throws VcsException {
        final String root = rootFor(matcher, rawReference);
        final String remoteUri = buildUriFor(root, rawReference);
        LOGGER.debug("Fetch remote refs for {} from {}...", rawReference.getId(), remoteUri);
        final Collection<Ref> refs;
        try {
            refs = Git.lsRemoteRepository()
                .setRemote(remoteUri)
                .call();
        } catch (final GitAPIException e) {
            throw new VcsException(e);
        }
        LOGGER.debug("Fetch remote refs for {} from {}... DONE!", rawReference.getId(), remoteUri);

        return selectBestFitRefFor(matcher, rawReference, refs);
    }

    @Nonnull
    protected String selectBestFitRefFor(@Nonnull Matcher matcher, @Nonnull RawVcsReference rawReference, @Nonnull Collection<Ref> refs) throws VcsException {
        final int expectedVersion = Integer.valueOf(matcher.group("version"));
        final Map<IntegerVersion, Ref> candidates = asVersionToRef(refs);
        Ref lastMatch = null;
        for (final Entry<IntegerVersion, Ref> candidate : candidates.entrySet()) {
            final IntegerVersion version = candidate.getKey();
            if (expectedVersion == version.getMajor()) {
                lastMatch = candidate.getValue();
            }
        }
        if (lastMatch == null) {
            throw new VcsIllegalReferenceException("Version " + expectedVersion + " of " + rawReference.getId() + " does not exist.");
        }
        return lastMatch.getName();
    }

    @Nonnull
    protected Map<IntegerVersion, Ref> asVersionToRef(@Nonnull Iterable<Ref> refs) throws VcsException {
        final Map<IntegerVersion, Ref> result = new TreeMap<>();
        for (final Ref ref : refs) {
            final Matcher matcher = REF_PATTERN.matcher(ref.getName());
            if (matcher.matches()) {
                final IntegerVersion version = new IntegerVersion(matcher.group("version"));
                result.put(version, ref);
            }
        }
        return result;
    }

    @Nonnull
    protected String buildUriFor(@Nonnull String root, @Nonnull RawVcsReference rawReference) throws VcsException {
        return vcsUriPrefixFor(rawReference, git) + root + vcsUriUriSuffixFor(rawReference, git);
    }

    @Nonnull
    @Override
    protected String getName() {
        return "gopkg.in";
    }

}
