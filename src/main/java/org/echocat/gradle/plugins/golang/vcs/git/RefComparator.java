package org.echocat.gradle.plugins.golang.vcs.git;

import org.eclipse.jgit.lib.Ref;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class RefComparator implements Comparator<Ref> {

    @Nonnull
    protected static Set<Ref> sort(@Nullable Collection<Ref> input) {
        final Set<Ref> output = new TreeSet<>(REF_COMPARATOR);
        if (input != null) {
            output.addAll(input);
        }
        return output;
    }

    protected static final Comparator<Ref> REF_COMPARATOR = new RefComparator();

    @Override
    public int compare(Ref o1, Ref o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        final String n1 = o1.getName();
        final String n2 = o2.getName();
        if ("HEAD".equals(n1) && !"HEAD".equals(n2)) {
            return -1;
        }
        if (!"HEAD".equals(n1) && "HEAD".equals(n2)) {
            return 1;
        }
        if (n1.startsWith("refs/tags/") && !n2.startsWith("refs/tags/")) {
            return -1;
        }
        if (!n1.startsWith("refs/tags/") && n2.startsWith("refs/tags/")) {
            return 1;
        }
        if (n1.startsWith("refs/heads/") && !n2.startsWith("refs/heads/")) {
            return -1;
        }
        if (!n1.startsWith("refs/heads/") && n2.startsWith("refs/heads/")) {
            return 1;
        }
        return n1.compareTo(n2);
    }
}
