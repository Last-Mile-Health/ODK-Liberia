package org.lastmilehealth.collect.android.cases;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;
import java.util.Date;

/**
 * Created by Anton Donchev on 05.05.2017.
 */

public interface Case {
    String getCaseUUID();

    String getDisplayName();

    InstanceElement getPrimaryForm();

    String getPrimaryVariableName();

    String getPrimaryVariableValue();

    Date getLastModifiedDate();

    /**
     * Returns the elements of this case.
     */
    CaseElement getCaseElements();

    /**
     * Checks if the case is closed.
     */
    boolean isClosed();

    /**
     * Checks if the case details are loaded.
     */
    boolean isLoaded();

    /**
     * Returns the status of the case.
     */
    int getStatus();

    /**
     * This method loads all reference instances connected to that case.
     */
    void loadReferences();

    Collection<InstanceElement> getSecondaryForms();

    void dispose();

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
        int INITIALIZED = 100;

        /**
         * Primary forms loaded.
         */
        int PRIMARY_LOADED = 200;

        /**
         * The case and it's references are parsed and it is fully loaded.
         */
        int FULLY_LOADED = 9999;
    }

}
