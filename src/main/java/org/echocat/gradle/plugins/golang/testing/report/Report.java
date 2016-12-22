package org.echocat.gradle.plugins.golang.testing.report;

import javax.annotation.Nonnegative;
import java.util.*;

import static java.util.Arrays.asList;

public class Report implements Iterable<Package> {

    private List<Package> _packages;

    public List<Package> getPackages() {
        return _packages;
    }

    public Report setPackages(List<Package> packages) {
        _packages = packages;
        return this;
    }

    public Report addPackage(Package... packages) {
        if (packages != null) {
            addPackage(asList(packages));
        }
        return this;
    }

    public Report addPackage(Collection<Package> packages) {
        if (_packages == null) {
            _packages = new ArrayList<>();
        }
        if (packages != null) {
            _packages.addAll(packages);
        }
        return this;
    }

    public Report addReport(Report... reports) {
        if (_packages == null) {
            _packages = new ArrayList<>();
        }
        if (reports != null) {
            for (final Report report : reports) {
                addPackage(report.getPackages());
            }
        }
        return this;
    }

    @Override
    public Iterator<Package> iterator() {
        final List<Package> packages = _packages;
        return packages != null ? packages.iterator() : Collections.<Package>emptyIterator();
    }

    @Nonnegative
    public int getNumberOfFailures() {
        int result = 0;
        for (final Package aPackage : this) {
            result += aPackage.getNumberOfFailures();
        }
        return result;
    }

    @Nonnegative
    public int getNumberOfTests() {
        int result = 0;
        for (final Package aPackage : this) {
            result += aPackage.getNumberOfTestCases();
        }
        return result;
    }

}
