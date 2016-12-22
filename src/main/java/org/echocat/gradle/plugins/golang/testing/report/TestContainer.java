package org.echocat.gradle.plugins.golang.testing.report;

import javax.annotation.Nonnegative;
import java.util.*;

import static java.util.Arrays.asList;
import static org.echocat.gradle.plugins.golang.testing.report.Result.FAIL;

public interface TestContainer extends Iterable<Test> {

    public List<Test> getTests();

    public TestContainer setTests(List<Test> tests);

    public TestContainer addTest(Test... tests);

    public TestContainer addTest(Collection<Test> tests);

    @Nonnegative
    public int getNumberOfFailures();

    @Nonnegative
    public int getNumberOfTestCases();

    public abstract static class Support<T extends Support<T>> implements TestContainer {

        private List<Test> _tests;

        @Override
        @Nonnegative
        public int getNumberOfFailures() {
            int result = 0;
            for (final Test test : this) {
                if (test.getResult() == FAIL) {
                    result++;
                }
            }
            return result;
        }

        @Override
        @Nonnegative
        public int getNumberOfTestCases() {
            final List<Test> tests = _tests;
            return tests != null ? tests.size() : 0;
        }

        @Override
        public Iterator<Test> iterator() {
            final List<Test> tests = _tests;
            return tests != null ? tests.iterator() : Collections.<Test>emptyIterator();
        }

        @Override
        public List<Test> getTests() {
            return _tests;
        }

        @Override
        public T setTests(List<Test> tests) {
            _tests = tests;
            return thisObject();
        }

        @Override
        public T addTest(Test... tests) {
            if (tests != null) {
                addTest(asList(tests));
            }
            return thisObject();
        }

        @Override
        public T addTest(Collection<Test> tests) {
            if (_tests == null) {
                _tests = new ArrayList<>();
            }
            if (tests != null) {
                _tests.addAll(tests);
            }
            return thisObject();
        }

        protected T thisObject() {
            //noinspection unchecked
            return (T) this;
        }

    }

}
