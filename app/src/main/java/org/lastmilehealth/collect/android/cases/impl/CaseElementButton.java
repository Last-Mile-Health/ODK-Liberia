package org.lastmilehealth.collect.android.cases.impl;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.activities.FormEntryActivity;
import org.lastmilehealth.collect.android.cases.CaseElement;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.provider.FormsProviderAPI;
import org.lastmilehealth.collect.android.utilities.FormsUtils;

import java.util.Collection;

/**
 * Created by eXirrah on 22-May-17.
 */

public class CaseElementButton extends BasicCaseElement implements CaseElement {
    private Uri formUri;

    @Override
    public View generateView(ViewGroup context,
                             Collection<InstanceElement> secondaryForms) {
        String caseUuid = null;
        if (secondaryForms != null) {
            for (InstanceElement instance : secondaryForms) {
                caseUuid = FormsUtils.getVariableValue(FormsUtils.CASE_UUID, instance);
                if (!TextUtils.isEmpty(caseUuid)) {
                    break;
                }
            }
        }
        findRequiredFormUri(context.getContext());
        if (formUri == null || TextUtils.isEmpty(caseUuid)) {
            return null;
        }
        View view = inflate(context, R.layout.case_element_button);
        Button button = (Button) view.findViewById(R.id.button);
        button.setText(generateText(null));
        button.setOnClickListener(new OnButtonClickListener(caseUuid, formUri));
        return button;
    }

    private void findRequiredFormUri(Context context) {
        if (formUri == null) {
            Cursor cursor = null;
            try {
//                String selection = FormsProviderAPI.FormsColumns.FORM_NAME + " = ?";
//                String[] selectionArgs = new String[]{formName.toUpperCase()};
//                String sortOrder = FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
                cursor = context.getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null, null);


                if ((cursor != null) && (cursor.moveToFirst())) {
                    int formNameIndex = cursor.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_NAME);
                    do {
                        String formName = cursor.getString(formNameIndex);
                        if (this.formName.equalsIgnoreCase(formName)) {
                            int idColumnIndex = cursor.getColumnIndexOrThrow("_id");
                            long formId = cursor.getLong(idColumnIndex);
                            formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, formId);
                            return;
                        }
                    } while (cursor.moveToNext());


                }

            } catch (Exception e) {
                formUri = null;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private static class OnButtonClickListener implements View.OnClickListener {

        private final String caseUuid;
        private final Uri formUri;

        public OnButtonClickListener(String caseUuid,
                                     Uri formUri) {
            this.caseUuid = caseUuid;
            this.formUri = formUri;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), FormEntryActivity.class);
            intent.setData(formUri);
            intent.putExtra(FormEntryActivity.KEY_CASE_UUID, caseUuid);
            view.getContext().startActivity(intent);
        }
    }
}
