package org.lastmilehealth.collect.android.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anton Donchev on 17.05.2017.
 */

public class InstanceElement {
    private final List<InstanceElement> children = new ArrayList<>();
    private String name;
    private String value;
    private InstanceElement parent;

    private final Map<String, String> attributes = new HashMap<>();

    public List<InstanceElement> getChildren() {
        return children;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InstanceElement getParent() {
        return parent;
    }

    public void setParent(InstanceElement parent) {
        this.parent = parent;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void dispose() {
        if (parent != null) {
            // This will try to dispose the parent
            // In order not to get into an infinite loop while disposing children the parent should be null.
            InstanceElement parentCopy = parent;
            parent = null;
            parentCopy.dispose();
        }
        if (children.size() > 0) {
            // If there is parent this array should be empty when the child tries to dispose of itself so
            // that there are no infinite loops.
            List<InstanceElement> childrenCopy = new ArrayList<>(children);
            children.clear();
            for (InstanceElement child : childrenCopy) {
                child.dispose();
            }
            childrenCopy.clear();
        }
    }
}
