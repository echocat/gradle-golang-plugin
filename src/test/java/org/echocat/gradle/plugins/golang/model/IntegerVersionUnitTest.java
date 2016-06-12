package org.echocat.gradle.plugins.golang.model;

import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

public class IntegerVersionUnitTest {

    @Test
    public void compareTo() throws Exception {
        final Set<IntegerVersion> versions = new TreeSet<>();
        versions.add(new IntegerVersion("1"));
        versions.add(new IntegerVersion("1.1"));
        versions.add(new IntegerVersion("1.0.1"));
        versions.add(new IntegerVersion("1.3.1"));
        versions.add(new IntegerVersion("1.0"));
        versions.add(new IntegerVersion("2.0.1"));
        versions.add(new IntegerVersion("2.1.1"));
        versions.add(new IntegerVersion("2"));
        versions.add(new IntegerVersion("2.0"));

        //noinspection UseOfSystemOutOrSystemErr
        System.out.println(versions.toString());

    }

}
