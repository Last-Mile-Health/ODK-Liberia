package org.lastmilehealth.collect.android.manager;

import org.lastmilehealth.collect.android.cases.CaseManager;
import org.lastmilehealth.collect.android.cases.impl.CaseManagerImpl;

/**
 * Class that contains references to various app managers.
 * <p>
 * Created by Anton Donchev on 09.05.2017.
 */

public class Manager extends EventHandlerImpl implements EventHandler {
    private static final CaseManager CASE_MANAGER = new CaseManagerImpl();


    protected Manager() {
    }

    public static CaseManager getCaseManager() {
        return CASE_MANAGER;
    }

}
