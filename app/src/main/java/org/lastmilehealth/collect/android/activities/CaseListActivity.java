package org.lastmilehealth.collect.android.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.adapters.CasesAdapter;
import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.cases.CaseCollection;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.dialog.CaseFilterDialog;
import org.lastmilehealth.collect.android.dialog.FilterDialog;
import org.lastmilehealth.collect.android.filter.Filter;
import org.lastmilehealth.collect.android.listeners.OnEventListener;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.utilities.CompatibilityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Donchev on 12.05.2017.
 */

public class CaseListActivity extends ListActivity {
    public static final String EXTRA_CASE_TYPE_ID = "CaseTypeId";
    public static final int MENU_FILTER_ID = 10001;
    private View progressBar, progressLayout, noItemsLabel;
    private TextView progressText;
    private CasesAdapter adapter;
    private CaseType caseType;
    private AlertDialog mAlertDialog;
    private Menu menu;
    private CaseFilterDialog dialog;
    private boolean isFilterSelected = false;
    private Filter<Case> filterSelected = null;
    private Filter<Case> sortSelected = null;
    private OnEventListener onCaseTypeEvent = new OnEventListener() {
        @Override
        public void onEvent(int eventId) {
            switch (eventId) {
                case CaseType.Event.CASES_NOT_LOADED:
                    break;

                case CaseType.Event.CASES_LIST_LOADING:
                    showProgress(R.string.case_types_loading);
                    break;

                case CaseType.Event.CASES_LIST_LOADED:
                case CaseType.Event.CASE_LIST_FAILED:
                    hideProgress();
                    adapter.update();
                    checkForDoubleCasesNames();
                    checkIfAdapterIsEmppy();
                    break;
            }
        }
    };
    private FilterDialog.OnFilterClicked<Case> onFilterClicked = new FilterDialog.OnFilterClicked<Case>() {
        @Override
        public void onFilterSelected(Filter<Case> filter) {
            adapter.applyFilter(filter);
            filterSelected = filter;
            isFilterSelected = filter != null;
            checkIfAdapterIsEmppy();
        }

        @Override
        public void onSortMethodSelected(Filter<Case> sortMethod) {
            adapter.applySortingMethod(sortMethod);
            sortSelected = sortMethod;
            checkIfAdapterIsEmppy();
        }

        @Override
        public void clearAll() {
            filterSelected = null;
            isFilterSelected = false;
            sortSelected = null;
            adapter.applyFilter(null);
            adapter.applySortingMethod(null);
            checkIfAdapterIsEmppy();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_types);

        progressLayout = findViewById(R.id.progress_layout);
        progressBar = findViewById(R.id.progress_bar);
        progressText = (TextView) findViewById(R.id.progress_text);
        noItemsLabel = findViewById(R.id.no_cases_label);


        String caseTypeId = getIntent().getStringExtra(EXTRA_CASE_TYPE_ID);
        caseType = Manager.getCaseManager().findCaseTypeById(caseTypeId);
        if (caseType == null) {
            finish();
            return;
        }

        caseType.registerEventListener(onCaseTypeEvent);
        Object lastState = getLastNonConfigurationInstance();
        if (lastState != null && lastState instanceof CasesAdapter) {
            adapter = (CasesAdapter) lastState;
        } else {
            adapter = new CasesAdapter(caseType);
        }

        isFilterSelected = adapter.hasFilter();

        setListAdapter(adapter);

        checkIfAdapterIsEmppy();

        setTitle(getString(R.string.app_name) + ">" + caseType.getDisplayName());
    }

    @Override
    protected void onPause() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (caseType == null) {
            finish();
            return;
        }
        if (caseType.isCaseListLoaded()) {
            adapter.update();
            hideProgress();
            checkForDoubleCasesNames();
            checkIfAdapterIsEmppy();
        } else {
            Manager.getCaseManager().loadCaseType(caseType, getApplicationContext());
        }
    }

    @Override
    protected void onListItemClick(ListView l,
                                   View v,
                                   int position,
                                   long id) {
        super.onListItemClick(l, v, position, id);
        Case caseInstance = adapter.getItem(position);

        Intent intent = new Intent(this, CaseDetailsActivity.class);
        intent.putExtra(CaseDetailsActivity.EXTRA_CASE_UUID, caseInstance.getCaseUUID());
        intent.putExtra(CaseDetailsActivity.EXTRA_CASE_TYPE, caseType.getId());
        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (caseType != null) {
            caseType.unregisterEventListener(onCaseTypeEvent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        CompatibilityUtils.setShowAsAction(menu.add(0, MENU_FILTER_ID, 0, R.string.general_preferences)
                                               .setIcon(R.drawable.ic_filter_select), MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (isFilterSelected) {
            setFilterDisableIcon();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_FILTER_ID:
//                if (!isFilterSelected) {
                dialog = new CaseFilterDialog(this, filterSelected, sortSelected);
                dialog.setOnFilterClickedListener(onFilterClicked);
                dialog.show();
//                } else {
//                    setFilterSelectIcon();
//                    adapter.applyFilter(null);
//                    isFilterSelected = false;
//                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return adapter;
    }

    private void checkIfAdapterIsEmppy() {
        if (adapter == null || adapter.getCount() == 0) {
            noItemsLabel.setVisibility(View.VISIBLE);
            getListView().setVisibility(View.GONE);
        } else {
            noItemsLabel.setVisibility(View.GONE);
            getListView().setVisibility(View.VISIBLE);
        }
    }

    private void setFilterSelectIcon() {
        menu.findItem(MENU_FILTER_ID).setIcon(R.drawable.ic_filter_select);
    }

    private void setFilterDisableIcon() {
        menu.findItem(MENU_FILTER_ID).setIcon(R.drawable.ic_filter_disable);
    }

    private void checkForDoubleCasesNames() {
        if (caseType != null) {
            CaseCollection collection = caseType.getCases();
            List<String> caseNames = new ArrayList<>();
            for (Case instance : collection.values()) {
                if (instance.isClosed()) {
                    continue;
                }

                String name = instance.getPrimaryVariableValue();
                // TODO is this comparison case sensitive?
                if (caseNames.contains(name)) {
                    createErrorDialog(getString(R.string.view_cases_list_duplicate_names), false);
                    break;
                }
                caseNames.add(name);
            }
        }
    }

    private void createErrorDialog(String errorMsg,
                                   final boolean shouldExit) {
        Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog", "show");
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,
                                int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance()
                               .getActivityLogger()
                               .logAction(this, "createErrorDialog", shouldExit ? "exitApplication" : "OK");
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }

    private void hideProgress() {
        if (progressLayout != null) {
            progressLayout.setVisibility(View.GONE);
        }
        if (getListView() != null) {
            getListView().setVisibility(View.VISIBLE);
        }
    }

    private void showProgress(@StringRes int text) {
        if (progressLayout != null) {
            progressLayout.setVisibility(View.VISIBLE);
            progressText.setText(text);
        }
        if (getListView() != null) {
            getListView().setVisibility(View.GONE);
        }
    }
}
