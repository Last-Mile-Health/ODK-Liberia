package org.lastmilehealth.collect.android.archive;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.manager.Manager;

/**
 * Created by Anton Donchev on 22.06.2017.
 */

public abstract class ArchiveTask implements Runnable {
    public void start() {
        new Thread(this).start();
    }

    protected void callEvent(final int event) {
        Collect.MAIN_THREAD_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                Manager.getArchiveManager().onEvent(event);
            }
        });
    }
}
