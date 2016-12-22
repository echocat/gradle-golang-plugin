package org.echocat.gradle.plugins.golang.testing.report;

import org.echocat.gradle.plugins.golang.testing.report.junit.*;

import javax.annotation.Nonnull;

import static org.apache.commons.lang3.StringUtils.join;
import static org.echocat.gradle.plugins.golang.testing.report.Result.FAIL;
import static org.echocat.gradle.plugins.golang.testing.report.Result.SKIP;

public class ReportTransformer {

    @Nonnull
    public TestSuites transformToJunit(@Nonnull Report report) {
        final TestSuites result = new TestSuites();

        for (final Package pkg : report) {
            final TestSuite ts = new TestSuite()
                .setFailures(pkg.getNumberOfFailures())
                .setTests(pkg.getNumberOfTestCases())
                .setTime(formatTime(pkg.getTime()))
                .setName(pkg.getName())
                ;

            String classname = pkg.getName();
            final int idx = classname.lastIndexOf('/');
            if (idx > -1 && idx < pkg.getName().length()) {
                classname = pkg.getName().substring(idx + 1);
            }

            // properties
            if (!pkg.getCoveragePct().isEmpty()) {
                ts.addProperty("coverage.statements.pct", pkg.getCoveragePct());
            }

            // individual test cases
            for (final Test test : pkg) {
                final TestCase testCase = new TestCase()
                    .setClassName(classname)
                    .setName(test.getName())
                    .setTime(formatTime(test.getTime()))
                    .setSystemOut(test.getSystemOut())
                    .setSystemErr(test.getSystemErr())
                    ;

                if (test.getResult() == FAIL) {
                    testCase.setFailure(new Failure()
                        .setMessage("Failed")
                        .setType("")
                        .setContents(join(test.getResultOutput(), "\n"))
                    );
                }

                if (test.getResult() == SKIP) {
                    testCase.setSkipped(new Skipped()
                        .setMessage(join(test.getResultOutput(), "\n"))
                    );
                }
                ts.addCase(testCase);
            }
            result.addSuite(ts);
        }
        return result;
    }

    protected int countFailures(@Nonnull Iterable<Test> tests) {
        int result = 0;
        for (final Test test : tests) {
            if (test.getResult() == FAIL) {
                result++;
            }
        }
        return result;
    }

    @Nonnull
    protected String formatTime(double time) {
        return Double.toString(time);
    }


}
