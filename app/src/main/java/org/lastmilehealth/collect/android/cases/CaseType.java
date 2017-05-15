package org.lastmilehealth.collect.android.cases;

import org.lastmilehealth.collect.android.manager.EventHandler;

import java.util.Collection;

/**
 * Represents a case type.
 * <p>
 * Created by Anton Donchev on 09.05.2017.
 */

public interface CaseType extends EventHandler {
    /**
     * Random generated id of each case type instance.
     */
    String getId();

    /**
     * The case type display name.
     */
    String getDisplayName();

    /**
     * All the cases that were found and loaded for this case type.
     */
    // Maybe this could be moved somewhere else??
    CaseCollection getCases();

    /**
     * Loads all primary form instances.
     */
    void loadFormInstances();

    /**
     * Creates cases from the loaded form instances.
     */
    void createCaseInstances();

    /**
     * Checks if all primary forms were loaded.
     */

    boolean isCaseListLoaded();
    /**
     * This is the name of the primary form for the cases.
     */
    String getPrimaryFormName();

    /**
     * This is the variable that is taken from the primary form and displayed on the cases list screen.
     */
    String getPrimaryVariable();

    /**
     * These are the names of secondary forms.
     */
    Collection<String> getSecondaryFormNames();

    interface Event {
        /**
         * The cases were not loaded and the loadings is not started.
         */
        int CASES_NOT_LOADED = 10;
        /**
         * In progress of loading cases list
         */
        int CASES_LIST_LOADING = 11;

        /**
         * Finished loading cases list successfully.
         */
        int CASES_LIST_LOADED = 12;

        /**
         * Failed to load cases list for some reason.
         */
        int CASES_LIST_FAILED = -1;
    }

}
