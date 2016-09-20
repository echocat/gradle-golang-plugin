package org.echocat.gradle.plugins.golang.tasks;

import groovy.lang.Closure;

import javax.annotation.Nullable;

public class GolangTask extends GolangTaskSupport {

    @Nullable
    private Closure<?> _action;

    @Override
    public void run() throws Exception {
        final Closure<?> action = _action;
        if (_action == null) {
            throw new IllegalArgumentException("There was neither an action property defined for task " + getName() + " nor the body of run() was overwritten.");
        }
        action.call();
    }

    @Nullable
    public Closure<?> getAction() {
        return _action;
    }

    public void setAction(@Nullable Closure<?> action) {
        _action = action;
    }

    public void action(@Nullable Closure<?> action) {
        setAction(action);
    }

}
