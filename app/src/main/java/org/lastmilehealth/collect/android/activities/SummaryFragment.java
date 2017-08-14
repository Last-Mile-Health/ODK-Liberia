package org.lastmilehealth.collect.android.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.dialog.FilterDialog;
import org.lastmilehealth.collect.android.dialog.SummaryFilterDialog;
import org.lastmilehealth.collect.android.filter.Filter;
import org.lastmilehealth.collect.android.listeners.OnEventListener;
import org.lastmilehealth.collect.android.manager.Manager;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.summary.SummaryCollection;
import org.lastmilehealth.collect.android.summary.SummaryManager;
import org.lastmilehealth.collect.android.utilities.CompatibilityUtils;

import java.util.Collection;

import static org.lastmilehealth.collect.android.activities.CaseListActivity.MENU_FILTER_ID;

/**
 * Created by Anton Donchev on 09.06.2017.
 */

public class SummaryFragment extends Fragment {
    private View progressBar;
    private ViewGroup container;
    private boolean viewAdded = false;
    private SummaryFilterDialog dialog;
    private Menu menu;
    private Filter<InstanceElement> filterSelected;

    private FilterDialog.OnFilterClicked<InstanceElement> onFilterClicked = new FilterDialog.OnFilterClicked<InstanceElement>() {
        @Override
        public void onFilterSelected(Filter<InstanceElement> filter) {
            filterSelected = filter;
            recreateViews();
        }

        @Override
        public void onSortMethodSelected(Filter<InstanceElement> sortMethod) {
            // No sorting in summary.
        }

        @Override
        public void clearAll() {
            filterSelected = null;
            recreateViews();
        }
    };

    private OnEventListener onSummaryManagerEvent = new OnEventListener() {
        @Override
        public void onEvent(int eventId) {
            switch (eventId) {
                case SummaryManager.Event.FAILED:
                    // TODO maybe some error;
                    hideProgressBar();
                    break;

                case SummaryManager.Event.LOADED:
                    addSummariesView();
                    hideProgressBar();
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        this.menu = menu;
        CompatibilityUtils.setShowAsAction(menu.add(0, MENU_FILTER_ID, 0, R.string.general_preferences).setIcon(getFilterIcon()),
                                           MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_FILTER_ID:
//                if (filterSelected == null) {
                dialog = new SummaryFilterDialog(getActivity(), filterSelected);
                dialog.setOnFilterClickedListener(onFilterClicked);
                dialog.show();
//                } else {
//                    getActivity().invalidateOptionsMenu();
//                    filterSelected = null;
//                    recreateViews();
//                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progress_bar);
        container = (ViewGroup) view.findViewById(R.id.element_container);
        Manager.getSummaryManager().registerEventListener(onSummaryManagerEvent);
    }

    @Override
    public void onDestroyView() {
        Manager.getSummaryManager().unregisterEventListener(onSummaryManagerEvent);
        Manager.getSummaryManager().dispose();
        super.onDestroyView();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Manager.getSummaryManager().isLoaded() || !viewAdded) {
            Manager.getSummaryManager().load();
            showProgressBar();
        }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(MENU_FILTER_ID).setIcon(getFilterIcon());
        super.onPrepareOptionsMenu(menu);
    }

    private int getFilterIcon() {
        int icon;
//        if (filterSelected == null) {
        icon = R.drawable.ic_filter_select;
//        } else {
//            icon = R.drawable.ic_filter_disable;
//        }
        return icon;
    }

    public void showProgressBar() {
        container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        container.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    public void addSummariesView() {
        if (!viewAdded) {
            SummaryCollection summaries = Manager.getSummaryManager().getSummaries();
            Collection<InstanceElement> instances = Manager.getSummaryManager().getInstances();
            if (filterSelected != null) {
                instances = filterSelected.filter(instances);
            }
            View view = summaries.createView(container, instances);
            if (view != null) {
                container.addView(view);
                viewAdded = true;
            } else {
                // TODO some error maybe?
            }
        }
    }

    public void recreateViews() {
        if (viewAdded) {
            container.removeAllViews();
        }

        viewAdded = false;
        if (Manager.getSummaryManager().isLoaded()) {
            addSummariesView();
        }
    }
}
