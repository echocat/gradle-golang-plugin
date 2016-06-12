package org.echocat.gradle.plugins.golang.vcs;

import org.echocat.gradle.plugins.golang.model.UpdatePolicy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;

public class RawVcsReference extends BaseVcsReference {

    public RawVcsReference(@Nullable VcsType type, @Nonnull String id, @Nullable URI plain, @Nullable String ref, UpdatePolicy updatePolicy) {
        super(type, id, plain, ref, updatePolicy);
    }

}
