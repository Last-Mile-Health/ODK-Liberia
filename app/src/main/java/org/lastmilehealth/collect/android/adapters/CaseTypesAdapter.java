package org.lastmilehealth.collect.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.manager.Manager;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying all the case types.
 * <p>
 * Created by Anton Donchev on 10.05.2017.
 */

public class CaseTypesAdapter extends BaseAdapter {
    private List<CaseType> caseTypes = new ArrayList<>();

    public CaseTypesAdapter() {
        update();
    }

    @Override
    public int getCount() {
        return caseTypes.size();
    }

    @Override
    public CaseType getItem(int position) {
        return caseTypes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position,
                        View convertView,
                        ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cases_type, parent, false);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        }

        holder.txtTitle.setText(caseTypes.get(position).getDisplayName());

        return convertView;
    }

    public void update() {
        caseTypes.clear();
        List<? extends CaseType> cases = Manager.getCaseManager().getCaseTypes();
        if (cases != null) {
            caseTypes.addAll(cases);
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        private TextView txtTitle;
    }
}
