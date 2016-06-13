package org.echocat.gradle.plugins.golang.model;

import groovy.lang.Closure;
import org.echocat.gradle.plugins.golang.model.BuildSettings.Argument;
import org.echocat.gradle.plugins.golang.utils.BeanUtils;
import org.gradle.api.Project;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.io.File.pathSeparator;
import static java.io.File.separator;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class TestingSettings {

    @Nonnull
    private final Project _project;

    private String[] _includes;
    private String[] _excludes;
    private String[] _arguments;


    /**
     * Run benchmarks matching the regular expression.
     * By default, no benchmarks run. To run all benchmarks, use '.'.
     */
    @Argument("-bench")
    private String _bench;
    /**
     * Print memory allocation statistics for benchmarks.
     */
    @Argument("-benchmem")
    private Boolean _benchMem;
    /**
     * Run enough iterations of each benchmark to take t, specified as a time.Duration (for example, 1h30s).
     * The default is 1 second (1s).
     */
    @Argument("-benchtime")
    private String _benchTime;
    /**
     * Write a goroutine blocking profile to the specified file when all tests are complete.
     * Writes test binary as -c would.
     */
    @Argument("-blockprofile")
    private File _blockProfile;
    /**
     * Control the detail provided in goroutine blocking profiles by
     * calling runtime.SetBlockProfileRate with n.
     * See 'go doc runtime.SetBlockProfileRate'.
     * The profiler aims to sample, on average, one blocking event every
     * n nanoseconds the program spends blocked.  By default,
     * if -test.blockprofile is set without this flag, all blocking events
     * are recorded, equivalent to -test.blockprofilerate=1.
     */
    @Argument("-blockprofilerate")
    private Integer _blockProfileRate;
    /**
     * Run each test and benchmark n times (default 1).
     * If -cpu is set, run n times for each GOMAXPROCS value.
     * Examples are always run once.
     */
    @Argument("-count")
    private Integer _count;
    /**
     * Enabled code coverage.
     */
    @Argument("-cover")
    private Boolean _cover;
    /**
     * Set the mode for coverage analysis for the package[s]
     * being tested. The default is "set" unless -race is enabled,
     * in which case it is "atomic".
     * The values:
     * set: bool: does this statement run?
     * count: int: how many times does this statement run?
     * atomic: int: count, but correct in multithreaded tests;
     * significantly more expensive.
     * Sets -cover.
     */
    @Argument("-covermode")
    private String _coverMode;


    @Inject
    public TestingSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _excludes = new String[]{
                ".git/**", ".svn/**", "build.gradle", "build/**", ".gradle/**", "gradle/**", "vendor"
            };
        }
    }

    @Nonnull
    public TestingSettings merge(@Nonnull TestingSettings with) {
        final TestingSettings result = new TestingSettings(false, _project);
        BeanUtils.copyNonNulls(TestingSettings.class, this, result);
        BeanUtils.copyNonNulls(TestingSettings.class, with, result);
        return result;
    }

}
