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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanIntrospector;
import org.apache.commons.beanutils.IntrospectionContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class FieldOnlyIntrospector implements BeanIntrospector {
    /** The singleton instance of this class. */
    public static final BeanIntrospector INSTANCE = new FieldOnlyIntrospector();

    /** Log instance */
    private final Log log = LogFactory.getLog(getClass());

    @Override
    public void introspect(IntrospectionContext icontext) throws IntrospectionException {
        BeanInfo beanInfo = null;
        Class target = icontext.getTargetClass();
        try {
            beanInfo = Introspector.getBeanInfo(target);
        } catch (final IntrospectionException e) {
            // no descriptors are added to the context
            log.error(
                    "Error when inspecting class " + icontext.getTargetClass(),
                    e);
            return;
        }

        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        if (descriptors == null) {
            descriptors = new PropertyDescriptor[0];
        }

        //handleIndexedPropertyDescriptors(icontext.getTargetClass(), descriptors);
        List<PropertyDescriptor> propOnlyDescriptors = new ArrayList<>();
        for(PropertyDescriptor pd : descriptors) {
            try {
                Field f = target.getDeclaredField(pd.getName());
                if(f.getModifiers() == Modifier.TRANSIENT) {
                    continue;
                }
                propOnlyDescriptors.add(pd);
            }
            catch(NoSuchFieldException e) {
                log.debug("ignoring property descriptor " + pd.getDisplayName() + " because there is no backing property");
            }
        }

        icontext.addPropertyDescriptors(propOnlyDescriptors.toArray(new PropertyDescriptor[propOnlyDescriptors.size()]));
    }
}
