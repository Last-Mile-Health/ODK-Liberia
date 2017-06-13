package org.lastmilehealth.collect.android.cases;

/**
 * Created by Anton Donchev on 11.05.2017.
 */

public interface LoadingTask {

    void start();

    boolean isSuccessful();

    Exception getError();

    void cancel();
}
