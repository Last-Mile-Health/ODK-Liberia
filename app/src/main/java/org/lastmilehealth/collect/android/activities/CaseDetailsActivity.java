package org.lastmilehealth.collect.android.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.cases.CaseManager;
import org.lastmilehealth.collect.android.listeners.OnEventListener;
import org.lastmilehealth.collect.android.manager.Manager;

/**
 * Activity for showing case details.
 * <p>
 * Created by Anton Donchev on 19.05.2017.
 */

public class CaseDetailsActivity extends Activity {
    public static final String EXTRA_CASE_UUID = "caseUUID";
    public static final String EXTRA_CASE_TYPE = "caseType";
    private static final String CASE_DETAILS_FRAGMENT_TAG = "CaseDetailsFragment";

    private OnEventListener onCaseManagerEvent = new OnEventListener() {
        @Override
        public void onEvent(int eventId) {
            switch (eventId) {
                case CaseManager.State.CASE_DATA_INVALIDATED:
                    Fragment oldFragment = getFragmentManager().findFragmentByTag(CASE_DETAILS_FRAGMENT_TAG);
                    Fragment newFragment = new CaseDetailsFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    if (oldFragment != null) {
                        transaction.remove(oldFragment);
                    }
                    transaction.add(R.id.container, newFragment, CASE_DETAILS_FRAGMENT_TAG).commitAllowingStateLoss();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_details);
        Manager.getCaseManager().registerEventListener(onCaseManagerEvent);

        Fragment fragment = getFragmentManager().findFragmentByTag(CASE_DETAILS_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new CaseDetailsFragment();
            getFragmentManager().beginTransaction().add(R.id.container, fragment, CASE_DETAILS_FRAGMENT_TAG).commitAllowingStateLoss();
        }



        // TODO setTitle(getString(R.string.app_name) + ">" + caseType.getDisplayName());

    }

    @Override
    protected void onDestroy() {
        Manager.getCaseManager().unregisterEventListener(onCaseManagerEvent);
        super.onDestroy();
    }
}
