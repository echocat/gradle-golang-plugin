package org.echocat.gradle.plugins.golang.testing.report.junit;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static javax.xml.bind.JAXBContext.newInstance;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

@XmlRootElement(name = "testsuites")
@XmlType(name = "testsuites", propOrder = "suites")
public class TestSuites {

    private static final JAXBContext JAXB_CONTEXT = createJaxbContext();

    private List<TestSuite> _suites;

    @XmlElement(name = "testsuite")
    public List<TestSuite> getSuites() {
        return _suites;
    }

    public TestSuites setSuites(List<TestSuite> suites) {
        _suites = suites;
        return this;
    }

    public TestSuites addSuite(TestSuite... values) {
        if (_suites == null) {
            _suites = new ArrayList<>();
        }
        if (values != null) {
            _suites.addAll(asList(values));
        }
        return this;
    }

    public void marshall(@Nonnull Writer to) throws IOException {
        marshall(this, to);
    }

    public static void marshall(@Nonnull TestSuites what, @Nonnull Writer to) throws IOException {
        try {
            marshallerFor(what).marshal(what, to);
        } catch (final JAXBException e) {
            throw new IOException("Could not marshall " + what + " to " + to + ".", e);
        }
    }

    @Nonnull
    private static javax.xml.bind.Marshaller marshallerFor(@Nonnull TestSuites element) {
        final javax.xml.bind.Marshaller marshaller;
        try {
            marshaller = JAXB_CONTEXT.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.setEventHandler(new ValidationEventHandler() {
                @Override
                public boolean handleEvent(ValidationEvent event) {
                    return true;
                }
            });
        } catch (final Exception e) {
            throw new RuntimeException("Could not create marshaller to marshall " + element + ".", e);
        }
        return marshaller;
    }


    @Nonnull
    private static JAXBContext createJaxbContext() {
        try {
            return newInstance(
                Failure.class,
                Property.class,
                Skipped.class,
                TestCase.class,
                TestSuite.class,
                TestSuites.class
            );
        } catch (final Exception e) {
            throw new RuntimeException("Could not create jaxb context.", e);
        }
    }

}
