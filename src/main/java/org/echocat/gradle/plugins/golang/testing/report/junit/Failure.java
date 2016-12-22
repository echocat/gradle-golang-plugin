package org.echocat.gradle.plugins.golang.testing.report.junit;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "failure", propOrder = {"message"})
public class Failure {

    private String _message;
    private String _type;
    private String _contents;

    @XmlAttribute(name = "message")
    public String getMessage() {
        return _message;
    }

    public Failure setMessage(String message) {
        _message = message;
        return this;
    }

    @XmlAttribute(name = "type")
    public String getType() {
        return _type;
    }

    public Failure setType(String type) {
        _type = type;
        return this;
    }

    @XmlValue
    public String getContents() {
        return _contents;
    }

    public Failure setContents(String contents) {
        _contents = contents;
        return this;
    }

}
