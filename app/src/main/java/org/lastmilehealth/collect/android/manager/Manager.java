package org.lastmilehealth.collect.android.manager;

import org.lastmilehealth.collect.android.cases.CaseManager;
import org.lastmilehealth.collect.android.cases.impl.CaseManagerImpl;
import org.lastmilehealth.collect.android.retention.RetentionTimeManager;
import org.lastmilehealth.collect.android.retention.impl.RetentionTimeManagerImpl;
import org.lastmilehealth.collect.android.summary.DefaultSummaryManager;
import org.lastmilehealth.collect.android.summary.SummaryManager;

/**
 * Class that contains references to various app managers.
 * <p>
 * Created by Anton Donchev on 09.05.2017.
 */

public class Manager extends EventHandlerImpl implements EventHandler {
    private static final CaseManager CASE_MANAGER = new CaseManagerImpl();
    private static final RetentionTimeManager RETENTION_MANAGER = new RetentionTimeManagerImpl();
    private static final SummaryManager SUMMARY_MANAGER = new DefaultSummaryManager();


    protected Manager() {
    }

    public static CaseManager getCaseManager() {
        return CASE_MANAGER;
    }

    public static RetentionTimeManager getRetentionManager() {
        return RETENTION_MANAGER;
    }

    public static SummaryManager getSummaryManager() {
        return SUMMARY_MANAGER;
    }

}
