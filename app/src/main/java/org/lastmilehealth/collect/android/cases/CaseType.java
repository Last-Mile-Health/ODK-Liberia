package org.lastmilehealth.collect.android.cases;

import org.lastmilehealth.collect.android.manager.EventHandler;
import org.lastmilehealth.collect.android.parser.InstanceElement;

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
     * Checks if all primary forms were loaded.
     */

    boolean isCaseListLoaded();

    /**
     * Case details are loaded.
     */
    boolean isCaseDetailsLoaded();

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

    SecondaryFormsMap getSecondaryForms();

    CaseElement getCaseElement();

    Case findCaseByUUID(String uuid);

    void setCaseElement(CaseElement caseElement);

    void reset();

    void resetInstances();

    Collection<InstanceElement> getCaseSecondaryForms(Case caseInstance);

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
         * Started loading case details.
         */
        int CASE_DETAILS_LOADING = 13;

        /**
         * Case type details loaded.
         */
        int CASE_DETAILS_LOADED = 14;

        /**
         * Failed to load cases list for some reason.
         */
        int CASE_LIST_FAILED = -1;

        /**
         * Failed to load case details.
         */
        int CASE_DETAILS_FAILED = -2;
    }

}
