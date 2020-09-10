package com.kayyagari;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DefaultBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.io.IOUtils;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

import ognl.MemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;

public class ObjectDiff {
    private Object left;
    private Object right;

    private static final Set<Class> primitiveObjClasses = new HashSet<>();
    private static final BeanUtilsBean beanUtilBean = new BeanUtilsBean();

    static {
        beanUtilBean.getPropertyUtils().removeBeanIntrospector(DefaultBeanIntrospector.INSTANCE);
        beanUtilBean.getPropertyUtils().addBeanIntrospector(FieldOnlyIntrospector.INSTANCE);
        
        primitiveObjClasses.add(String.class);
        primitiveObjClasses.add(Character.class);
        primitiveObjClasses.add(Boolean.class);
        primitiveObjClasses.add(Number.class);
    }

    public ObjectDiff(Object left, Object right) {
        this.left = left;
        this.right = right;
    }

    public void create() throws Exception {
        OgnlContext ctx = new OgnlContext(null, null, new DefaultMemberAccess(true));

        PropertyUtilsBean putils = beanUtilBean.getPropertyUtils();
        Map<String, Object> map = new TreeMap<>(putils.describe(left)); // sort the fields, never the expressions

        Map<String, List<String>> exprsMap = new LinkedHashMap<>();
        for(Map.Entry<String, Object> e : map.entrySet()) {
            List<String> lst = new ArrayList<>();
            Stack<String> path = new Stack<>();
            gatherFields(e.getKey(), e.getValue(), path, lst);
            System.out.println(e.getKey() + " " + lst);
            exprsMap.put(e.getKey(), lst);
        }

        Channel ch = (Channel)left;
        ch.getSourceConnector().setProperties(null);
        List<String> lst = exprsMap.get("sourceConnector");
        for(String expr : lst) {
            System.out.print(expr + "-->");
            System.out.println(Ognl.getValue(expr, ctx, left));
        }
    }
    
    private void gatherFields(String objFieldName, Object obj, Stack<String> path, List<String> exprs) throws Exception {
        path.push(objFieldName);
        if(obj != null) {
            FieldType vt = getType(obj);
            switch (vt) {
            case PRIMITIVE:
                exprs.add(toExpr(path));
                break;

            case INDEXED:
                exprs.add(toExpr(path));
                break;

            case SET:
                exprs.add(toExpr(path));
                break;

            case MAP:
                exprs.add(toExpr(path));
                break;

            case OBJECT:
                exprs.add(toExpr(path));
                Map<String, Object> map = beanUtilBean.getPropertyUtils().describe(obj);
                map = new TreeMap<>(map); // sort the fields, never the expressions
                for(Map.Entry<String, Object> e : map.entrySet()) {
                    gatherFields(e.getKey(), e.getValue(), path, exprs);
                }
                break;
            }
        }
        else {
            exprs.add(toExpr(path));
        }
        
        path.pop();
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

    private String toExpr(Stack<String> path) {
        int len = path.size();
        StringBuilder sb = new StringBuilder();
        if(len > 0) {
            for(int i=0; i < len; i++) {
                sb.append(path.elementAt(i)).append('.');
            }
            sb.deleteCharAt(sb.length()-1);
        }
        
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        serializer.init("3.9.1");
        String xml = IOUtils.resourceToString("channel-for-diffing-version1.xml", Charset.forName("utf-8"), ObjectDiff.class.getClassLoader());
        Channel ch = serializer.deserialize(xml, Channel.class);
        
        ObjectDiff od = new ObjectDiff(ch, null);
        od.create();
    }
}
