package org.lastmilehealth.collect.android.cases;

import org.javarosa.core.model.FormDef;

import java.util.List;

/**
 * Created by Anton Donchev on 05.05.2017.
 */

public interface Case {
    String getDisplayName();

    FormDef getPrimaryForm();

    String getPrimaryVariableName();

    String getPrimaryVariableValue();

    List<FormDef> getSecondaryForms();

    /**
     * Returns the elements of this case.
     */
    List<CaseElement> getCaseElements();

    /**
     * Checks if the case is closed.
     */
    boolean isClosed();

    /**
     * Returns the status of the case.
     */
    int getStatus();

    /**
     * This method loads all reference instances connected to that case.
     */
    void loadReferences();

    interface Status {
        /**
         * Reserved in case of issues.
         */
        int UNKNOWN = -1;

        /**
         * The case is not loaded. This is the default status a case has when created.
         */
        int NOT_LOADED = 0;
        /**
         * The case has been parsed. It's name and structure is retrieved, but it's references and sub-properties are not loaded.
         */
        int INITIALIZED = 1;

        /**
         * Primary forms loaded.
         */
        int PRIMARY_LOADED = 2;

        /**
         * The case and it's references are parsed and it is fully loaded.
         */
        int FULLY_LOADED = 3;
    }

}
