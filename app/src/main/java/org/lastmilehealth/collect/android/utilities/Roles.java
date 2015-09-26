package org.lastmilehealth.collect.android.utilities;

import java.util.ArrayList;

/**
 * Created by fklymenko on 6/26/2015.
 */
public class Roles {

    private String name;
    private ArrayList<String> permission;

    public String getName() {
        return name;
    }

    public ArrayList<String> getPermission() {
        return permission;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPermission(ArrayList<String> permission) {
        this.permission = permission;
    }
}
