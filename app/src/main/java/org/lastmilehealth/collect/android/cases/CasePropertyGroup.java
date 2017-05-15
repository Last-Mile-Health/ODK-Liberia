package org.lastmilehealth.collect.android.cases;

import org.javarosa.core.model.FormDef;

/**
 * Created by Anton Donchev on 05.05.2017.
 */

public interface CasePropertyGroup extends CaseProperty {

    FormDef getForm();
}
