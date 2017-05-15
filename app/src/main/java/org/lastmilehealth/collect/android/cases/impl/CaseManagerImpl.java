package org.lastmilehealth.collect.android.cases.impl;

import android.content.Context;
import android.text.TextUtils;

import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.cases.CaseCollection;
import org.lastmilehealth.collect.android.cases.CaseManager;
import org.lastmilehealth.collect.android.cases.CaseManagerLoadingTask;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.manager.Manager;

import java.util.List;

/**
 * Simple implementation of case manager.
 * <p>
 * Created by Anton Donchev on 09.05.2017.
 */

public class CaseManagerImpl extends Manager implements CaseManager {
    private int state = State.INITIALIZED;
    private List<CaseType> cases;
    private CaseManagerLoadingTask task;

    @Override
    public boolean isLoaded() {
        return state == State.LOADED;
    }

    @Override
    public List<CaseType> getCaseTypes() {
        return cases;
    }

    @Override
    public CaseType findCaseTypeById(String caseTypeId) {
        // It doesn't sound like there should be a lot of case types and this is called rarely
        // so there doesn't seem to be need for this to be cached in a map.
        for (CaseType caseType : cases) {
            if (TextUtils.equals(caseTypeId, caseType.getId())) {
                return caseType;
            }
        }
        return null;
    }

    @Override
    public CaseCollection getCasesOfType(String caseTypeId) {
        CaseType caseType = findCaseTypeById(caseTypeId);
        if (caseType != null) {
            return caseType.getCases();
        }
        return null;
    }

    @Override
    public void loadCaseDetails(Case instance) {

    }

    @Override
    public void loadManager() {
        loadCaseTypes();
    }

    @Override
    public void onEvent(int event) {
        switch (event) {
            case State.LOADED:
                if ((task != null) && (task.isSuccessful())) {
                    cases = task.getLoadedCases();
                    state = State.LOADED;
                }
                break;

            case State.FAILED:
                state = State.INITIALIZED;
                break;

            case State.LOADING:
                state = State.LOADING;
                break;
        }
        super.onEvent(event);
    }

    @Override
    public void loadCaseType(CaseType caseType,
                             Context context) {
        if (caseType == null) {
            throw new NullPointerException("Case Type is null");
        }
        if (!caseType.isCaseListLoaded()) {
            DefaultCaseListLoadingTask task = new DefaultCaseListLoadingTask(context, caseType);
            task.start();
        }


    }


    private synchronized void loadCaseTypes() {
        if (state < State.LOADING) {
            task = new DefaultCaseManagerLoadingTask();
            task.start();
        } else {
            onEvent(state);
        }
    }

}
