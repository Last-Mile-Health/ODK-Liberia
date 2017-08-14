package org.lastmilehealth.collect.android.cases.impl;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.cases.CaseElement;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * This is a simple implementation of a case.
 * <p>
 * Created by Anton Donchev on 05.05.2017.
 */

public class CaseImpl implements Case {
    private String displayName;
    private InstanceElement primaryForm;
    private String primaryVariable;
    private CaseElement caseElements;
    private int status = Status.NOT_LOADED;
    private String uuid;
    private Date lastModifiedDate;
    private final Collection<InstanceElement> secondaryForms = new ArrayList<>();

    @Override
    public String getCaseUUID() {
        return uuid;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public InstanceElement getPrimaryForm() {
        return primaryForm;
    }

    public void setPrimaryForm(InstanceElement primaryForm) {
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
            return FormsUtils.getVariableValue(primaryVariable, primaryForm);
        }
        return null;
    }

    @Override
    public Date getLastModifiedDate() {
        if (lastModifiedDate == null && primaryForm != null) {
            String variableName = Manager.getRetentionManager().getVariableName();
            if (!TextUtils.isEmpty(variableName)) {
                String value = FormsUtils.getVariableValue(variableName, primaryForm);
                lastModifiedDate = FormsUtils.parseDateString(value);
            }
        }
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public CaseElement getCaseElements() {
        // Maybe return a copy to be safe???
        return caseElements;
    }

    public void setCaseElements(CaseElement caseElements) {
        this.caseElements = caseElements;
    }

    @Override
    public boolean isClosed() {
        if (primaryForm == null) {
            return true;
        } else if (isClosed(primaryForm)) {
            return true;
        } else if (isSecondaryFormsClosed()) {
            return true;
        }
        return false;
    }

    private boolean isClosed(InstanceElement element) {
        String caseStatus = FormsUtils.getVariableValue(FormsUtils.CASE_STATUS, element);
        return (FormsUtils.CASE_STATUS_CLOSED.equalsIgnoreCase(caseStatus));
    }

    private boolean isSecondaryFormsClosed() {
        for (InstanceElement form : secondaryForms) {
            if (isClosed(form)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<InstanceElement> getSecondaryForms() {
        return secondaryForms;
    }

    @Override
    public boolean isLoaded() {
        return status >= Status.FULLY_LOADED;
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

    @Override
    public void dispose() {
        if (primaryForm != null) {
            primaryForm.dispose();
        }
        if (caseElements != null) {
            caseElements.dispose();
        }
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
