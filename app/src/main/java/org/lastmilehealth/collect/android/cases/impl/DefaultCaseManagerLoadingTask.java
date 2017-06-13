package org.lastmilehealth.collect.android.cases.impl;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.CaseManager;
import org.lastmilehealth.collect.android.cases.CaseManagerLoadingTask;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.parser.XmlCaseParser;

import java.util.List;

/**
 * This is the default task for loading cases in the app.
 * <p>
 * Created by Anton Donchev on 09.05.2017.
 */

public class DefaultCaseManagerLoadingTask extends BaseLoadingTask implements CaseManagerLoadingTask, Runnable {
    private List<CaseType> cases = null;

    @Override
    public List<CaseType> getLoadedCases() {
        return cases;
    }

    @Override
    public void run() {
        // This is executed on worker thread.
        parseCaseTypes();
    }

    private void parseCaseTypes() {
        int executionState;
        cases = null;
        error = null;
        sendEventToCaseManager(CaseManager.State.LOADING);
        try {
            XmlCaseParser parser = new XmlCaseParser();
            cases = parser.parse();
            if (canceled) {
                cases = null;
                executionState = CaseManager.State.FAILED;
            } else {
                success = true;
                executionState = CaseManager.State.LOADED;
            }
        } catch (Exception e) {
            error = e;
            executionState = CaseManager.State.FAILED;
        }

        sendEventToCaseManager(executionState);
    }

    /**
     * Calls the event on the main thread.
     */
    private void sendEventToCaseManager(final int event) {
        // The Handler should enqueue the messages on the main thread in the order they were passed
        // so there is no worry about LOADING message executing after the other.
        Collect.MAIN_THREAD_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                Manager.getCaseManager().onEvent(event);
            }
        });
    }
}
