package org.echocat.gradle.plugins.golang.testing.reports;

import org.echocat.gradle.plugins.golang.testing.report.GolangTestOutputBasedReportObserver;
import org.echocat.gradle.plugins.golang.testing.report.Report;
import org.echocat.gradle.plugins.golang.testing.report.ReportObserver;
import org.echocat.gradle.plugins.golang.testing.report.ReportTransformer;
import org.echocat.gradle.plugins.golang.testing.report.junit.TestSuites;
import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.lang3.StringUtils.split;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ReportsRoundtripTest {

    private static final String[] TESTS = {
        "01-pass",
        "02-fail",
        "03-skip",
        "04-go_1_4",
        "06-mixed",
        "07-compiled_test",
        "08-parallel",
        "09-coverage",
        "10-multipkg-coverage",
        "11-go_1_5",
        "12-go_1_7",
    };

    @Parameters(name = "{0}")
    public static Collection<Object[]> getTests() {
        final Collection<Object[]> result = new ArrayList<>();
        for (final String test : TESTS) {
            result.add(new Object[]{test});
        }
        return result;
    }

    @Nonnull
    private final String _test;

    public ReportsRoundtripTest(@Nonnull String test) {
        _test = test;
    }

    @Test
    public void test() throws Exception {
        test(_test);
    }

    protected void test(@Nonnull String test) throws Exception {
        final ReportTransformer transformer = new ReportTransformer();
        try (final InputStream is = inputInputStreamOf(test)) {
            try (final ReportObserver observer = new GolangTestOutputBasedReportObserver("package/name", Charset.forName("UTF-8"))) {
                copy(is, observer.out());
                observer.close();
                final Report report = observer.getReport();
                assertThat(report, notNullValue());
                final TestSuites testSuites = transformer.transformToJunit(report);
                assertThat(testSuites, notNullValue());
                try (final StringWriter writer = new StringWriter()) {
                    testSuites.marshall(writer);
                    final String expectedJunitReportContent = resultContentOf(test);
                    assertThat(writer.toString(), is(expectedJunitReportContent));
                }
            }
        }
    }

    @Nonnull
    protected InputStream inputInputStreamOf(@Nonnull String test) throws IOException {
        return inputStreamOf(test + ".txt");
    }

    @Nonnull
    protected String resultContentOf(@Nonnull String test) throws IOException {
        try (final Reader reader = resultReaderOf(test)) {
            return IOUtils.toString(reader);
        }
    }

    @Nonnull
    protected Reader resultReaderOf(@Nonnull String test) throws IOException {
        final String[] parts = split(test, "-", 2);
        assertThat(parts.length, is(2));
        return readerOf(parts[0] + "-report.xml");
    }

    @Nonnull
    protected Reader readerOf(@Nonnull String filename) throws IOException {
        final InputStream is = inputStreamOf(filename);
        return new InputStreamReader(is, "UTF-8");
    }

    @Nonnull
    protected InputStream inputStreamOf(@Nonnull String filename) {
        final InputStream is = getClass().getResourceAsStream(filename);
        assertThat(is, notNullValue());
        return is;
    }

}
