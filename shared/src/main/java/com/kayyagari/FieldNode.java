package com.kayyagari;

import java.util.SortedSet;
import java.util.TreeSet;

public class FieldNode implements Comparable<FieldNode> {
    private String name;
    private String path;
    private Object value;
    private FieldType type;

    private SortedSet<FieldNode> children = new TreeSet<>();

    public FieldNode(String name, String path, Object value) {
        this.name = name;
        this.path = path;
        this.value = value;
    }

    @Override
    public int compareTo(FieldNode o) {
        return name.compareTo(o.name);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public SortedSet<FieldNode> getChildren() {
        return children;
    }

    public void addChild(FieldNode child) {
        children.add(child);
    }
}
