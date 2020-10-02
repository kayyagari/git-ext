package com.kayyagari;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DefaultBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.io.IOUtils;

import com.kayyagari.objmeld.OgnlComparison;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

import ognl.OgnlContext;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
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
            case ARRAY:
            case SET:
            case MAP:
                fn.setType(vt);
                break;
            case LIST:
                fn.setType(vt);
                List lst = (List)fn.getValue();
                for(int k=0; k< lst.size(); k++) {
                    String path = fn.getPath() + "[" + k + "]";
                    FieldNode chFn = new FieldNode(path, path, lst.get(k));
                    fn.addChild(chFn);
                    gatherFields(chFn);
                }
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
                    || Number.class.isAssignableFrom(c)) {
                primitive = true;
            }
        }

        if(primitive) {
            vt = FieldType.PRIMITIVE;
        }

        if(c.isArray()) {
            vt = FieldType.ARRAY;
        }

        if(List.class.isAssignableFrom(c)) {
            vt = FieldType.LIST;
        }

        if(Set.class.isAssignableFrom(c)) {
            vt = FieldType.SET;
        }

        if(Map.class.isAssignableFrom(c)) {
            vt = FieldType.MAP;
        }

        return vt;
    }

    private void insertMissingPeers() {
        for(Map.Entry<String, FieldNode> e : leftNodes.entrySet()) {
            if(!rightNodes.containsKey(e.getKey())) {
                rightNodes.put(e.getKey(), (FieldNode)e.getValue().emptyPeer());
            }
        }

        for(Map.Entry<String, FieldNode> e : rightNodes.entrySet()) {
            if(!leftNodes.containsKey(e.getKey())) {
                leftNodes.put(e.getKey(), (FieldNode)e.getValue().emptyPeer());
            }
        }
    }

    public JPanel getVisualPanel() {
        insertMissingPeers();
        return OgnlComparison.prepare(new ArrayList<>(leftNodes.values()), new ArrayList<>(rightNodes.values()), false);
    }

    public void show() {
        JPanel panel = getVisualPanel();
        OgnlComparison.show(panel);
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
