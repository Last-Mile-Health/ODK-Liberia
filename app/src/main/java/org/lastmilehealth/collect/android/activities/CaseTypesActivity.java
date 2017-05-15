package org.lastmilehealth.collect.android.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.adapters.CaseTypesAdapter;
import org.lastmilehealth.collect.android.cases.CaseManager;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.listeners.OnEventListener;
import org.lastmilehealth.collect.android.manager.Manager;

/**
 * Created by Anton Donchev on 10.05.2017.
 */

public class CaseTypesActivity extends ListActivity {
    private CaseTypesAdapter adapter = new CaseTypesAdapter();
    private View progressBar, progressLayout;
    private TextView progressText;

    private OnEventListener onManagerEvent = new OnEventListener() {
        @Override
        public void onEvent(int eventId) {
            switch (eventId) {
                case CaseManager.State.LOADING:
                    if (!Manager.getCaseManager().isLoaded()) {
                        showProgress(R.string.case_types_loading);
                    }
                    break;

                case CaseManager.State.FAILED:
                case CaseManager.State.LOADED:
                    hideProgress();
                    Manager.getCaseManager().unregisterEventListener(onManagerEvent);
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

        setTitle(getString(R.string.app_name) + ">" + getString(R.string.case_types_screen_title));

        setListAdapter(adapter);
        Manager.getCaseManager().registerEventListener(onManagerEvent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Manager.getCaseManager().isLoaded()) {
            Manager.getCaseManager().loadManager();
        } else {
            hideProgress();
            adapter.update();
        }
    }

    @Override
    protected void onDestroy() {
        Manager.getCaseManager().unregisterEventListener(onManagerEvent);
        super.onDestroy();
    }

    @Override
    protected void onListItemClick(ListView l,
                                   View v,
                                   int position,
                                   long id) {
        super.onListItemClick(l, v, position, id);
        CaseType caseType = adapter.getItem(position);
        Intent intent = new Intent(this, CaseListActivity.class);
        intent.putExtra(CaseListActivity.EXTRA_CASE_TYPE_ID, caseType.getId());
        startActivity(intent);
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
