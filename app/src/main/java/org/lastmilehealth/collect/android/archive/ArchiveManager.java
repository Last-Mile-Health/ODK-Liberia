package org.lastmilehealth.collect.android.archive;

import org.lastmilehealth.collect.android.manager.EventHandler;

/**
 * Created by Anton Donchev on 22.06.2017.
 */

public interface ArchiveManager extends EventHandler {
    long ARCHIVES_LIFESPAN_MS = 7776000000L; // 90 days in millies

    boolean isLoaded();

    void load();

    void checkForOutdatedArchives();

    int getArchivesCount();

    void invalidate();

    void copyInstancesToArchives();


    interface Event {
        /**
         * The manager is initialized and ready. This is it's initial state.
         */
        int INITIALIZED = 500;

        /**
         * The number of archived instances is loaded.
         */
        int LOADED_ARCHIVES_COUNT = 2000;

        /**
         * Older than lifespan archives are deleted.
         */
        int DELETED_OLD_ARCHIVES = 3000;

        /**
         * Called when the instances are moved to archives.
         */
        int COPIED_INSTANCES_TO_ARCHIVES = 4000;
    }
}
