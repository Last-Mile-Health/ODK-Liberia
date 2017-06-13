package org.lastmilehealth.collect.android.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.retention.RetentionTimeManager;

public class RetentionVariableDialogPreference extends DialogPreference implements OnClickListener {

    private EditText variableEditText;

    public RetentionVariableDialogPreference(Context context,
                                             AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_retention_variable);
    }

    @Override
    public void onBindDialogView(View view) {
        variableEditText = (EditText) view.findViewById(R.id.retention_variable_name);
        variableEditText.setText(getPersistedString(Manager.getRetentionManager().getDetaulfVariableName()));
        super.onBindDialogView(view);
    }

    @Override
    protected void onClick() {
        super.onClick();
        // this seems to work to pop the keyboard when the dialog appears
        // i hope this isn't a race condition
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onClick(DialogInterface dialog,
                        int which) {
        super.onClick(dialog, which);
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                String oldValue = getPersistedString("");
                String value = variableEditText.getText() == null ? "" : variableEditText.getText().toString();
                persistString(value);
                dialog.dismiss();
                if (!TextUtils.equals(oldValue, value)) {
                    Manager.getRetentionManager().onEvent(RetentionTimeManager.Event.VARIABLE_NAME_CHANGED);
                }
                break;
        }
    }
}
