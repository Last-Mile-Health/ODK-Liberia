package org.lastmilehealth.collect.android.parser;

import java.util.ArrayList;

/**
 * Created by user on 7/1/15.
 */
public class Role {

    private  String name;
    private ArrayList<String> permissions = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(ArrayList<String> permissions) {
        this.permissions = permissions;
    }
}
