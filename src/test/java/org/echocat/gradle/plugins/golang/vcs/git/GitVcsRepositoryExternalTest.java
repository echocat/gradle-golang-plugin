package org.echocat.gradle.plugins.golang.vcs.git;

import org.echocat.gradle.plugins.golang.vcs.VcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsValidationException;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Paths;

import static org.echocat.gradle.plugins.golang.vcs.VcsReference.vcsReference;
import static org.echocat.gradle.plugins.golang.vcs.VcsType.git;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class GitVcsRepositoryExternalTest {

    private static final String NAME = "github.com/echocat/caretakerd";
    private static final String URI = "https://github.com/echocat/caretakerd.git";

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() throws Exception {
        new GitVcsRepository(referenceFor("refs/tags/v0.1.12")).updateIfRequired(Paths.get("build/test/123"));

        assertThat(new GitVcsRepository(referenceFor(null)).resolveRemoteRef().getName(), equalTo("HEAD"));
        assertThat(new GitVcsRepository(referenceFor("master")).resolveRemoteRef().getName(), equalTo("refs/heads/master"));
        assertThat(new GitVcsRepository(referenceFor("refs/master")).resolveRemoteRef(), nullValue());
        assertThat(new GitVcsRepository(referenceFor("refs/heads/master")).resolveRemoteRef().getName(), equalTo("refs/heads/master"));
        assertThat(new GitVcsRepository(referenceFor("v0.1.12")).resolveRemoteRef().getName(), equalTo("refs/tags/v0.1.12"));
        assertThat(new GitVcsRepository(referenceFor("refs/v0.1.12")).resolveRemoteRef(), nullValue());
        assertThat(new GitVcsRepository(referenceFor("refs/tags/v0.1.12")).resolveRemoteRef().getName(), equalTo("refs/tags/v0.1.12"));

    }

    @Nonnull
    protected VcsReference referenceFor(@Nullable String ref) throws VcsValidationException {
        return vcsReference(git, NAME, URI, ref, null);
    }

}
