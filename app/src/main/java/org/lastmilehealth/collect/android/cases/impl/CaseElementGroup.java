package org.lastmilehealth.collect.android.cases.impl;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.cases.CaseElement;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Anton Donchev on 19.05.2017.
 */

public class CaseElementGroup implements CaseElement {
    protected final List<CaseElement> caseElements;
    protected CaseElementGroup parent;
    protected String groupForm;

    public CaseElementGroup(CaseElementGroup parent) {
        this.parent = parent;
        this.caseElements = new ArrayList<>();
    }

    @Override
    public View generateView(ViewGroup context,
                             Collection<InstanceElement> secondaryForms) {
        try {
            Collection<InstanceElement> groupInstances = sortOutGroupElements(secondaryForms);
            if (groupInstances.size() > 0) {
                LayoutInflater inflater = LayoutInflater.from(context.getContext());
                View view = inflater.inflate(R.layout.case_element_group, context, false);
                ViewGroup group = (ViewGroup) view.findViewById(R.id.group_container);
                for (InstanceElement secondaryFormSelected : groupInstances) {
                    Collection<InstanceElement> secondaryFormSelectedAsCollection = new ArrayList<>();
                    secondaryFormSelectedAsCollection.add(secondaryFormSelected);
                    for (CaseElement element : caseElements) {
                        View elementView = element.generateView(context, secondaryFormSelectedAsCollection);
                        if (elementView != null) {
                            group.addView(elementView);
                        }
                    }
                }
                return view;
            }
        } catch (Exception e) {
            // Just return null...
        }
        return null;
    }

    @Override
    public void dispose() {
        if (parent != null) {
            // This will try to dispose the parent
            // In order not to get into an infinite loop while disposing children the parent should be null.
            CaseElementGroup parentCopy = parent;
            parent = null;
            parentCopy.dispose();
        }
        if (caseElements.size() > 0) {
            // If there is parent this array should be empty when the child tries to dispose of itself so
            // that there are no infinite loops.
            List<CaseElement> caseElementsCopy = new ArrayList<>(caseElements);
            caseElements.clear();
            for (CaseElement element : caseElementsCopy) {
                element.dispose();
            }
            caseElementsCopy.clear();
        }
    }

    private Collection<InstanceElement> sortOutGroupElements(Collection<InstanceElement> secondaryForms) {
        Collection<InstanceElement> groupElements = new ArrayList<>();
        if (!TextUtils.isEmpty(groupForm)) {
            for (InstanceElement element : secondaryForms) {
                if (groupForm.equalsIgnoreCase(element.getAttributes().get(FormsUtils.ATTR_FORM_NAME))) {
                    groupElements.add(element);
                }
            }
        }
        return groupElements;
    }

    public void setCaseElementsGroupForm(String formName) {
        groupForm = formName;
    }

    public CaseElementGroup getParent() {
        return parent;
    }

    public List<CaseElement> getCaseElements() {
        return caseElements;
    }
}
