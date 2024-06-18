package com.kayyagari;

/*
   Copyright [2024] [Kiran Ayyagari]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

import java.util.Map;
import java.util.TreeMap;

import com.kayyagari.objmeld.OgnlContent;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class FieldNode implements Comparable<FieldNode>, OgnlContent {
    private String name;
    private String path;
    private Object value;
    private FieldType type = FieldType.UNKNOWN;

    private Map<String, FieldNode> children = new TreeMap<>();

    public FieldNode(String name, String path) {
        this(name, path, null);
    }

    public FieldNode(String name, String path, Object value) {
        this.name = name;
        this.path = path;
        this.value = value;
    }

    @Override
    public Map<String, OgnlContent> children() {
        return (Map)children;
    }

    @Override
    public OgnlContent emptyPeer() {
        return new FieldNode(name, path, "");
    }

    @Override
    public String toText() {
        if(value == null 
                || type == FieldType.OBJECT 
                || type == FieldType.ARRAY 
                || type == FieldType.LIST) {
            return "";
        }
        
        String t = value.toString();
//        if(t.length() > 250) {
//            t = t.substring(0, 20);
//        }
        return t;
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

    public Map<String, FieldNode> getChildren() {
        return children;
    }

    public void addChild(FieldNode child) {
        children.put(child.name, child);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FieldNode other = (FieldNode) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        for(FieldNode child : children.values()) {
            sb.append(", ").append(child.toString());
        }

        return sb.toString();
    }
}
