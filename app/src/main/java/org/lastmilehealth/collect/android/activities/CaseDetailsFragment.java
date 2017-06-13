package org.lastmilehealth.collect.android.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.cases.CaseElement;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.listeners.OnEventListener;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Fragment that contains the case details.
 * <p>
 * Created by Anton Donchev on 19.05.2017.
 */

public class CaseDetailsFragment extends Fragment {
    private Case caseInstance;
    private CaseType caseType;
    private View progressBar;
    private ViewGroup container;
    private boolean viewAdded = false;
    final OnEventListener onCaseTypeEvent = new OnEventListener() {
        @Override
        public void onEvent(int eventId) {
            switch (eventId) {
                case CaseType.Event.CASE_DETAILS_FAILED:
                case CaseType.Event.CASE_LIST_FAILED:
                    // TODO show failed dialog.
                    Toast.makeText(Collect.getInstance(), "Failed to load case", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    break;

                case CaseType.Event.CASE_DETAILS_LOADED:
                    loadCaseDetails();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_case_view_details, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        try {
            String caseUUID = getActivity().getIntent().getStringExtra(CaseDetailsActivity.EXTRA_CASE_UUID);
            String caseTypeId = getActivity().getIntent().getStringExtra(CaseDetailsActivity.EXTRA_CASE_TYPE);

            caseType = Manager.getCaseManager().findCaseTypeById(caseTypeId);
            caseInstance = caseType.getCases().get(caseUUID);
        } catch (Exception e) {
            caseInstance = null;
        }
        if (caseInstance == null) {
            // Maybe some error and then finish?
            getActivity().finish();
            return;
        }
        viewAdded = false;

        progressBar = view.findViewById(R.id.progress_bar);
        container = (ViewGroup) view.findViewById(R.id.container);

        showProgressBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (caseType == null || !caseType.isCaseListLoaded()) {
            getActivity().finish();
            return;
        }
        if (viewAdded) {
            return;
        }
        if (!caseType.isCaseDetailsLoaded()) {
            loadCaseTypeDetails();
        } else {
            loadCaseDetails();
        }
    }

    @Override
    public void onDestroy() {
        caseType.unregisterEventListener(onCaseTypeEvent);
        super.onDestroy();
    }

    private void loadCaseDetails() {
        if (!viewAdded) {
            Collection<InstanceElement> allForms = new ArrayList<>();
            allForms.add(caseInstance.getPrimaryForm());
            Collection<InstanceElement> secondaryForms = caseType.getCaseSecondaryForms(caseInstance);
            if (secondaryForms != null) {
                allForms.addAll(secondaryForms);
            }
            CaseElement caseElement = caseType.getCaseElement();
            if (caseElement != null) {
                View view = caseElement.generateView(container, allForms);
                if (view != null) {
                    viewAdded = true;
                    container.addView(view);
                    hideProgressBar();
                }
            } else {
                // TODO error ?
                hideProgressBar();
            }
        }
    }

    private void loadCaseTypeDetails() {
        caseType.registerEventListener(onCaseTypeEvent);
        Manager.getCaseManager().loadCaseTypeDetails(caseType);
    }

    private void showProgressBar() {
        container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        progressBar.startAnimation(animation);
    }

    private void hideProgressBar() {
        container.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        container.startAnimation(animation);
    }
}
