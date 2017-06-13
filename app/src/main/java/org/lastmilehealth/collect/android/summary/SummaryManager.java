package org.lastmilehealth.collect.android.summary;

import org.lastmilehealth.collect.android.manager.EventHandler;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.summary.calc.FunctionParser;

import java.util.Collection;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public interface SummaryManager extends EventHandler {

    boolean isLoaded();

    FunctionParser newFunctionParser();

    SummaryCollection getSummaries();

    Collection<InstanceElement> getInstances();

    boolean dispose();

    boolean load();

    interface Event {
        /**
         * The manager is created;
         */
        int INITIALIZED = 1000;

        /**
         * The manager has started loading.
         */
        int LOADING = 1100;

        /**
         * The manager has loaded successfully.
         */
        int LOADED = 1200;


        /**
         * The manager failed to load content.
         */
        int FAILED = -1000;
    }

}
