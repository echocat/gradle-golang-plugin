package org.echocat.gradle.plugins.golang.vcs.isps;

import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsException;
import org.echocat.gradle.plugins.golang.vcs.VcsType;
import org.echocat.gradle.plugins.golang.vcs.VcsValidationException;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;

public class SuffixDetectingVcsRepositoryProvider extends VcsRepositoryProviderSupport {

    public SuffixDetectingVcsRepositoryProvider() {
        super(compile("^(?<root>(?<repo>([a-z0-9.\\-]+\\.)+[a-z0-9.\\-]+(:[0-9]+)?(/~?[A-Za-z0-9_.\\-]+)+?)\\.(?<vcs>bzr|git|hg|svn))(?<path>/~?[A-Za-z0-9_.\\-]+)*$"));
    }

    @Override
    @Nonnull
    protected VcsType detectVcsTypeOf(@Nonnull RawVcsReference rawReference) throws VcsException {
        final Matcher matcher = nameMatcherFor(rawReference);
        final VcsType type = VcsType.valueOf(matcher.group("vcs"));
        return fixedVcsTypeFor(rawReference, type);
    }

    @Override
    protected boolean couldHandle(@Nonnull RawVcsReference rawReference) {
        return dependencyPattern().matcher(rawReference.getId()).matches();
    }

    @Override
    @Nonnull
    protected Matcher nameMatcherFor(@Nonnull RawVcsReference rawReference) throws VcsException {
        final Matcher matcher = dependencyPattern().matcher(rawReference.getId());
        if (!matcher.matches()) {
            throw new VcsValidationException("Name of dependency " + rawReference + " is invalid.");
        }
        return matcher;
    }

}
