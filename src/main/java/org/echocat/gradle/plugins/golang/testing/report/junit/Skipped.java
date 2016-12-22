package org.echocat.gradle.plugins.golang.testing.report.junit;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "skipped", propOrder = {"message"})
public class Skipped {

    private String _message;

    @XmlAttribute(name = "message")
    public String getMessage() {
        return _message;
    }

    public Skipped setMessage(String message) {
        _message = message;
        return this;
    }

}
