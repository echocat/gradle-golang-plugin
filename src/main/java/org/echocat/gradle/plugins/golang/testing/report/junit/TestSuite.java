package org.echocat.gradle.plugins.golang.testing.report.junit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@XmlType(name = "testsuite", propOrder = {"tests", "failures", "time", "name", "properties", "cases"})
public class TestSuite {

    private int _tests;
    private int _failures;
    private String _time;
    private String _name;
    private List<Property> _properties;
    private List<TestCase> _cases;

    @XmlAttribute(name = "tests")
    public int getTests() {
        return _tests;
    }

    public TestSuite setTests(int tests) {
        _tests = tests;
        return this;
    }

    @XmlAttribute(name = "failures")
    public int getFailures() {
        return _failures;
    }

    public TestSuite setFailures(int failures) {
        _failures = failures;
        return this;
    }

    @XmlAttribute(name = "time")
    public String getTime() {
        return _time;
    }

    public TestSuite setTime(String time) {
        _time = time;
        return this;
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return _name;
    }

    public TestSuite setName(String name) {
        _name = name;
        return this;
    }

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    public List<Property> getProperties() {
        return _properties;
    }

    public TestSuite setProperties(List<Property> properties) {
        _properties = properties;
        return this;
    }

    @XmlElement(name = "testcase")
    public List<TestCase> getCases() {
        return _cases;
    }

    public TestSuite setCases(List<TestCase> cases) {
        _cases = cases;
        return this;
    }

    public TestSuite addCase(TestCase... values) {
        if (_cases == null) {
            _cases = new ArrayList<>();
        }
        if (values != null) {
            _cases.addAll(asList(values));
        }
        return this;
    }

    public TestSuite addProperty(Property... values) {
        if (_properties == null) {
            _properties = new ArrayList<>();
        }
        if (values != null) {
            _properties.addAll(asList(values));
        }
        return this;
    }

    public TestSuite addProperty(@Nonnull String name, @Nullable String value) {
        return addProperty(new Property()
            .setName(name)
            .setValue(value)
        );
    }

}
