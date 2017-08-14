package org.lastmilehealth.collect.android.archive;

import org.lastmilehealth.collect.android.manager.EventHandlerImpl;

/**
 * Created by Anton Donchev on 22.06.2017.
 */

public class ArchiveManagerImpl extends EventHandlerImpl implements ArchiveManager {
    private ArchiveCopyTask copyTask = null;
    private ArchiveLoaderTask loaderTask = null;
    private ArchiveOutdatedCheckTask outdatesTask = null;
    private int status = Event.INITIALIZED;
    private int archivesCount = 0;

    @Override
    public boolean isLoaded() {
        return status >= Event.LOADED_ARCHIVES_COUNT;
    }

    @Override
    public void load() {
        if (loaderTask == null && !isLoaded()) {
            loaderTask = new ArchiveLoaderTask();
            loaderTask.start();
        }
    }

    @Override
    public void onEvent(int event) {
        switch (event) {
            case Event.LOADED_ARCHIVES_COUNT:
                if (loaderTask != null) {
                    status = Event.LOADED_ARCHIVES_COUNT;
                    archivesCount = loaderTask.getCount();
                    loaderTask = null;
                } else {
                    status = Event.INITIALIZED;
                }
                break;

            case Event.COPIED_INSTANCES_TO_ARCHIVES:
                copyTask = null;
                break;
        }
        super.onEvent(event);
    }

    @Override
    public void checkForOutdatedArchives() {
        if (outdatesTask == null) {
            outdatesTask = new ArchiveOutdatedCheckTask();
            outdatesTask.start();
        }
    }

    @Override
    public int getArchivesCount() {
        return archivesCount;
    }

    @Override
    public void invalidate() {
        archivesCount = 0;
        status = Event.INITIALIZED;
    }

    @Override
    public void copyInstancesToArchives() {
        copyTask = new ArchiveCopyTask();
        copyTask.run();
    }
}
