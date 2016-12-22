package org.echocat.gradle.plugins.golang.testing.report;

public class Package extends TestContainer.Support<Package> {

    private String _name;
    private double _time;
    private String _coveragePct;

    public String getName() {
        return _name;
    }

    public Package setName(String name) {
        _name = name;
        return this;
    }

    public double getTime() {
        return _time;
    }

    public Package setTime(double time) {
        _time = time;
        return this;
    }

    public String getCoveragePct() {
        return _coveragePct;
    }

    public Package setCoveragePct(String coveragePct) {
        _coveragePct = coveragePct;
        return this;
    }

}
