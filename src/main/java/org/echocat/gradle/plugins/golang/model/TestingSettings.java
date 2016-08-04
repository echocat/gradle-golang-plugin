package org.echocat.gradle.plugins.golang.model;

import org.echocat.gradle.plugins.golang.utils.Arguments;
import org.echocat.gradle.plugins.golang.utils.Arguments.Argument;
import org.gradle.api.Project;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class TestingSettings {

    @Nonnull
    private final Project _project;

    private boolean _skip;

    private String[] _packages;
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
    private String _blockProfile;
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
    private String _coverProfile;
    /**
     * Write a coverage profile as HTML to the file after all tests have passed.
     * Sets -cover.
     */
    private String _coverProfileHtml;
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
    private String _cpuProfile;
    /**
     * Write a memory profile to the file after all tests have passed.
     * Writes test binary as -c would.
     */
    @Argument("-memprofile")
    private String _memProfile;
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
    private String _trace;
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
            _includes = new String[] {
                "**/*_test.go"
            };
            _excludes = new String[]{
                ".git/**", ".svn/**", "build.gradle", "build/**", ".gradle/**", "gradle/**", "vendor/**"
            };
        }
    }

    public boolean isSkip() {
        return _skip;
    }

    public void setSkip(boolean skip) {
        _skip = skip;
    }

    public String[] getPackages() {
        return _packages;
    }

    public void setPackages(String[] packages) {
        _packages = packages;
    }

    public String[] getIncludes() {
        return _includes;
    }

    public void setIncludes(String[] includes) {
        _includes = includes;
    }

    public String[] getExcludes() {
        return _excludes;
    }

    public void setExcludes(String[] excludes) {
        _excludes = excludes;
    }

    public String[] getArguments() {
        return _arguments;
    }

    public void setArguments(String[] arguments) {
        _arguments = arguments;
    }

    public String getBench() {
        return _bench;
    }

    public void setBench(String bench) {
        _bench = bench;
    }

    public Boolean getBenchMem() {
        return _benchMem;
    }

    public void setBenchMem(Boolean benchMem) {
        _benchMem = benchMem;
    }

    public String getBenchTime() {
        return _benchTime;
    }

    public void setBenchTime(String benchTime) {
        _benchTime = benchTime;
    }

    public String getBlockProfile() {
        return _blockProfile;
    }

    public void setBlockProfile(String blockProfile) {
        _blockProfile = blockProfile;
    }

    public Integer getBlockProfileRate() {
        return _blockProfileRate;
    }

    public void setBlockProfileRate(Integer blockProfileRate) {
        _blockProfileRate = blockProfileRate;
    }

    public Integer getCount() {
        return _count;
    }

    public void setCount(Integer count) {
        _count = count;
    }

    public Boolean getCover() {
        return _cover;
    }

    public void setCover(Boolean cover) {
        _cover = cover;
    }

    public String getCoverPackages() {
        return _coverPackages;
    }

    public void setCoverPackages(String coverPackages) {
        _coverPackages = coverPackages;
    }

    public String getCoverMode() {
        return _coverMode;
    }

    public void setCoverMode(String coverMode) {
        _coverMode = coverMode;
    }

    public String getCoverProfile() {
        return _coverProfile;
    }

    public void setCoverProfile(String coverProfile) {
        _coverProfile = coverProfile;
    }

    public Path getCoverProfileFile() {
        final String plain = _coverProfile;
        return plain != null ? Paths.get(plain) : null;
    }

    public String getCoverProfileHtml() {
        return _coverProfileHtml;
    }

    public void setCoverProfileHtml(String coverProfileHtml) {
        _coverProfileHtml = coverProfileHtml;
    }

    public Path getCoverProfileHtmlFile() {
        final String plain = _coverProfileHtml;
        return plain != null ? Paths.get(plain) : null;
    }

    public String getCpu() {
        return _cpu;
    }

    public void setCpu(String cpu) {
        _cpu = cpu;
    }

    public String getCpuProfile() {
        return _cpuProfile;
    }

    public void setCpuProfile(String cpuProfile) {
        _cpuProfile = cpuProfile;
    }

    public String getMemProfile() {
        return _memProfile;
    }

    public void setMemProfile(String memProfile) {
        _memProfile = memProfile;
    }

    public Integer getMemProfileRate() {
        return _memProfileRate;
    }

    public void setMemProfileRate(Integer memProfileRate) {
        _memProfileRate = memProfileRate;
    }

    public Integer getParallel() {
        return _parallel;
    }

    public void setParallel(Integer parallel) {
        _parallel = parallel;
    }

    public String getTests() {
        return _tests;
    }

    public void setTests(String tests) {
        _tests = tests;
    }

    public Boolean getShort() {
        return _short;
    }

    public void setShort(Boolean aShort) {
        _short = aShort;
    }

    public String getTimeout() {
        return _timeout;
    }

    public void setTimeout(String timeout) {
        _timeout = timeout;
    }

    public String getTrace() {
        return _trace;
    }

    public void setTrace(String trace) {
        _trace = trace;
    }

    public boolean isVerbose() {
        return _verbose;
    }

    public void setVerbose(boolean verbose) {
        _verbose = verbose;
    }

    @Nonnull
    public Map<String, String> additionalArgumentMap() {
        return Arguments.argumentMapOf(TestingSettings.class, this);
    }

}
