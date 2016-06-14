package org.echocat.gradle.plugins.golang.model;

import org.echocat.gradle.plugins.golang.model.BuildSettings.Argument;
import org.echocat.gradle.plugins.golang.utils.BeanUtils;
import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;

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
     *  Apply coverage analysis in each test to the given list of packages.
     * The default is for each test to analyze only the package being tested.
     * Packages are specified as import paths.
     * Sets -cover.
     */
    @Argument("-coverpkg")
    private String _coverPackages;
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
    /**
     * Write a coverage profile to the file after all tests have passed.
     * Sets -cover.
     */
    @Argument("-coverprofile")
    private File _coverProfile;
    /**
     * Specify a list of GOMAXPROCS values for which the tests or
     * benchmarks should be executed.  The default is the current value
     * of GOMAXPROCS.
     */
    @Argument("-cpu")
    private String _cpu;
    /**
     * Write a CPU profile to the specified file before exiting.
     * Writes test binary as -c would.
     */
    @Argument("-cpuprofile")
    private File _cpuProfile;
    /**
     * Write a memory profile to the file after all tests have passed.
     * Writes test binary as -c would.
     */
    @Argument("-memprofile")
    private File _memProfile;
    /**
     * Enable more precise (and expensive) memory profiles by setting
     * runtime.MemProfileRate.  See 'go doc runtime.MemProfileRate'.
     * To profile all memory allocations, use -test.memprofilerate=1
     * and pass --alloc_space flag to the pprof tool.
     */
    @Argument("-memprofilerate")
    private Integer _memProfileRate;
    /**
     * Allow parallel execution of test functions that call t.Parallel.
     * The value of this flag is the maximum number of tests to run
     * simultaneously; by default, it is set to the value of GOMAXPROCS.
     * Note that -parallel only applies within a single test binary.
     * The 'go test' command may run tests for different packages
     * in parallel as well, according to the setting of the -p flag
     * (see 'go help build').
     */
    @Argument("-parallel")
    private Integer _parallel;
    /**
     * Run only those tests and examples matching the regular
     * expression.
     */
    @Argument("-run")
    private String _tests;
    /**
     * Tell long-running tests to shorten their run time.
     * It is off by default but set during all.bash so that installing
     * the Go tree can run a sanity check but not spend time running
     * exhaustive tests.
     */
    @Argument("-short")
    private Boolean _short;
    /**
     * If a test runs longer than t, panic.
     * The default is 10 minutes (10m).
     */
    @Argument("-timeout")
    private String _timeout;
    /**
     * Write an execution trace to the specified file before exiting.
     * Writes test binary as -c would.
     */
    @Argument("-trace")
    private File _trace;
    /**
     * Verbose output: log all tests as they are run. Also print all
     * text from Log and Logf calls even if the test succeeds.
     */
    @Argument("-verbose")
    private boolean _verbose;

    @Inject
    public TestingSettings(boolean initialize, @Nonnull Project project) {
        _project = project;
        if (initialize) {
            _excludes = new String[]{
                ".git/**", ".svn/**", "build.gradle", "build/**", ".gradle/**", "gradle/**", "vendor"
            };
        }
    }

    public String[] getIncludes() {
        return _includes;
    }

    public TestingSettings setIncludes(String[] includes) {
        _includes = includes;
        return this;
    }

    public String[] getExcludes() {
        return _excludes;
    }

    public TestingSettings setExcludes(String[] excludes) {
        _excludes = excludes;
        return this;
    }

    public String[] getArguments() {
        return _arguments;
    }

    public TestingSettings setArguments(String[] arguments) {
        _arguments = arguments;
        return this;
    }

    public String getBench() {
        return _bench;
    }

    public TestingSettings setBench(String bench) {
        _bench = bench;
        return this;
    }

    public Boolean getBenchMem() {
        return _benchMem;
    }

    public TestingSettings setBenchMem(Boolean benchMem) {
        _benchMem = benchMem;
        return this;
    }

    public String getBenchTime() {
        return _benchTime;
    }

    public TestingSettings setBenchTime(String benchTime) {
        _benchTime = benchTime;
        return this;
    }

    public File getBlockProfile() {
        return _blockProfile;
    }

    public TestingSettings setBlockProfile(File blockProfile) {
        _blockProfile = blockProfile;
        return this;
    }

    public Integer getBlockProfileRate() {
        return _blockProfileRate;
    }

    public TestingSettings setBlockProfileRate(Integer blockProfileRate) {
        _blockProfileRate = blockProfileRate;
        return this;
    }

    public Integer getCount() {
        return _count;
    }

    public TestingSettings setCount(Integer count) {
        _count = count;
        return this;
    }

    public Boolean getCover() {
        return _cover;
    }

    public TestingSettings setCover(Boolean cover) {
        _cover = cover;
        return this;
    }

    public String getCoverPackages() {
        return _coverPackages;
    }

    public TestingSettings setCoverPackages(String coverPackages) {
        _coverPackages = coverPackages;
        return this;
    }

    public String getCoverMode() {
        return _coverMode;
    }

    public TestingSettings setCoverMode(String coverMode) {
        _coverMode = coverMode;
        return this;
    }

    public File getCoverProfile() {
        return _coverProfile;
    }

    public TestingSettings setCoverProfile(File coverProfile) {
        _coverProfile = coverProfile;
        return this;
    }

    public String getCpu() {
        return _cpu;
    }

    public TestingSettings setCpu(String cpu) {
        _cpu = cpu;
        return this;
    }

    public File getCpuProfile() {
        return _cpuProfile;
    }

    public TestingSettings setCpuProfile(File cpuProfile) {
        _cpuProfile = cpuProfile;
        return this;
    }

    public File getMemProfile() {
        return _memProfile;
    }

    public TestingSettings setMemProfile(File memProfile) {
        _memProfile = memProfile;
        return this;
    }

    public Integer getMemProfileRate() {
        return _memProfileRate;
    }

    public TestingSettings setMemProfileRate(Integer memProfileRate) {
        _memProfileRate = memProfileRate;
        return this;
    }

    public Integer getParallel() {
        return _parallel;
    }

    public TestingSettings setParallel(Integer parallel) {
        _parallel = parallel;
        return this;
    }

    public String getTests() {
        return _tests;
    }

    public TestingSettings setTests(String tests) {
        _tests = tests;
        return this;
    }

    public Boolean getShort() {
        return _short;
    }

    public TestingSettings setShort(Boolean aShort) {
        _short = aShort;
        return this;
    }

    public String getTimeout() {
        return _timeout;
    }

    public TestingSettings setTimeout(String timeout) {
        _timeout = timeout;
        return this;
    }

    public File getTrace() {
        return _trace;
    }

    public TestingSettings setTrace(File trace) {
        _trace = trace;
        return this;
    }

    public boolean isVerbose() {
        return _verbose;
    }

    public TestingSettings setVerbose(boolean verbose) {
        _verbose = verbose;
        return this;
    }

    @Nonnull
    public TestingSettings merge(@Nonnull TestingSettings with) {
        final TestingSettings result = new TestingSettings(false, _project);
        BeanUtils.copyNonNulls(TestingSettings.class, this, result);
        BeanUtils.copyNonNulls(TestingSettings.class, with, result);
        return result;
    }

}
