/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.     
 * 
 * The contents of this file are subject to the terms of the Common Development 
 * and Distribution License("CDDL") (the "License").  You may not use this file 
 * except in compliance with the License.
 * 
 * You can obtain a copy of the License at 
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.dummy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.*;

@ConnectorClass(displayNameKey="DummyConnector",configurationClass=DummyConfiguration.class)
public class DummyConnector
    implements PoolableConnector, CreateOp, DeleteOp, SearchOp<String>, UpdateOp, SchemaOp
{

    public DummyConnector() {
    }

    public void dispose() {
    }

    public void checkAlive() {
    }

    public Configuration getConfiguration() {
        return _configuration;
    }

    public void init(Configuration cfg) {
        _configuration = (DummyConfiguration)cfg;
        _cacheKey = Integer.valueOf(cfg.hashCode());
        ConcurrentMap<Uid, Set<Attribute>> initialMap = new ConcurrentHashMap<Uid, Set<Attribute>>();
        _map = _objectCacheMap.putIfAbsent(_cacheKey, initialMap);
        if (_map == null) {
            _map = initialMap;
        }
        if (null == _schema) {
            //The concurrent call is harmless here
            _schema = schema();
        }
    }

    public Uid create(ObjectClass objClass, Set<Attribute> attrs, OperationOptions options) {
        validateAttributes(objClass, attrs, true);
        Uid uid = generateUid();
        _map.put(uid, attrs);
        return uid;
    }

    private void validateAttributes(ObjectClass oclass, Set<Attribute> attrs, boolean checkRequired) {
        ObjectClassInfo oci = _schema.findObjectClassInfo(oclass.getObjectClassValue());
        Map<String, Attribute> attrMap = new HashMap<String, Attribute>(AttributeUtil.toMap(attrs));
        for (AttributeInfo attributeInfo : oci.getAttributeInfo()) {
            if(checkRequired && attributeInfo.isRequired() && !attrMap.containsKey(attributeInfo.getName()))
                throw new IllegalArgumentException((new StringBuilder()).append("Required attribute ").append(attributeInfo.getName()).append(" is missing").toString());
            if(!(attributeInfo.isCreateable() || attributeInfo.isUpdateable()) && attrMap.containsKey(attributeInfo.getName()))
                throw new IllegalArgumentException((new StringBuilder()).append("Non-writeable attribute ").append(attributeInfo.getName()).append(" is present").toString());
        }

        Map<String, AttributeInfo> ociMap = AttributeInfoUtil.toMap(oci.getAttributeInfo());
        for (Attribute attribute : attrs) {
            if(!ociMap.containsKey(attribute.getName()))
                throw new IllegalArgumentException((new StringBuilder()).append("Unknown attribute ").append(attribute.getName()).append(" is present").toString());
        }

    }

    public synchronized Uid generateUid() {
        return new Uid((new StringBuilder()).append(_uidIndex.getAndIncrement()).append(_cacheKey).toString());
    }

    public void delete(ObjectClass objClass, Uid uid, OperationOptions options) {
        if (!_map.containsKey(uid)) {
            throw new UnknownUidException();
        } else {
            _map.remove(uid);
            return;
        }
    }

    public FilterTranslator<String> createFilterTranslator(ObjectClass oclass, OperationOptions options) {
        return new DummyFilterTranslator();
    }

    public void executeQuery(ObjectClass oclass, String query, ResultsHandler handler, OperationOptions options) {
        String attrsToGet[] = options.getAttributesToGet();
        ConnectorObjectBuilder builder;
        for (Iterator<Map.Entry<Uid, Set<Attribute>>> iter = _map.entrySet().iterator(); iter.hasNext(); handler.handle(builder.build())) {
            java.util.Map.Entry<Uid, Set<Attribute>> entry = iter.next();
            builder = new ConnectorObjectBuilder();
            if(attrsToGet == null) {
                builder.addAttributes(entry.getValue());
            } else {
                Map<String, Attribute> map = AttributeUtil.toMap(entry.getValue());
                String attributes[] = attrsToGet;
                int length = attributes.length;
                for(int i = 0; i < length; i++) {
                    String attribute = attributes[i];
                    Attribute fetchedAttribute = map.get(attribute);
                    if (fetchedAttribute!=null) {
                        builder.addAttribute(new Attribute[] {
                            fetchedAttribute
                        });
                    }
                }
            }
            builder.setUid(entry.getKey());
        }

    }

    
    public Uid update(ObjectClass obj, Uid uid, Set<Attribute> attrs, OperationOptions options) {
        return update(obj, AttributeUtil.addUid(attrs, uid), options);
    }
    
    private Uid update(ObjectClass objclass, Set<Attribute> attrs, OperationOptions options) {
        Map<String, Attribute> attrMap = new HashMap<String, Attribute>(AttributeUtil.toMap(attrs));
        Uid uid = (Uid)attrMap.remove(Uid.NAME);
        if (uid == null)
            throw new RuntimeException("missing Uid");
        if (!_map.containsKey(uid)) {
            throw new UnknownUidException();
        } else {
            Name name = (Name)attrMap.remove(Name.NAME);
            Attribute currentPassword = attrMap.remove(OperationalAttributeInfos.CURRENT_PASSWORD);
            Set<Attribute> object = _map.get(uid);
            validateAttributes(objclass, attrs, false);
            object.addAll(attrMap.values());
            return uid;
        }
    }

    public Schema schema() {
        return staticSchema();
    }

    public static Schema staticSchema() {
        if (_schema != null)
            return _schema;
        try {
            SchemaBuilder schemaBuilder = new SchemaBuilder(DummyConnector.class);
            Set<AttributeInfo> attributes = new HashSet<AttributeInfo>();
            attributes.add(OperationalAttributeInfos.CURRENT_PASSWORD);
            attributes.add(OperationalAttributeInfos.DISABLE_DATE);
            attributes.add(OperationalAttributeInfos.ENABLE);
            attributes.add(OperationalAttributeInfos.ENABLE_DATE);
            attributes.add(OperationalAttributeInfos.LOCK_OUT);
            attributes.add(OperationalAttributeInfos.PASSWORD);
            attributes.add(OperationalAttributeInfos.PASSWORD_EXPIRATION_DATE);
            Method setters[] = getAttributeInfoBuilderSetters();
            AttributeInfoBuilder builder = new AttributeInfoBuilder();
            attributes.addAll(buildAttributeInfo(builder, setters));
            schemaBuilder.defineObjectClass(ObjectClass.ACCOUNT_NAME, attributes);
            _schema = schemaBuilder.build();
            return _schema;
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static List<AttributeInfo> buildAttributeInfo(AttributeInfoBuilder builder, Method methods[])
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        List<AttributeInfo> list = new LinkedList<AttributeInfo>();
        buildAttributeInfo(builder, methods, 0, "", list, _supportedTypes, new int[] {
            0
        });
        return list;
    }

    private static void buildAttributeInfo(AttributeInfoBuilder builder, Method methods[], int index, String name, List<AttributeInfo> list, Class<? extends Object> supportedTypes[], int typeIndex[])
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (index < methods.length) {
            methods[index].invoke(builder, new Object[] {
                Boolean.valueOf(true)
            });
            // We want to make arrays more obvious, so we will use Array in the name
            //
            String t = "_T";
            if (methods[index].getName().contains("MultiValue"))
            	t = "_Array";
            buildAttributeInfo(builder, methods, index + 1, (new StringBuilder()).append(name).append(t).toString(), list, supportedTypes, typeIndex);
            methods[index].invoke(builder, new Object[] {
                Boolean.valueOf(false)
            });
            buildAttributeInfo(builder, methods, index + 1, (new StringBuilder()).append(name).append("_F").toString(), list, supportedTypes, typeIndex);
        } else {
            Class<? extends Object> clazz = supportedTypes[typeIndex[0]];
            String prefix = clazz.getSimpleName();
            if (clazz.isArray())
                prefix = (new StringBuilder()).append(clazz.getComponentType().getSimpleName()).append("Array").toString();
            builder.setName((new StringBuilder()).append(prefix).append(name).toString().toUpperCase());
            builder.setType(clazz);
            try {
                AttributeInfo info = builder.build();
                // Exclude attributes marked required, but not creatable
                //
                boolean ignore1 = !info.isCreateable() && info.isRequired();
                boolean ignore2 = !info.isReadable() && info.isReturnedByDefault();
                boolean ignore3 = info.isRequired() && clazz.equals(byte[].class);
                if (!(ignore1 || ignore2 || ignore3)) {
                    list.add(info);
                    typeIndex[0]++;
                    if(typeIndex[0] == supportedTypes.length)
                        typeIndex[0] = 0;
                }
            } catch (IllegalArgumentException iae) {
                // If it was an illegal cobo, we can't use it.
            }
        }
    }

    private static Method[] getAttributeInfoBuilderSetters() {
        List<Method> setters = new LinkedList<Method>();
        Method methods[] = AttributeInfoBuilder.class.getMethods();
        for (Method method : methods) {
            if (!method.getName().startsWith("setReturned") && method.getName().startsWith("set") && 
                method.getParameterTypes().length == 1 && 
            	(method.getParameterTypes()[0] == Boolean.TYPE || method.getParameterTypes()[0] == Boolean.class))
                setters.add(method);
        }
        Method[] methodArray = (Method[])setters.toArray(new Method[0]);
        Arrays.sort(methodArray, new MethodComparator());
        System.out.println("Order of setters is:");
        for (Method method : methodArray)
            System.out.println("    "+method.getName());
        return methodArray;
    }
    
    static class MethodComparator implements Comparator<Method> {
        public int compare(Method o1, Method o2) {
            return o1.getName().compareTo(o2.getName());
        }
        
    }

    private static final AtomicInteger _uidIndex = new AtomicInteger();
    private static Schema _schema;
    private static final Class<? extends Object> _supportedTypes[] = FrameworkUtil.getAllSupportedAttributeTypes().toArray(new Class[0]);
    private static final ConcurrentMap<Integer, ConcurrentMap<Uid, Set<Attribute>>> _objectCacheMap =
            new ConcurrentHashMap<Integer, ConcurrentMap<Uid, Set<Attribute>>>();

    private ConcurrentMap<Uid, Set<Attribute>> _map = null;
    private DummyConfiguration _configuration= null;
    private Integer _cacheKey= null;
}
