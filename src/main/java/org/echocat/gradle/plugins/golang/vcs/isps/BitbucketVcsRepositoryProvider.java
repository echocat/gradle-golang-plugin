package org.echocat.gradle.plugins.golang.vcs.isps;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.echocat.gradle.plugins.golang.vcs.*;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;

import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static org.echocat.gradle.plugins.golang.vcs.VcsType.git;
import static org.echocat.gradle.plugins.golang.vcs.VcsType.hg;

public class BitbucketVcsRepositoryProvider extends IspBasedVcsRepositoryProviderSupport {

    @Nonnull
    protected static final String BITBUCKET_REPOSITORIES_API_URI = "https://api.bitbucket.org/1.0/repositories/";

    public BitbucketVcsRepositoryProvider() {
        super("bitbucket.org/", compile("^(?<root>bitbucket\\.org/(?<bitname>[A-Za-z0-9_.\\-]+/[A-Za-z0-9_.\\-]+))(?<subPath>/[A-Za-z0-9_.\\-]+)*$"));
    }

    @Nonnull
    @Override
    protected VcsType detectVcsTypeOf(@Nonnull RawVcsReference rawReference) throws VcsException {
        final Matcher matcher = nameMatcherFor(rawReference);
        final String uri = BITBUCKET_REPOSITORIES_API_URI + matcher.group("bitname");

        final OkHttpClient client = new OkHttpClient();
        final Gson gson = new Gson();
        final Request request = new Builder()
            .url(uri)
            .build();

        final Response response;
        try {
            response = client.newCall(request).execute();
        } catch (final Exception e) {
            throw new VcsException("Could not get data from " + uri + ".", e);
        }
        final int code = response.code();
        if (code > 0 && code < 400) {
            final BitkucketRepository repository;
            try {
                repository = gson.fromJson(response.body().charStream(), BitkucketRepository.class);
            } catch (final Exception e) {
                throw new VcsException("Could not decode response of " + uri + ".", e);
            }
            try {
                return VcsType.valueOf(repository.getScm());
            } catch (final IllegalArgumentException ignored) {
                throw new VcsValidationException("Unable to detect version control system for bitbucket.org/ path");
            }
        } else if (code == 403) {
            for (final VcsType candidate : asList(git, hg)) {
                try {
                    final VcsReference reference = detectVcsUriOf(rawReference, candidate);
                    final VcsRepository vcsRepository = vcsFactory().createFor(reference);
                    if (vcsRepository.isWorking()) {
                        return candidate;
                    }
                } catch (final VcsException ignored) {}
            }
            throw new VcsException("Could not get data from " + uri + ".");
        } else {
            throw new VcsException("Got unexpected error code from " + uri + ": " + code);
        }
    }

    @Nonnull
    @Override
    protected String getName() {
        return "Bitbucket";
    }

    public static class BitkucketRepository {

        @SerializedName(value = "scm")
        private String _scm;

        public String getScm() {
            return _scm;
        }

        public void setScm(String scm) {
            _scm = scm;
        }
    }
}
