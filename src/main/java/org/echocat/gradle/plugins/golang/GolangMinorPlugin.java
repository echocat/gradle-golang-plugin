package org.echocat.gradle.plugins.golang;

import org.codehaus.plexus.util.StringUtils;

import javax.annotation.Nonnull;

public class GolangMinorPlugin extends GolangPluginSupport {

    @Nonnull
    @Override
    protected String realTaskNameFor(@Nonnull String simpleTaskName) {
        return "golang" + StringUtils.capitalise(simpleTaskName);
    }

}
