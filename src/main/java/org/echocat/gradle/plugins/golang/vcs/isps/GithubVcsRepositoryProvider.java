package org.echocat.gradle.plugins.golang.vcs.isps;

import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsException;
import org.echocat.gradle.plugins.golang.vcs.VcsType;

import javax.annotation.Nonnull;

import static java.util.regex.Pattern.compile;

public class GithubVcsRepositoryProvider extends IspBasedVcsRepositoryProviderSupport {

    public GithubVcsRepositoryProvider() {
        super("github.com/", compile("^(?<root>github\\.com/[A-Za-z0-9_.\\-]+/[A-Za-z0-9_.\\-]+)(?<subPath>/[A-Za-z0-9_.\\-]+)*$"));
    }

    @Nonnull
    @Override
    protected VcsType detectVcsTypeOf(@Nonnull RawVcsReference rawReference) throws VcsException {
        return fixedVcsTypeFor(rawReference, VcsType.git);
    }

    @Nonnull
    @Override
    protected String getName() {
        return "GitHub";
    }

}
