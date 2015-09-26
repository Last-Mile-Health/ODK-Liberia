/*
 * Copyright (C) 2012 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.lastmilehealth.collect.android.utilities;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.parser.TinyDB;
import org.lastmilehealth.collect.android.provider.FormsProviderAPI;

import java.util.ArrayList;

/**
 * Implementation of cursor adapter that displays the version of a form if a form has a version.
 *
 * @author mitchellsundt@gmail.com
 */
public class BlankFormAdapter extends SimpleCursorAdapter {

    private final String CURRENT_PERMISSIONS = "current_permission";
    private final Context mContext;
    private final String versionColumnName;
    private final ViewBinder originalBinder;
    private final LayoutInflater inflater;
    private int mLayout;


    public BlankFormAdapter(String versionColumnName, Context context, int layout, Cursor c, String[] from, int[] to) {

        super(context, layout, c, from, to);
        this.versionColumnName = versionColumnName;
        mContext = context;
        mLayout = layout;
        originalBinder = getViewBinder();
        this.inflater = LayoutInflater.from(context);
        setViewBinder(new ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor,
                                        int columnIndex) {
                String columnName = cursor.getColumnName(columnIndex);
                if (!columnName.equals(BlankFormAdapter.this.versionColumnName)) {
                    if (originalBinder != null) {
                        return originalBinder.setViewValue(view, cursor, columnIndex);
                    }
                    return false;
                } else {
                    String version = cursor.getString(columnIndex);
                    TextView v = (TextView) view;
                    if (version != null) {
                        v.setText(mContext.getString(R.string.version) + " " + version);
                        v.setVisibility(View.VISIBLE);
                    } else {
                        v.setText(null);
                        v.setVisibility(View.GONE);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        String formName = cursor.getString(cursor.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
        if (!permissionIsAvailable(formName)) {
            return new View(context);
        }
        return inflater.inflate(mLayout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if (view != null) {
            super.bindView(view, context, cursor);
            TextView formView = (TextView) view.findViewById(R.id.blank_form_item_name);
            String formName = cursor.getString(cursor.getColumnIndexOrThrow(FormsProviderAPI.FormsColumns.DISPLAY_NAME));
            if (permissionIsAvailable(formName)) {
                formView.setText(formName);
            }
        }
    }

    private boolean permissionIsAvailable(String formName) {

        if (mContext == null) {
            return true;
        }

        TinyDB tinyDB = new TinyDB(mContext);
        ArrayList<String> permissionList = tinyDB.getListString(CURRENT_PERMISSIONS);

        if (permissionList.isEmpty()) {
            return true;
        }

        for (String permission : permissionList) {
            if (formName.equals(permission)) {
                return true;
            }
        }
        return false;
    }
}