package org.lastmilehealth.collect.android.cases.impl;

import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.cases.CaseCollection;
import org.lastmilehealth.collect.android.cases.CaseElement;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.cases.SecondaryFormsMap;
import org.lastmilehealth.collect.android.manager.EventHandlerImpl;
import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;
import java.util.UUID;

import static org.lastmilehealth.collect.android.cases.CaseType.Event.CASE_DETAILS_LOADED;
import static org.lastmilehealth.collect.android.cases.CaseType.Event.CASE_DETAILS_LOADING;

/**
 * Represents a case type as structured in cases.xml.
 * <p>
 * Created by Anton Donchev on 09.05.2017.
 */

public class CaseTypeImpl extends EventHandlerImpl implements CaseType {
    private final UUID caseTypeId = UUID.randomUUID();
    private final CaseCollection cases = new CaseCollectionImpl();
    private String displayName;
    private String primaryFormVariable;
    private String primaryFormName;
    private Collection<String> secondaryFormNames;
    private int casesState = Event.CASES_NOT_LOADED;
    private CaseElement caseElement;
    private final SecondaryFormsMap secondaryForms = new DefaultSecondaryFormMaps();


    public CaseTypeImpl() {}


    /**
     * This is the case typee id. It is randomly generated when the case type is parsed. When parsed again it would have different id.
     */
    @Override
    public String getId() {
        return caseTypeId.toString();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public CaseCollection getCases() {
        return cases;
    }

    @Override
    public boolean isCaseListLoaded() {
        return casesState >= Event.CASES_LIST_LOADED;
    }

    @Override
    public boolean isCaseDetailsLoaded() {
        return false;
    }

    @Override
    public String getPrimaryFormName() {
        return primaryFormName;
    }

    public void setPrimaryFormName(String primaryFormName) {
        this.primaryFormName = primaryFormName;
    }

    @Override
    public String getPrimaryVariable() {
        return primaryFormVariable;
    }

    @Override
    public Collection<String> getSecondaryFormNames() {
        return secondaryFormNames;
    }

    @Override
    public SecondaryFormsMap getSecondaryForms() {
        return secondaryForms;
    }

    public void setSecondaryFormNames(Collection<String> secondaryFormNames) {
        this.secondaryFormNames = secondaryFormNames;
    }

    @Override
    public void reset() {
        casesState = Event.CASES_NOT_LOADED;
        for (Case caseInstance : cases.values()) {
            caseInstance.dispose();
        }
        cases.clear();
    }

    @Override
    public Collection<InstanceElement> getCaseSecondaryForms(Case caseInstance) {
        if (secondaryForms.size() > 0 && caseInstance != null) {
            return secondaryForms.get(caseInstance.getCaseUUID());
        }
        return null;
    }

    @Override
    public void onEvent(int event) {
        super.onEvent(event);
        switch (event) {
            case Event.CASES_LIST_LOADED:
                casesState = Event.CASES_LIST_LOADED;
                break;

            case Event.CASE_LIST_FAILED:
                casesState = Event.CASES_NOT_LOADED;
                cases.clear();
                break;

            case Event.CASES_LIST_LOADING:
                casesState = Event.CASES_LIST_LOADING;
                break;

            case Event.CASE_DETAILS_FAILED:
                casesState = Event.CASES_LIST_LOADED;
                break;

            case CASE_DETAILS_LOADING:
                casesState = CASE_DETAILS_LOADING;
                break;

            case CASE_DETAILS_LOADED:
                casesState = CASE_DETAILS_LOADED;
                break;
        }
    }

    @Override
    public CaseElement getCaseElement() {
        return caseElement;
    }

    @Override
    public void setCaseElement(CaseElement caseElement) {
        this.caseElement = caseElement;
    }

    public void setPrimaryFormVariable(String primaryFormVariable) {
        this.primaryFormVariable = primaryFormVariable;
    }

}
