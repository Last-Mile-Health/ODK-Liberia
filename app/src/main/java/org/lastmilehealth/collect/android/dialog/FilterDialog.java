package org.lastmilehealth.collect.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.filter.Filter;
import org.lastmilehealth.collect.android.filter.FilterTransformer;
import org.lastmilehealth.collect.android.filter.impl.FilterMonth;
import org.lastmilehealth.collect.android.filter.impl.FilterPeriod;
import org.lastmilehealth.collect.android.filter.impl.FilterTrimonth;
import org.lastmilehealth.collect.android.filter.impl.FilterWeek;
import org.lastmilehealth.collect.android.filter.impl.SortAlpha;
import org.lastmilehealth.collect.android.filter.impl.SortNewestFirst;
import org.lastmilehealth.collect.android.filter.impl.SortOldestFirst;

import java.util.Date;

/**
 * Created by Anton Donchev on 18.05.2017.
 */

public abstract class FilterDialog<TYPE> extends Dialog {
    private final FilterTransformer<TYPE, Date> objToDateTransformer;
    private final FilterTransformer<TYPE, String> objToPrimaryVarTransformer;
    private final Filter<TYPE> currentFilter;
    private final Filter<TYPE> currentSort;
    private TextView sortAlpha, sortOldest, sortNewest, filterWeek, filterMonth, filterTrimonth, clearAll;
    private OnFilterClicked<TYPE> onFilterClickedListener = null;
    private int currentFilterId, currentSortId;
    private final View.OnClickListener onButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onFilterClickedListener == null) {
                dismiss();
                return;
            }

            int filterType = 0;
            Filter<TYPE> filter = null;
            if (v.getId() == currentSortId) {
                filterType = 2;
            } else if (v.getId() == currentFilterId) {
                filterType = 1;
            } else {

                switch (v.getId()) {
                    case R.id.filter_month:
                        filter = new FilterMonth<>(objToDateTransformer);
                        filterType = 1;
                        break;
                    case R.id.filter_week:
                        filter = new FilterWeek<>(objToDateTransformer);
                        filterType = 1;
                        break;
                    case R.id.filter_trimonth:
                        filter = new FilterTrimonth<>(objToDateTransformer);
                        filterType = 1;
                        break;
                    case R.id.sort_alpha:
                        filter = new SortAlpha<>(objToPrimaryVarTransformer);
                        filterType = 2;
                        break;
                    case R.id.sort_newest:
                        filter = new SortNewestFirst<>(objToDateTransformer);
                        filterType = 2;
                        break;
                    case R.id.sort_oldest:
                        filter = new SortOldestFirst<>(objToDateTransformer);
                        filterType = 2;
                        break;

                    case R.id.filter_clear:
                        filterType = -1;
                        break;
                }
            }
            if (filterType > 0 && (filter == null || filter.isEnabled())) {
                switch (filterType) {
                    case 1:
                        onFilterClickedListener.onFilterSelected(filter);
                        break;

                    case 2:
                        onFilterClickedListener.onSortMethodSelected(filter);

                }
                dismiss();
            } else if (filterType < 0) {
                onFilterClickedListener.clearAll();
                dismiss();
            }

        }
    };

    protected FilterDialog(@NonNull Context context,
                           FilterTransformer<TYPE, Date> objToDateTransformer,
                           FilterTransformer<TYPE, String> objToPrimaryVarTransformer,
                           Filter<TYPE> currentFilter,
                           Filter<TYPE> currentSort) {
        super(context);
        this.objToDateTransformer = objToDateTransformer;
        this.objToPrimaryVarTransformer = objToPrimaryVarTransformer;
        this.currentFilter = currentFilter;
        this.currentSort = currentSort;
        init();
    }

    public void setOnFilterClickedListener(OnFilterClicked<TYPE> onFilterClickedListener) {
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

        clearAll = (TextView) findViewById(R.id.filter_clear);

        setFiltersState();
    }

    private void setFiltersState() {
        sortAlpha.setOnClickListener(onButtonClicked);
        sortNewest.setOnClickListener(onButtonClicked);
        sortOldest.setOnClickListener(onButtonClicked);

        filterMonth.setOnClickListener(onButtonClicked);
        filterWeek.setOnClickListener(onButtonClicked);
        filterTrimonth.setOnClickListener(onButtonClicked);

        clearAll.setOnClickListener(onButtonClicked);

        int colorSelected = getContext().getResources().getColor(R.color.bg_filter_selected);
        int colorDisabled = getContext().getResources().getColor(R.color.text_filter_disabled);

        if (currentFilter != null) {
            if (currentFilter instanceof FilterMonth) {
                filterMonth.setBackgroundColor(colorSelected);
                currentFilterId = R.id.filter_month;
            } else if (currentFilter instanceof FilterWeek) {
                filterWeek.setBackgroundColor(colorSelected);
                currentFilterId = R.id.filter_week;
            } else if (currentFilter instanceof FilterTrimonth) {
                filterTrimonth.setBackgroundColor(colorSelected);
                currentFilterId = R.id.filter_trimonth;
            }
        }

        if (currentSort != null) {
            if (currentSort instanceof SortAlpha) {
                sortAlpha.setBackgroundColor(colorSelected);
                currentSortId = R.id.sort_alpha;
            } else if (currentSort instanceof SortNewestFirst) {
                sortNewest.setBackgroundColor(colorSelected);
                currentSortId = R.id.sort_newest;
            } else if (currentSort instanceof SortOldestFirst) {
                sortOldest.setBackgroundColor(colorSelected);
                currentSortId = R.id.sort_oldest;
            }
        }

        if (!FilterPeriod.isPeriodFilteringEnabled()) {
            filterMonth.setTextColor(colorDisabled);
            filterWeek.setTextColor(colorDisabled);
            filterTrimonth.setTextColor(colorDisabled);
            sortNewest.setTextColor(colorDisabled);
            sortOldest.setTextColor(colorDisabled);
        }

    }

    public interface OnFilterClicked<TYPE> {
        void onFilterSelected(Filter<TYPE> filter);

        void onSortMethodSelected(Filter<TYPE> sortMethod);

        void clearAll();

    }
}
