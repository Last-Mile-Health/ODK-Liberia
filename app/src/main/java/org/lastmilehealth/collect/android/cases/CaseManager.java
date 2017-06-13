package org.lastmilehealth.collect.android.cases;

import android.content.Context;

import org.lastmilehealth.collect.android.manager.EventHandler;

import java.util.List;

/**
 * Manages cases.
 * <p>
 * Created by Anton Donchev on 09.05.2017.
 */

public interface CaseManager extends EventHandler {

    /**
     * Checks if the manager has been loaded or not. This will return true only on successful parsing of cases.xml.
     *
     * @return true if loaded, false if not.
     */
    boolean isLoaded();

    /**
     * Returns the cases held by this manager. If the manager is not loaded this would be null.
     */
    List<? extends CaseType> getCaseTypes();

    /**
     * Returns case type that macthes the given id.
     */
    CaseType findCaseTypeById(String caseTypeId);


    /**
     * Returns the case collection for the case type with the given id.
     */
    CaseCollection getCasesOfType(String caseTypeId);


    /**
     * Executes loading of a case type details.
     * This loads secondary case type forms.
     */
    void loadCaseTypeDetails(CaseType caseType);

    /**
     * This loads the specific case elements.
     *
     * @param caseType
     * @param caseInstance
     */
    void loadCaseDetails(CaseType caseType,
                         Case caseInstance);

    /**
     * Starts loading the case manager.
     * This loads all the case types, but not the cases themselves.
     */
    void loadManager();

    /**
     * Executes loading the cases list for this case type.
     * This loads only the primary forms of the cases and not all the details.
     */
    void loadCaseType(CaseType caseType,
                      Context activity);

    /**
     * Resets the manager cache.
     */
    void reset();


    interface State {
        /**
         * The manager is initialized, but not loaded.
         * This is the default state.
         */
        int INITIALIZED = 0;

        /**
         * The manager is currently executing a loading task.
         */
        int LOADING = 1;

        /**
         * The manager is loaded.
         */
        int LOADED = 2;

        /**
         * The manager failed to load.
         */
        int FAILED = 3;

        /**
         * Fired when a case data has been altered.
         */
        int CASE_DATA_INVALIDATED = 1001;
    }
}
