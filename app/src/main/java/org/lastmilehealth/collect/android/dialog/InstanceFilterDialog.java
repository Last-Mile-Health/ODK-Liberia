package org.lastmilehealth.collect.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.summary.InstanceFilter;
import org.lastmilehealth.collect.android.summary.MonthInstanceFilter;
import org.lastmilehealth.collect.android.summary.TriMonthInstanceFilter;
import org.lastmilehealth.collect.android.summary.WeekInstanceFilter;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class InstanceFilterDialog extends Dialog {
    private TextView filterWeek, filterMonth, filterTrimonth;
    private OnFilterClicked onFilterClickedListener = null;
    private final View.OnClickListener onButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onFilterClickedListener == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.filter_month:
                    if (MonthInstanceFilter.isEnabled()) {
                        onFilterClickedListener.onFilterSelected(new MonthInstanceFilter());
                        dismiss();
                    }
                    break;
                case R.id.filter_week:
                    if (WeekInstanceFilter.isEnabled()) {
                        onFilterClickedListener.onFilterSelected(new WeekInstanceFilter());
                        dismiss();
                    }
                    break;
                case R.id.filter_trimonth:
                    if (TriMonthInstanceFilter.isEnabled()) {
                        onFilterClickedListener.onFilterSelected(new TriMonthInstanceFilter());
                        dismiss();
                    }
                    break;

            }
            dismiss();
        }
    };

    public InstanceFilterDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public InstanceFilterDialog(@NonNull Context context,
                                @StyleRes int theme) {
        super(context, theme);
        init();
    }

    protected InstanceFilterDialog(@NonNull Context context,
                                   boolean cancelable,
                                   @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    public void setOnFilterClickedListener(OnFilterClicked onFilterClickedListener) {
        this.onFilterClickedListener = onFilterClickedListener;
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_filter);

        View sortAlpha = findViewById(R.id.sort_alpha);
        View sortNewest = findViewById(R.id.sort_newest);
        View sortOldest = findViewById(R.id.sort_oldest);

        filterMonth = (TextView) findViewById(R.id.filter_month);
        filterWeek = (TextView) findViewById(R.id.filter_week);
        filterTrimonth = (TextView) findViewById(R.id.filter_trimonth);

        sortAlpha.setVisibility(View.GONE);
        sortNewest.setVisibility(View.GONE);
        sortOldest.setVisibility(View.GONE);

        filterMonth.setOnClickListener(onButtonClicked);
        filterWeek.setOnClickListener(onButtonClicked);
        filterTrimonth.setOnClickListener(onButtonClicked);

        int colorEnabled = getContext().getResources().getColor(R.color.text_filter_enabled);
        int colorDisabled = getContext().getResources().getColor(R.color.text_filter_disabled);

        filterWeek.setTextColor(WeekInstanceFilter.isEnabled() ? colorEnabled : colorDisabled);
        filterMonth.setTextColor(MonthInstanceFilter.isEnabled() ? colorEnabled : colorDisabled);
        filterTrimonth.setTextColor(TriMonthInstanceFilter.isEnabled() ? colorEnabled : colorDisabled);

    }

    public interface OnFilterClicked {
        void onFilterSelected(InstanceFilter filter);
    }
}
