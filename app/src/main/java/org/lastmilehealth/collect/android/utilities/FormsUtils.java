package org.lastmilehealth.collect.android.utilities;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;

import java.util.List;

/**
 * Contains various forms helpers.
 * <p>
 * Created by Anton Donchev on 15.05.2017.
 */

public class FormsUtils {
    public static IAnswerData getVariableValue(String variableName,
                                               FormDef formDef) {
        TreeElement element = findChildForName(formDef.getInstance().getRoot(), variableName);
        if (element != null) {
            return element.getValue();
        }
        return null;
    }

    public static TreeElement findChildForName(TreeElement element,
                                               String name) {
        String[] nameSplitReversed = name.split("[\\\\/]");

        return findChildForNameRecursive(element, nameSplitReversed, 0);
    }

    private static TreeElement findChildForNameRecursive(TreeElement element,
                                                         String[] names,
                                                         int index) {
        if (index >= names.length) {
            return element;
        }
        TreeElement foundElement = findFirstChildForName(element, names[index]);
        if (foundElement != null) {
            return findChildForNameRecursive(foundElement, names, ++index);
        }
        return null;
    }

    public static TreeElement findFirstChildForName(TreeElement element,
                                                    String name) {
        List<TreeElement> elements = element.getChildrenWithName(name);
        if (elements.size() > 0) {
            return elements.get(0);
        }
        return null;
    }

    public static <T> T getVariableValue(String variableName,
                                         FormDef form,
                                         Class<T> classOfT) {
        IAnswerData variableValue = getVariableValue(variableName, form);
        if (variableValue != null) {
            Object value = variableValue.getValue();
            if (classOfT.isInstance(value)) {
                return classOfT.cast(value);
            }
        }
        return null;
    }

    public static String getVariableValueAsString(String variableName,
                                                  FormDef form) {
        IAnswerData data = getVariableValue(variableName, form);
        if (data != null) {
            return data.getValue().toString();
        }
        return null;
    }
}
