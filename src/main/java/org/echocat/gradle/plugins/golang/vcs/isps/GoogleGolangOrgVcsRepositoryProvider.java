package org.echocat.gradle.plugins.golang.vcs.isps;

import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsException;
import org.echocat.gradle.plugins.golang.vcs.VcsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.echocat.gradle.plugins.golang.vcs.VcsType.git;

public class GoogleGolangOrgVcsRepositoryProvider extends IspBasedVcsRepositoryProviderSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleGolangOrgVcsRepositoryProvider.class);

    protected static final Pattern REF_PATTERN = compile("^refs/(?:heads|tags)/v(?<version>[0-9]{1,9}(?:\\.[0-9]{1,9}(?:\\.[0-9]{1,9})?)?)$");

    public GoogleGolangOrgVcsRepositoryProvider() {
        super("google.golang.org/", compile("^(?<root>google.golang\\.org/(?<repo>[A-Za-z0-9_.\\-]+))(?<subPath>/[A-Za-z0-9_.\\-]+)*$"));
    }

    @Nonnull
    @Override
    protected VcsType detectVcsTypeOf(@Nonnull RawVcsReference rawReference) throws VcsException {
        return fixedVcsTypeFor(rawReference, git);
    }

    @Nonnull
    @Override
    protected String rootFor(@Nonnull Matcher matcher, @Nonnull RawVcsReference rawReference) throws VcsException {
        final String repo = matcher.group("repo");
        if ("api".equals(repo)) {
            return "github.com/google/google-api-go-client";
        }
        if ("code".equals(repo)) {
            return "code.googlesource.com/gocloud-legacy";
        }
        if ("grpc".equals(repo)) {
            return "github.com/grpc/grpc-go";
        }
        return "github.com/golang/" + repo;
    }

    @Nullable
    @Override
    protected String refFor(@Nonnull Matcher matcher, @Nonnull RawVcsReference rawReference) throws VcsException {
        return "master";
    }

    @Nonnull
    protected String buildUriFor(@Nonnull String root, @Nonnull RawVcsReference rawReference) throws VcsException {
        return vcsUriPrefixFor(rawReference, git) + root + vcsUriUriSuffixFor(rawReference, git);
    }

    @Nonnull
    @Override
    protected String getName() {
        return "google.golang.org";
    }

}
