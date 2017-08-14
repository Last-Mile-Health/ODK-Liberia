package org.lastmilehealth.collect.android.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.logic.FormController;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.provider.FormsProviderAPI;
import org.lastmilehealth.collect.android.provider.InstanceProviderAPI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Contains various forms helpers.
 * <p>
 * Created by Anton Donchev on 15.05.2017.
 */

public class FormsUtils {
    public static final String CASE_UUID = "data/Case_UUID";
    public static final String CASE_STATUS = "data/caseStatus";
    public static final String CASE_STATUS_CLOSED = "closed";
    public static final String ATTR_FORM_NAME = "name";
    //    public static final String CASE_FILTER_VARIABLE = "/data/autoDate";
    public static final String CASE_FILTER_VARIABLE = "/data/LMD-VAL-meta_dataEntry_endTime";
    public static final String[] DATE_FORMATS = {"yyyy-MM-dd'T'HH:mm:ss.S", "yyyy-MM-dd"};

    public static IAnswerData getVariableValue(String variableName,
                                               FormDef formDef) {
        TreeElement element = findChildForName(formDef.getInstance().getRoot(), variableName);
        if (element != null) {
            return element.getValue();
        }
        return null;
    }

    public static String getVariableValue(String variableName,
                                          InstanceElement root) {
        if (TextUtils.isEmpty(variableName)) {
            return null;
        }
        InstanceElement element = findChildForName(root, variableName);
        if (element != null) {
            return element.getValue();
        }
        return null;
    }

    public static TreeElement findChildForName(TreeElement element,
                                               String name) {
        String[] nameSplitReversed = name.split("[\\\\/]");

        return findChildForNameRecursive(element, nameSplitReversed, TextUtils.isEmpty(nameSplitReversed[0]) ? 1 : 0);
    }

    public static InstanceElement findChildForName(InstanceElement element,
                                                   String name) {
        String[] nameSplitReversed = name.split("[\\\\/]");

        return findChildForNameRecursive(element, nameSplitReversed, TextUtils.isEmpty(nameSplitReversed[0]) ? 1 : 0);
    }

    private static TreeElement findChildForNameRecursive(TreeElement element,
                                                         String[] names,
                                                         int index) {
        if (element.getName().equals(names[index])) {
            if (++index >= names.length) {
                return element;
            }
            TreeElement foundElement = findFirstChildForName(element, names[index]);
            if (foundElement != null) {
                return findChildForNameRecursive(foundElement, names, index);
            }
        }
        return null;
    }

    private static InstanceElement findChildForNameRecursive(InstanceElement element,
                                                             String[] names,
                                                             int index) {
        if (index < names.length && element.getName().equals(names[index])) {
            if (++index >= names.length) {
                return element;
            }
            InstanceElement foundElement = findFirstChildForName(element, names[index]);
            if (foundElement != null) {
                return findChildForNameRecursive(foundElement, names, index);
            }
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

    public static InstanceElement findFirstChildForName(InstanceElement element,
                                                        String name) {
        for (InstanceElement child : element.getChildren()) {
            if (child.getName().equals(name)) {
                return child;
            }
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

    public static Cursor getInstancesCursor(Context context,
                                            String formName) {
        String selection;
        String[] selectionArgs;
        if (TextUtils.isEmpty(formName)) {
            selection = InstanceProviderAPI.InstanceColumns.STATUS + " != ?";
            selectionArgs = new String[]{InstanceProviderAPI.STATUS_SUBMITTED};
        } else {
            selection = InstanceProviderAPI.InstanceColumns.STATUS + " != ? AND " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " = ?";
            selectionArgs = new String[]{InstanceProviderAPI.STATUS_SUBMITTED, formName};
        }
        return context.getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, selection, selectionArgs, null);
    }

    public static Cursor getInstanceCursor(Context context,
                                           Uri databaseUri,
                                           boolean isLocal) {
        String selection;
        String[] selectionArgs;
        if (!isLocal) {
            selection = InstanceProviderAPI.InstanceColumns.STATUS + " != ?";
            selectionArgs = new String[]{InstanceProviderAPI.STATUS_SUBMITTED};
        } else {
            selection = InstanceProviderAPI.InstanceColumns.STATUS + " != ? AND " + InstanceProviderAPI.InstanceColumns.APP_ID + " = ? AND " + InstanceProviderAPI.InstanceColumns.IS_TRANSFERRED + " = ?";
            selectionArgs = new String[]{InstanceProviderAPI.STATUS_SUBMITTED, Collect.getInstance().getAppId(), "0"};
        }
        return context.getContentResolver().query(databaseUri, null, selection, selectionArgs, null);
    }

    public static String getFormFilePath(Cursor instanceCursor,
                                         Context context) {
        // get the formId and version for this instance...
        String jrFormId = null;
        String jrVersion = null;
        {
            jrFormId = instanceCursor.getString(instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
            int idxJrVersion = instanceCursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION);

            jrVersion = instanceCursor.isNull(idxJrVersion) ? null : instanceCursor.getString(idxJrVersion);
        }

        String[] selectionArgs;
        String selection;

        if (jrVersion == null) {
            selectionArgs = new String[]{jrFormId};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND " + FormsProviderAPI.FormsColumns.JR_VERSION + " IS NULL";
        } else {
            selectionArgs = new String[]{jrFormId, jrVersion};
            selection = FormsProviderAPI.FormsColumns.JR_FORM_ID + "=? AND " + FormsProviderAPI.FormsColumns.JR_VERSION + "=?";
        }

        Cursor formCursor = null;
        String formPath = null;
        try {
            formCursor = context.getContentResolver()
                                .query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, selection, selectionArgs, null);
            if (formCursor.getCount() == 1) {
                formCursor.moveToFirst();
                formPath = formCursor.getString(formCursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH));
            } else {
                formPath = null;
            }
        } finally {
            if (formCursor != null) {
                formCursor.close();
            }
        }
        return formPath;
    }

    public static String getFormName(InstanceElement instance) {
        return instance.getAttributes().get(ATTR_FORM_NAME);
    }

    public static Date parseDateString(String str) {
        for (String dateFormat : DATE_FORMATS) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
                return formatter.parse(str);
            } catch (Exception e) {
                // Just continue to the next format.
            }
        }
        // If the date cannot be parsed by any formats than return null.
        return null;
    }

    public static void setCaseUuidToForm(String caseUuid,
                                         FormController formController) {
        TreeElement treeElement = findChildForName(formController.getFormDef().getInstance().getRoot(), CASE_UUID);
        treeElement.setAnswer(new StringData(caseUuid));
    }
}
