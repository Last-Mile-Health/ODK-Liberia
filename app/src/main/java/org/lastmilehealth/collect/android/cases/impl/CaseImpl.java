package org.lastmilehealth.collect.android.cases.impl;

import android.text.TextUtils;

import org.javarosa.core.model.FormDef;
import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.cases.CaseElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.List;

/**
 * This is a simple implementation of a case.
 * <p>
 * Created by Anton Donchev on 05.05.2017.
 */

public class CaseImpl implements Case {
    private String displayName;
    private FormDef primaryForm;
    private List<FormDef> secondaryForms;
    private String primaryVariable;
    private List<CaseElement> caseElements;
    private int status = Status.NOT_LOADED;
    private String primaryFormName;

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public FormDef getPrimaryForm() {
        return primaryForm;
    }

    public void setPrimaryForm(FormDef primaryForm) {
        this.primaryForm = primaryForm;
    }

    @Override
    public String getPrimaryVariableName() {
        return primaryVariable;
    }

    public void setPrimaryVariableName(String primaryVariable) {
        this.primaryVariable = primaryVariable;
    }

    @Override
    public String getPrimaryVariableValue() {
        if (primaryForm != null && !TextUtils.isEmpty(primaryVariable)) {
            return FormsUtils.getVariableValueAsString(primaryVariable, primaryForm);
        }
        return null;
    }

    @Override
    public List<FormDef> getSecondaryForms() {
        // Maybe return a copy to be safe???
        return secondaryForms;
    }

    public void setSecondaryForms(List<FormDef> secondaryForms) {
        this.secondaryForms = secondaryForms;
    }

    @Override
    public List<CaseElement> getCaseElements() {
        // Maybe return a copy to be safe???
        return caseElements;
    }

    public void setCaseElements(List<CaseElement> caseElements) {
        this.caseElements = caseElements;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void loadReferences() {

    }

    public void setPrimaryFormName(String primaryFormName) {
        this.primaryFormName = primaryFormName;
    }
}
