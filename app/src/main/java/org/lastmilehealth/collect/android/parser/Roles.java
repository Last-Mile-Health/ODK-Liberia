package org.lastmilehealth.collect.android.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 7/1/15.
 */
public class Roles {

    List<Role> roles = new ArrayList<>();

    public Roles(List<Role> entries) {
        this.roles = entries;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<Role> roles) {
        this.roles = roles;
    }
}
