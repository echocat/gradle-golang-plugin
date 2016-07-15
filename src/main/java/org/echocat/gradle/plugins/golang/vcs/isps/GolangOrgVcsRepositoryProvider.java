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

public class GolangOrgVcsRepositoryProvider extends IspBasedVcsRepositoryProviderSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GolangOrgVcsRepositoryProvider.class);

    protected static final Pattern REF_PATTERN = compile("^refs/(?:heads|tags)/v(?<version>[0-9]{1,9}(?:\\.[0-9]{1,9}(?:\\.[0-9]{1,9})?)?)$");

    public GolangOrgVcsRepositoryProvider() {
        super("golang.org/", compile("^(?<root>golang\\.org/x/(?<repo>[A-Za-z0-9_.\\-]+))(?<subPath>/[A-Za-z0-9_.\\-]+)*$"));
    }

    @Nonnull
    @Override
    protected VcsType detectVcsTypeOf(@Nonnull RawVcsReference rawReference) throws VcsException {
        return fixedVcsTypeFor(rawReference, git);
    }

    @Nonnull
    @Override
    protected String rootFor(@Nonnull Matcher matcher, @Nonnull RawVcsReference rawReference) throws VcsException {
        return "github.com/golang/" + matcher.group("repo");
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
        return "golang.org/x";
    }

}
