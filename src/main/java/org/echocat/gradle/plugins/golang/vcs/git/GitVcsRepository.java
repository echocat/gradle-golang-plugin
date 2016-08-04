package org.echocat.gradle.plugins.golang.vcs.git;

import com.sun.nio.sctp.IllegalReceiveException;
import org.echocat.gradle.plugins.golang.vcs.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.HttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.echocat.gradle.plugins.golang.utils.FileUtils.delete;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.deleteQuietly;
import static org.echocat.gradle.plugins.golang.vcs.git.GitVcsUri.gitVcsUriFor;

public class GitVcsRepository extends VcsRepositorySupport {

    static {
        HttpTransport.setConnectionFactory(new HttpConnectionFactoryImpl());
    }

    protected static final Pattern SPLIT_PATH_PATTERN = Pattern.compile("^(?<root>/[A-Za-z0-9_.\\-/]+?\\.git)(?<subPath>/.*|)$");
    private static final Logger LOGGER = LoggerFactory.getLogger(VcsRepositorySupport.class);

    public GitVcsRepository(@Nonnull VcsReference ref) {
        super(ref);
    }

    @Override
    public boolean isWorking() throws VcsException {
        try {
            return resolveRemoteRef() != null;
        } catch (final IllegalReceiveException ignored) {
            return false;
        } catch (final GitAPIException e) {
            throw new VcsException(e);
        }
    }

    @Override
    @Nonnull
    public VcsFullReference downloadToInternal(@Nonnull Path targetDirectory) throws VcsException {
        Ref ref = null;
        final GitVcsUri uri = gitVcsUriFor(getReference());
        final Git git;
        try {
            ref = resolveRemoteRef();
            if (ref == null) {
                throw new VcsValidationException("Could not find ref " + uri + "@" + getReference().getRef() + ".");
            }
            final String refName = ref.getName();
            LOGGER.debug("Clone remote refs from {}@{} to {}...", uri, refName, targetDirectory);
            git = Git.cloneRepository()
                .setURI(uri.getUri().toString())
                .setDirectory(targetDirectory.toFile())
                .setBranch(refName)
                .call();
            LOGGER.debug("Clone remote refs from {}@{} to {}... DONE!", uri, refName, targetDirectory);
        } catch (final GitAPIException e) {
            throw new VcsException("Cannot clone " + uri + "@" + (ref != null ? ref.getName() : "unresolved") + " to " + targetDirectory + ".", e);
        }
        final String fullRevision = fullRevisionOf(git);
        removeGitDirectoryIfNeeded(targetDirectory, git);
        return new VcsFullReference(getReference(), fullRevision);
    }

    protected void removeGitDirectoryIfNeeded(@Nonnull Path targetDirectory, @Nonnull Git git) throws VcsException {
        git.getRepository().close();
        deleteQuietly(targetDirectory.resolve(".git"));
    }

    @Nonnull
    protected String fullRevisionOf(@Nonnull Git git) throws VcsException {
        try {
            return git.getRepository().resolve(Constants.HEAD).name();
        } catch (final IOException e) {
            throw new VcsException("Could not get current head revision from " + git + ".", e);
        }
    }

    @Nullable
    protected Ref resolveRemoteRef() throws VcsException, GitAPIException {
        LOGGER.debug("Fetch remote refs from {}...", getReference().getUri());
        final Iterable<Ref> refs = RefComparator.sort(Git.lsRemoteRepository()
            .setRemote(gitVcsUriFor(getReference()).getUri().toString())
            .call());
        LOGGER.debug("Fetch remote refs from {}... DONE!", getReference().getUri());
        return selectFirstMatching(refs);
    }

    @Nullable
    protected Ref selectFirstMatching(@Nullable Iterable<Ref> refs) throws VcsException {
        if (refs == null) {
            return null;
        }
        for (final Ref ref : refs) {
            if (match(ref)) {
                return ref;
            }
        }
        return null;
    }

    protected boolean match(@Nullable Ref ref) throws VcsException {
        if (ref == null) {
            return false;
        }
        return match(ref.getName());
    }

    protected boolean match(@Nullable String ref) throws VcsException {
        if (ref == null) {
            return false;
        }
        String expected = getReference().getRef();
        if (expected == null) {
            expected = "HEAD";
        }
        if (expected.equals(ref)) {
            return true;
        }
        if (("refs/tags/" + expected).equals(ref)) {
            return true;
        }
        if (("refs/heads/" + expected).equals(ref)) {
            return true;
        }
        return expected.equals(ref);
    }


}
