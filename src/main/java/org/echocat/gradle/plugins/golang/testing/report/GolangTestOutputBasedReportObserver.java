package org.echocat.gradle.plugins.golang.testing.report;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.echocat.gradle.plugins.golang.testing.report.Result.*;

public class GolangTestOutputBasedReportObserver implements ReportObserver {

    private static final char LINE_SEPARATOR = Objects.equals(getProperty("line.separator", "\n"), "\r") ? '\r' : '\n';
    private static final Pattern START_PATTERN = Pattern.compile("^=== RUN\\s+(.+)\\s*$");
    private static final Pattern STATUS_PATTERN = Pattern.compile("^\\s*--- (PASS|FAIL|SKIP): (.+) \\((\\d+\\.\\d+)(?: seconds|s)\\)$");
    private static final Pattern COVERAGE_PATTERN = Pattern.compile("^coverage:\\s+(\\d+\\.\\d+)%\\s+of\\s+statements$");
    private static final Pattern RESULT_PATTERN = Pattern.compile("^(ok|FAIL)\\s+(.+)\\s(\\d+\\.\\d+)s(?:\\s+coverage:\\s+(\\d+\\.\\d+)%\\s+of\\s+statements)?$");
    private static final Pattern RESULT_OUTPUT_PATTERN = Pattern.compile("(    )*\\t(.*)");

    @Nonnull
    private final Charset _charset;
    @Nonnull
    private final String _packageName;
    @Nonnull
    private final ByteArrayOutputStream _currentStdLine = new ByteArrayOutputStream();
    @Nonnull
    private final ByteArrayOutputStream _currentErrLine = new ByteArrayOutputStream();
    @Nonnull
    private final List<Notifier> _notifiers = new ArrayList<>();
    @Nonnull
    private final Report _report = new Report();
    @Nonnull
    private Map<String, Test> _tests = new LinkedHashMap<>();
    @Nonnegative
    private double _testsTime;
    @Nonnull
    private String _coveragePct = "";
    @Nullable
    private Test _current;
    @Nullable
    private Test _resultOutputTest;

    @Nonnull
    private final OutputStream _stdout = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            writeStdout(b);
        }

        @Override
        public void close() throws IOException {
            closeStdout();
        }
    };

    @Nonnull
    private final OutputStream _stderr = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
            writeStderr(b);
        }
    };

    public GolangTestOutputBasedReportObserver(@Nonnull String packageName) {
        this(packageName, Charset.defaultCharset());
    }

    public GolangTestOutputBasedReportObserver(@Nonnull String packageName, @Nonnull Charset charset) {
        _packageName = packageName;
        _charset = charset;
    }

    protected synchronized void writeStdout(int b) throws IOException {
        if (b == LINE_SEPARATOR) {
            final String line = new String(_currentStdLine.toByteArray(), _charset);
            handleStdLine(line);
            _currentStdLine.reset();
        } else {
            _currentStdLine.write(b);
        }
    }

    protected synchronized void closeStdout() throws IOException {
        if (!_tests.isEmpty()) {
            // no result line found
            _report.addPackage(
                new Package()
                    .setName(_packageName)
                    .setTime(_testsTime)
                    .addTest(_tests.values())
                    .setCoveragePct(_coveragePct)
            );
        }
    }

    protected synchronized void writeStderr(int b) throws IOException {
        if (b == LINE_SEPARATOR) {
            final String line = new String(_currentErrLine.toByteArray(), _charset);
            handleErrLine(line);
            _currentErrLine.reset();
        } else {
            _currentErrLine.write(b);
        }
    }

    protected synchronized void handleStdLine(@Nonnull String line) throws IOException {
        Matcher matcher;
        if ((matcher = START_PATTERN.matcher(line)).matches()) {
            _resultOutputTest = null;

            // new test
            final String name = matcher.group(1);
            _current = new Test()
                .setName(name)
                .setResult(FAIL)
            ;
            _tests.put(name, _current);
            for (final Notifier notifier : _notifiers) {
                notifier.onTestStarted(name);
            }
        } else if ((matcher = RESULT_PATTERN.matcher(line)).matches()) {
            if (isNotEmpty(matcher.group(4))) {
                _coveragePct = matcher.group(4);
            }

            // all tests in this package are finished
            _report.addPackage(
                new Package()
                    .setName(matcher.group(2))
                    .setTime(parseTime(matcher.group(3)))
                    .addTest(_tests.values())
                    .setCoveragePct(_coveragePct)
            );
            _tests = new LinkedHashMap<>();
            _current = null;
            _currentErrLine.reset();
            _resultOutputTest = null;
            _coveragePct = "";
            _testsTime = 0.0d;
        } else if ((matcher = STATUS_PATTERN.matcher(line)).matches()) {
            final Test test = _tests.get(matcher.group(2));
            if (test != null) {
                final String status = matcher.group(1);
                // test status
                if (Objects.equals(status, "PASS")) {
                    test.setResult(PASS);
                } else if (Objects.equals(status, "SKIP")) {
                    test.setResult(SKIP);
                } else {
                    test.setResult(FAIL);
                }

                test.setName(matcher.group(2));
                final double testTime = parseTime(matcher.group(3));
                test.setTime(testTime);
                _testsTime += testTime;
                _current = null;
                _currentErrLine.reset();
                _resultOutputTest = test;
            }
        } else if ((matcher = COVERAGE_PATTERN.matcher(line)).matches()) {
            _coveragePct = matcher.group(1);
        } else if (_resultOutputTest != null && (matcher = RESULT_OUTPUT_PATTERN.matcher(line)).matches()) {
            // Sub-tests start with one or more series of 4-space indents, followed by a hard tab,
            // followed by the test output
            // Top-level tests start with a hard tab.
            _resultOutputTest.addResultOutput(matcher.group(2));
        } else if (_current != null) {
            _current.addSystemOut(line + LINE_SEPARATOR);
        }
    }

    protected synchronized void handleErrLine(@Nonnull String line) throws IOException {
        if (_current != null) {
            _current.addSystemErr(line + LINE_SEPARATOR);
        }
    }

    @Nonnegative
    protected double parseTime(String time) {
        try {
            return parseDouble(time);
        } catch (final NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    @Nonnull
    public OutputStream out() {
        return _stdout;
    }

    @Override
    @Nonnull
    public OutputStream err() {
        return _stderr;
    }

    @Override
    @Nonnull
    public Report getReport() {
        return _report;
    }

    @Override
    public synchronized void registerNotifier(@Nonnull Notifier notifier) {
        _notifiers.add(notifier);
    }

    @Override
    public void close() throws IOException {
        closeStdout();
    }

}
