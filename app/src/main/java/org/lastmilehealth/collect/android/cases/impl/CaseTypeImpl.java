package org.lastmilehealth.collect.android.cases.impl;

import org.javarosa.core.model.FormDef;
import org.lastmilehealth.collect.android.cases.CaseCollection;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.manager.EventHandlerImpl;

import java.util.Collection;
import java.util.UUID;

/**
 * Represents a case type as structured in cases.xml.
 * <p>
 * Created by Anton Donchev on 09.05.2017.
 */

public class CaseTypeImpl extends EventHandlerImpl implements CaseType {
    private final UUID caseTypeId = UUID.randomUUID();
    private String displayName;
    private final CaseCollection cases = new CaseCollectionImpl();
    private int state;
    private String primaryFormVariable;
    private String primaryFormName;
    private Collection<String> secondaryFormNames;
    private Collection<FormDef> secondaryForms;
    private FormDef primaryForm;
    private int casesState = Event.CASES_NOT_LOADED;

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
    public void loadFormInstances() {

    }

    @Override
    public void createCaseInstances() {

    }

    @Override
    public boolean isCaseListLoaded() {
        return casesState >= Event.CASES_LIST_LOADED;
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

    public void setSecondaryFormNames(Collection<String> secondaryFormNames) {
        this.secondaryFormNames = secondaryFormNames;
    }

    @Override
    public void onEvent(int event) {
        switch (event) {
            case Event.CASES_LIST_LOADED:
                casesState = Event.CASES_LIST_LOADED;
                break;

            case Event.CASES_LIST_FAILED:
                casesState = Event.CASES_NOT_LOADED;
                cases.clear();
                break;

            case Event.CASES_LIST_LOADING:
                casesState = Event.CASES_LIST_LOADING;
                break;
        }
        super.onEvent(event);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setPrimaryFormVariable(String primaryFormVariable) {
        this.primaryFormVariable = primaryFormVariable;
    }

    public Collection<FormDef> getSecondaryForms() {
        return secondaryForms;
    }

    public void setSecondaryForms(Collection<FormDef> secondaryForms) {
        this.secondaryForms = secondaryForms;
    }

    public FormDef getPrimaryForm() {
        return primaryForm;
    }

    public void setPrimaryForm(FormDef primaryForm) {
        this.primaryForm = primaryForm;
    }
}
