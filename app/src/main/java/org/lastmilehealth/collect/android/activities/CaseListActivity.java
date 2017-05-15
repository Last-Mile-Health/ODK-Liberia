package org.lastmilehealth.collect.android.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.adapters.CasesAdapter;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.listeners.OnEventListener;
import org.lastmilehealth.collect.android.manager.Manager;

/**
 * Created by Anton Donchev on 12.05.2017.
 */

public class CaseListActivity extends ListActivity {
    public static final String EXTRA_CASE_TYPE_ID = "CaseTypeId";
    private View progressBar, progressLayout;
    private TextView progressText;
    private CasesAdapter adapter;
    private CaseType caseType;
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
                case CaseType.Event.CASES_LIST_FAILED:
                    hideProgress();
                    adapter.update();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_types);

        progressLayout = findViewById(R.id.progress_layout);
        progressBar = findViewById(R.id.progress_bar);
        progressText = (TextView) findViewById(R.id.progress_text);


        String caseTypeId = getIntent().getStringExtra(EXTRA_CASE_TYPE_ID);
        caseType = Manager.getCaseManager().findCaseTypeById(caseTypeId);

        adapter = new CasesAdapter(caseType);
        setListAdapter(adapter);

        if (caseType == null) {
            finish();
            return;
        }
        caseType.registerEventListener(onCaseTypeEvent);

        setTitle(getString(R.string.app_name) + ">" + caseType.getDisplayName());
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
        } else {
            Manager.getCaseManager().loadCaseType(caseType, getApplicationContext());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (caseType != null) {
            caseType.unregisterEventListener(onCaseTypeEvent);
        }
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
