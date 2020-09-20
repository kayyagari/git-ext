package com.kayyagari;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DefaultBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.io.IOUtils;

import com.kayyagari.objmeld.OgnlComparison;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

import ognl.OgnlContext;

public class ObjectDiff {
    private Object left;
    private Object right;

    private Map<String, FieldNode> leftNodes;
    private Map<String, FieldNode> rightNodes;

    private OgnlContext ctx = new OgnlContext(null, null, new DefaultMemberAccess(true));

    private static final BeanUtilsBean beanUtilBean = new BeanUtilsBean();

    static {
        beanUtilBean.getPropertyUtils().removeBeanIntrospector(DefaultBeanIntrospector.INSTANCE);
        beanUtilBean.getPropertyUtils().addBeanIntrospector(FieldOnlyIntrospector.INSTANCE);
    }

    public ObjectDiff(Object left, Object right) {
        this.left = left;
        this.right = right;
    }

    public void create() throws Exception {
        //Channel ch = (Channel)left;
        //ch.getSourceConnector().getProperties().setPluginProperties(new HashSet<>());
        leftNodes = create(left);
        rightNodes = create(right);
//        ch.getSourceConnector().setProperties(null);
//        List<String> lst = exprsMap.get("sourceConnector");
//        for(String expr : lst) {
//            System.out.print(expr + "-->");
//            System.out.println(Ognl.getValue(expr, ctx, left));
//        }
    }

    private Map<String, FieldNode> create(Object root) throws Exception {
        PropertyUtilsBean putils = beanUtilBean.getPropertyUtils();
        Map<String, Object> map = new TreeMap<>(putils.describe(root)); // sort the fields, never the expressions

        Map<String, FieldNode> nodes = new TreeMap<>();
        for(Map.Entry<String, Object> e : map.entrySet()) {
            String name = e.getKey();
            if(name.equals("name") || name.equals("description") || name.equals("deployScript")) {
                
            }
            String path = name;
            Object value = e.getValue();
            FieldNode child = new FieldNode(name, path, value);
            gatherFields(child);
            nodes.put(name, child);
            System.out.println(child);
        }
        
        return nodes;
    }

    private void gatherFields(FieldNode fn) throws Exception {
        if(fn.getValue() != null) {
            FieldType vt = getType(fn.getValue());
            switch (vt) {
            case PRIMITIVE:
            case INDEXED:
            case SET:
            case MAP:
                fn.setType(vt);
                break;

            case OBJECT:
                fn.setType(vt);
                Map<String, Object> map = beanUtilBean.getPropertyUtils().describe(fn.getValue());
                map = new TreeMap<>(map); // sort the fields, never the expressions
                for(Map.Entry<String, Object> e : map.entrySet()) {
                    String name = e.getKey();
                    String path = fn.getPath() + "." + name;
                    FieldNode child = new FieldNode(name, path, e.getValue());
                    fn.addChild(child);
                    gatherFields(child);
                }
                break;
                
            case UNKNOWN:
                throw new IllegalStateException("unknonw field type for field path " + fn.getPath());
                    
            }
        }
    }

    private FieldType getType(Object obj) {
        Class<?> c = obj.getClass();
        
        FieldType vt = FieldType.OBJECT;
        boolean primitive = c.isPrimitive();
        if(!primitive) {
            if(c == String.class
                    || c == Character.class
                    || c == Boolean.class
                    || c.isAssignableFrom(Number.class)) {
                primitive = true;
            }
        }

        if(primitive) {
            vt = FieldType.PRIMITIVE;
        }

        if(c.isArray() || c.isAssignableFrom(List.class)) {
            vt = FieldType.INDEXED;
        }

        if(c.isAssignableFrom(Set.class)) {
            vt = FieldType.SET;
        }

        if(c.isAssignableFrom(Map.class)) {
            vt = FieldType.MAP;
        }

        return vt;
    }

    public void show() {
        for(Map.Entry<String, FieldNode> e : leftNodes.entrySet()) {
            if(!rightNodes.containsKey(e.getKey())) {
                System.out.println("not found " + e.getKey());
                rightNodes.put(e.getKey(), (FieldNode)e.getValue().emptyPeer());
            }
        }

        for(Map.Entry<String, FieldNode> e : rightNodes.entrySet()) {
            if(!leftNodes.containsKey(e.getKey())) {
                leftNodes.put(e.getKey(), (FieldNode)e.getValue().emptyPeer());
            }
        }

        OgnlComparison.show(new ArrayList<>(leftNodes.values()), new ArrayList<>(rightNodes.values()));
    }

    public static void main(String[] args) throws Exception {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        serializer.init("3.9.1");
        
        String chXml1 = IOUtils.resourceToString("channel-for-diffing-version1.xml", Charset.forName("utf-8"), ObjectDiff.class.getClassLoader());
        Channel chLeft = serializer.deserialize(chXml1, Channel.class);
        
        String chXml2 = IOUtils.resourceToString("channel-for-diffing-version2.xml", Charset.forName("utf-8"), ObjectDiff.class.getClassLoader());
        Channel chRight = serializer.deserialize(chXml2, Channel.class);
        //System.out.println(chRight);

        ObjectDiff od = new ObjectDiff(chLeft, chRight);
        od.create();
        od.show();
    }
}
