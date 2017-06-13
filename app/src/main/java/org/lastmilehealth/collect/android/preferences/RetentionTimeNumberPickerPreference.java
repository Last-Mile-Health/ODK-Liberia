package org.lastmilehealth.collect.android.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.retention.RetentionTimeManager;

/**
 * Preference dialog with a number picker.
 * <p>
 * Created by Anton Donchev on 15.05.2017.
 */

public class RetentionTimeNumberPickerPreference extends DialogPreference {
    private NumberPicker numberPicker;
    private int MAX_VALUE, MIN_VALUE, DEFAULT_VALUE;

    public RetentionTimeNumberPickerPreference(Context context,
                                               AttributeSet attrs,
                                               int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RetentionTimeNumberPickerPreference(Context context,
                                               AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        if (view.getId() == R.id.number_picker_layout) {

            numberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
            numberPicker.setMinValue(MIN_VALUE);
            numberPicker.setMaxValue(MAX_VALUE);
            int value = getPersistedInt(DEFAULT_VALUE);
            numberPicker.setValue(value);
        }
    }

    @Override
    public void onClick(DialogInterface dialog,
                        int which) {
        super.onClick(dialog, which);
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                int oldValue = getPersistedInt(DEFAULT_VALUE);
                int value = numberPicker.getValue();
                persistInt(value);
                dialog.dismiss();
                if (oldValue != value) {
                    Manager.getRetentionManager().onEvent(RetentionTimeManager.Event.EXPIRATION_TIME_CHANGED);
                }
                break;
        }
    }

    private void init() {
        MIN_VALUE = getContext().getResources().getInteger(R.integer.retention_time_min_expiration);
        MAX_VALUE = getContext().getResources().getInteger(R.integer.retention_time_max_expiration);
        DEFAULT_VALUE = getContext().getResources().getInteger(R.integer.retention_time_default_expiration);
        setDialogLayoutResource(R.layout.dialog_preference_number_picker);
    }
}
