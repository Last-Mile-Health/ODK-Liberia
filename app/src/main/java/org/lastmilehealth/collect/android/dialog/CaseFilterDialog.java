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
import org.lastmilehealth.collect.android.filter.CaseFilter;
import org.lastmilehealth.collect.android.filter.impl.FilterMonth;
import org.lastmilehealth.collect.android.filter.impl.FilterTrimonth;
import org.lastmilehealth.collect.android.filter.impl.FilterWeek;
import org.lastmilehealth.collect.android.filter.impl.SortAlpha;
import org.lastmilehealth.collect.android.filter.impl.SortNewestFirst;
import org.lastmilehealth.collect.android.filter.impl.SortOldestFirst;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public class CaseFilterDialog extends Dialog {
    private TextView sortAlpha, sortOldest, sortNewest, filterWeek, filterMonth, filterTrimonth;
    private OnFilterClicked onFilterClickedListener = null;
    private final View.OnClickListener onButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onFilterClickedListener == null) {
                dismiss();
                return;
            }
            // TODO add some functionality if the filter is not enabled maybe show an error dialog.
            switch (v.getId()) {
                case R.id.filter_month:
                    if (FilterMonth.isEnabled()) {
                        onFilterClickedListener.onFilterSelected(new FilterMonth());
                        dismiss();
                    }
                    break;
                case R.id.filter_week:
                    if (FilterWeek.isEnabled()) {
                        onFilterClickedListener.onFilterSelected(new FilterWeek());
                        dismiss();
                    }
                    break;
                case R.id.filter_trimonth:
                    if (FilterTrimonth.isEnabled()) {
                        onFilterClickedListener.onFilterSelected(new FilterTrimonth());
                        dismiss();
                    }
                    break;
                case R.id.sort_alpha:
                    if (SortAlpha.isEnabled()) {
                        onFilterClickedListener.onSortMethodSelected(new SortAlpha());
                        dismiss();
                    }
                    break;
                case R.id.sort_newest:
                    if (SortNewestFirst.isEnabled()) {
                        onFilterClickedListener.onSortMethodSelected(new SortNewestFirst());
                        dismiss();
                    }
                    break;
                case R.id.sort_oldest:
                    if (SortOldestFirst.isEnabled()) {
                        onFilterClickedListener.onSortMethodSelected(new SortOldestFirst());
                        dismiss();
                    }
                    break;

            }

        }
    };

    public CaseFilterDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public CaseFilterDialog(@NonNull Context context,
                            @StyleRes int theme) {
        super(context, theme);
        init();
    }

    protected CaseFilterDialog(@NonNull Context context,
                               boolean cancelable,
                               @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    public void setOnFilterClickedListener(OnFilterClicked onFilterClickedListener) {
        this.onFilterClickedListener = onFilterClickedListener;
    }

    public void setShowSort(boolean isShowing) {
        sortAlpha.setVisibility(!isShowing ? View.GONE : View.VISIBLE);
        sortNewest.setVisibility(!isShowing ? View.GONE : View.VISIBLE);
        sortOldest.setVisibility(!isShowing ? View.GONE : View.VISIBLE);
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_filter);

        sortAlpha = (TextView) findViewById(R.id.sort_alpha);
        sortNewest = (TextView) findViewById(R.id.sort_newest);
        sortOldest = (TextView) findViewById(R.id.sort_oldest);

        filterMonth = (TextView) findViewById(R.id.filter_month);
        filterWeek = (TextView) findViewById(R.id.filter_week);
        filterTrimonth = (TextView) findViewById(R.id.filter_trimonth);

        setFiltersState();
    }

    private void setFiltersState() {
        sortAlpha.setOnClickListener(onButtonClicked);
        sortNewest.setOnClickListener(onButtonClicked);
        sortOldest.setOnClickListener(onButtonClicked);

        filterMonth.setOnClickListener(onButtonClicked);
        filterWeek.setOnClickListener(onButtonClicked);
        filterTrimonth.setOnClickListener(onButtonClicked);

        int colorEnabled = getContext().getResources().getColor(R.color.text_filter_enabled);
        int colorDisabled = getContext().getResources().getColor(R.color.text_filter_disabled);

        sortAlpha.setTextColor(SortAlpha.isEnabled() ? colorEnabled : colorDisabled);
        sortNewest.setTextColor(SortNewestFirst.isEnabled() ? colorEnabled : colorDisabled);
        sortOldest.setTextColor(SortOldestFirst.isEnabled() ? colorEnabled : colorDisabled);

        filterWeek.setTextColor(FilterWeek.isEnabled() ? colorEnabled : colorDisabled);
        filterMonth.setTextColor(FilterMonth.isEnabled() ? colorEnabled : colorDisabled);
        filterTrimonth.setTextColor(FilterTrimonth.isEnabled() ? colorEnabled : colorDisabled);
    }

    public interface OnFilterClicked {
        void onFilterSelected(CaseFilter filter);

        void onSortMethodSelected(CaseFilter sortMethod);

    }
}
