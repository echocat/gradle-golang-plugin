package org.echocat.gradle.plugins.golang.vcs.isps;

import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsException;
import org.echocat.gradle.plugins.golang.vcs.VcsType;

import javax.annotation.Nonnull;

import static java.util.regex.Pattern.compile;

public class GitApacheVcsRepositoryProvider extends IspBasedVcsRepositoryProviderSupport {

    public GitApacheVcsRepositoryProvider() {
        super("git.apache.org/", compile("^(?<root>git.apache.org/[a-z0-9_.\\-]+\\.git)(?<subPath>/[A-Za-z0-9_.\\-]+)*$"));
    }

    @Nonnull
    @Override
    protected VcsType detectVcsTypeOf(@Nonnull RawVcsReference rawReference) throws VcsException {
        return fixedVcsTypeFor(rawReference, VcsType.git);
    }

    @Nonnull
    @Override
    protected String getName() {
        return "Git at Apache";
    }

}
