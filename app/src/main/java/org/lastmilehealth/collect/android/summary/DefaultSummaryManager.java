package org.lastmilehealth.collect.android.summary;

import org.lastmilehealth.collect.android.cases.LoadingTask;
import org.lastmilehealth.collect.android.manager.EventHandlerImpl;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.summary.calc.DefaultFunctionFactory;
import org.lastmilehealth.collect.android.summary.calc.DefaultFunctionParser;
import org.lastmilehealth.collect.android.summary.calc.FunctionFactory;
import org.lastmilehealth.collect.android.summary.calc.FunctionParser;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public class DefaultSummaryManager extends EventHandlerImpl implements SummaryManager {
    private final SummaryCollection summaries = new BasicSummaryCollection();
    private final Collection<InstanceElement> instances = new ArrayList<>();
    private int status = Event.INITIALIZED;
    private FunctionFactory functionFactory = new DefaultFunctionFactory();
    private LoadingTask executingTask = null;

    @Override
    public boolean isLoaded() {
        return status >= Event.LOADED;
    }

    @Override
    public FunctionParser newFunctionParser() {
        return new DefaultFunctionParser(functionFactory);
    }

    @Override
    public SummaryCollection getSummaries() {
        return summaries;
    }

    @Override
    public Collection<InstanceElement> getInstances() {
        return instances;
    }

    @Override
    public boolean dispose() {
        status = Event.INITIALIZED;
        summaries.dispose();
        cancelExecutingTask();
        for (InstanceElement instance : instances) {
            instance.dispose();
        }
        instances.clear();
        return false;
    }

    @Override
    public boolean load() {
        if (executingTask == null && status < Event.LOADING) {
            executingTask = new DefaultSummaryLoadingTask();
            executingTask.start();
        }
        return false;
    }

    @Override
    public void onEvent(int event) {
        super.onEvent(event);
        switch (event) {
            case Event.FAILED:
                status = Event.INITIALIZED;
                cancelExecutingTask();
                break;

            case Event.LOADING:
                status = Event.LOADING;
                break;

            case Event.LOADED:
                status = Event.LOADED;
                cancelExecutingTask();
                break;
        }
    }

    private void cancelExecutingTask() {
        if (executingTask != null) {
            executingTask.cancel();
            executingTask = null;
        }
    }
}

