package org.lastmilehealth.collect.android.cases.impl;

import org.lastmilehealth.collect.android.cases.LoadingTask;

/**
 * Created by Anton Donchev on 11.05.2017.
 */

public abstract class BaseLoadingTask implements LoadingTask, Runnable {
    protected boolean success = false;
    protected Exception error;

    @Override
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

    @Override
    public Exception getError() {
        return error;
    }


}
