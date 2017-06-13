package org.lastmilehealth.collect.android.cases;

import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.Collection;

/**
 * Created by Anton Donchev on 05.05.2017.
 */

public interface CaseElement {
    View generateView(ViewGroup parent,
                      Collection<InstanceElement> secondaryForms);

    void dispose();

}