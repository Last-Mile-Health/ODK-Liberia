package org.lastmilehealth.collect.android.activities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.lastmilehealth.collect.android.R;

/**
 * Created by Anton Donchev on 09.06.2017.
 */

public class SummaryActivity extends Activity {
    public static final String TAG_SUMMARY_FRAGMENT = "SummaryStatisticsFragment";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        Fragment fragment = getFragmentManager().findFragmentByTag(TAG_SUMMARY_FRAGMENT);
        if (fragment == null) {
            fragment = new SummaryFragment();
            getFragmentManager().beginTransaction().add(R.id.container, fragment, TAG_SUMMARY_FRAGMENT).commitAllowingStateLoss();
        }

    }
}
