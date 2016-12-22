package org.echocat.gradle.plugins.golang.testing.report.junit;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "property", propOrder = {"name", "value"})
public class Property {

    private String _name;
    private String _value;

    @XmlAttribute(name = "name")
    public String getName() {
        return _name;
    }

    public Property setName(String name) {
        _name = name;
        return this;
    }

    @XmlAttribute(name = "value")
    public String getValue() {
        return _value;
    }

    public Property setValue(String value) {
        _value = value;
        return this;
    }
}
