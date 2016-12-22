package org.echocat.gradle.plugins.golang.testing.report.junit;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "testcase", propOrder = {"className", "name", "time", "skipped", "failure", "systemOut", "systemErr"})
public class TestCase {

    private String _className;
    private String _name;
    private String _time;
    private Skipped _skipped;
    private Failure _failure;
    private String _systemOut;
    private String _systemErr;

    @XmlAttribute(name = "classname")
    public String getClassName() {
        return _className;
    }

    public TestCase setClassName(String className) {
        _className = className;
        return this;
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return _name;
    }

    public TestCase setName(String name) {
        _name = name;
        return this;
    }

    @XmlAttribute(name = "time")
    public String getTime() {
        return _time;
    }

    public TestCase setTime(String time) {
        _time = time;
        return this;
    }

    @XmlElement(name = "skipped")
    public Skipped getSkipped() {
        return _skipped;
    }

    public TestCase setSkipped(Skipped skipped) {
        _skipped = skipped;
        return this;
    }

    @XmlElement(name = "failure")
    public Failure getFailure() {
        return _failure;
    }

    public TestCase setFailure(Failure failure) {
        _failure = failure;
        return this;
    }

    @XmlElement(name = "system-out")
    public String getSystemOut() {
        return _systemOut;
    }

    public TestCase setSystemOut(String systemOut) {
        _systemOut = systemOut;
        return this;
    }

    @XmlElement(name = "system-err")
    public String getSystemErr() {
        return _systemErr;
    }

    public TestCase setSystemErr(String systemErr) {
        _systemErr = systemErr;
        return this;
    }

}
