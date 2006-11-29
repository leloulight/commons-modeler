/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.modeler.ant;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.Registry;
import org.apache.tools.ant.Task;

/**
 * Set mbean properties.
 *
 */
public class JmxSet extends Task {
    private static Log log = LogFactory.getLog(JmxSet.class);

    String attribute;
    String value;
    String valueRef;
    Object objValue;
    String objectName;
    ObjectName oname;
    String type;


    public JmxSet() {
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setName( String name ) {
        this.attribute=name;
    }

    public String getName() {
        return attribute;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void addText( String value ) {
        this.value=value;
    }

    public void setValueRef(String valueRef) {
        this.valueRef = valueRef;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setObjValue(Object objValue) {
        this.objValue = objValue;
    }

    public void setObjectName(String name) {
        this.objectName = name;
    }

    public void setObjectName( ObjectName oname ) {
        this.oname=oname;
    }

    public void execute() {
        try {
            Registry registry=Registry.getRegistry();
            MBeanServer server=registry.getMBeanServer();

            if( oname==null )
                oname=new ObjectName(objectName);
            if( type==null ) {
                type=registry.getType(oname, attribute);
                if( log.isDebugEnabled())
                    log.debug("Discovered type " + type);
            }

            // XXX convert value, use meta data to find type
             if( objValue==null && valueRef != null ) {
                 objValue=project.getReference(valueRef);
             }
             if( objValue==null ) {
                 objValue=registry.convertValue(type, value);

             }
            if( log.isDebugEnabled())
                log.debug("Setting " + oname + " " + attribute + " " +
                        objValue);
            server.setAttribute(oname, new Attribute(attribute, objValue));

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
