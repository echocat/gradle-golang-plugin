package org.echocat.gradle.plugins.golang.testing.report;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.echocat.gradle.plugins.golang.testing.report.Result.FAIL;

public class Test extends TestContainer.Support<Test> {

    private String _name;
    private double _time;
    private Result _result;
    private List<String> _resultOutput;
    @SuppressWarnings("StringBufferField")
    private StringBuilder _systemOut;
    @SuppressWarnings("StringBufferField")
    private StringBuilder _systemErr;

    public String getName() {
        return _name;
    }

    public Test setName(String name) {
        _name = name;
        return this;
    }

    public double getTime() {
        return _time;
    }

    public Test setTime(double time) {
        _time = time;
        return this;
    }

    public Result getResult() {
        return _result;
    }

    public Test setResult(Result result) {
        _result = result;
        return this;
    }

    public List<String> getResultOutput() {
        return _resultOutput;
    }

    public Test setResultOutput(List<String> resultOutput) {
        _resultOutput = resultOutput;
        return this;
    }

    public Test addResultOutput(String... outputs) {
        if (_resultOutput == null) {
            _resultOutput = new ArrayList<>();
        }
        if (outputs != null) {
            _resultOutput.addAll(asList(outputs));
        }
        return this;
    }

    @Override
    public int getNumberOfFailures() {
        return super.getNumberOfFailures() + (getResult() == FAIL ? 1 : 0);
    }

    @Override
    public int getNumberOfTestCases() {
        return super.getNumberOfTestCases() + 1;
    }

    public String getSystemOut() {
        final StringBuilder builder = _systemOut;
        return builder != null ? builder.toString() : null;
    }

    public Test setSystemOut(String systemOut) {
        _systemOut = systemOut != null ? new StringBuilder(systemOut) : null;
        return this;
    }

    public String getSystemErr() {
        final StringBuilder builder = _systemErr;
        return builder != null ? builder.toString() : null;
    }

    public Test setSystemErr(String systemErr) {
        _systemErr = systemErr != null ? new StringBuilder(systemErr) : null;
        return this;
    }

    public Test addSystemOut(String what) {
        if (what != null) {
            if (_systemOut == null) {
                _systemOut = new StringBuilder();
            }
            _systemOut.append(what);
        }
        return this;
    }

    public Test addSystemErr(String what) {
        if (what != null) {
            if (_systemErr == null) {
                _systemErr = new StringBuilder();
            }
            _systemErr.append(what);
        }
        return this;
    }

}
