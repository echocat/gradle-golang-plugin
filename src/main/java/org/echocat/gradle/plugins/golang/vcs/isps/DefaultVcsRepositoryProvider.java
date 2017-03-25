package org.echocat.gradle.plugins.golang.vcs.isps;

import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsException;
import org.echocat.gradle.plugins.golang.vcs.VcsType;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class DefaultVcsRepositoryProvider extends IspBasedVcsRepositoryProviderSupport {

    @Nonnull
    private final VcsType _type;
    @Nonnull
    private final String _name;

    public DefaultVcsRepositoryProvider(@Nonnull VcsType type, @Nonnull String prefix, @Nonnull String name, @Nonnull Pattern dependencyPattern) {
        super(prefix, dependencyPattern);
        _type = type;
        _name = name;
    }

    @Nonnull
    @Override
    protected VcsType detectVcsTypeOf(@Nonnull RawVcsReference rawReference) throws VcsException {
        return fixedVcsTypeFor(rawReference, _type);
    }

    @Nonnull
    @Override
    protected String getName() {
        return _name;
    }

}
