package org.lastmilehealth.collect.android.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.cases.Case;
import org.lastmilehealth.collect.android.cases.CaseCollection;
import org.lastmilehealth.collect.android.cases.CaseType;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying case list.
 * <p>
 * Created by Anton Donchev on 12.05.2017.
 */

public class CasesAdapter extends BaseAdapter {
    private final CaseType caseType;
    private List<Case> cases = new ArrayList<>();

    public CasesAdapter(CaseType caseType) {
        this.caseType = caseType;
        update();
    }

    @Override
    public int getCount() {
        return cases.size();
    }

    @Override
    public Case getItem(int position) {
        return cases.get(position);
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

        Case instance = getItem(position);
        String primaryVariableValue = instance.getPrimaryVariableValue();
        if (TextUtils.isEmpty(primaryVariableValue)) {
            primaryVariableValue = "<Empty>";
        }
        holder.txtTitle.setText(primaryVariableValue);

        return convertView;
    }

    public void update() {
        CaseCollection cases = caseType.getCases();
        this.cases.clear();
        if (cases != null) {
            this.cases.addAll(cases.values());
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView txtTitle;
    }
}
