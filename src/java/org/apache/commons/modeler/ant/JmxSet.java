/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.commons.modeler.ant;

import org.apache.tools.ant.*;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.management.*;
import javax.management.MBeanServer;
import javax.management.ObjectName;

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
    String type;


    public JmxSet() {
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
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


    public void execute() {
        try {
            MBeanServer server=(MBeanServer)project.getReference("jmx.server");

            if (server == null) {
                if( MBeanServerFactory.findMBeanServer(null).size() > 0 ) {
                    server=(MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
                } else {
                    if( log.isDebugEnabled())
                        log.debug("Creating mbean server");
                    server=MBeanServerFactory.createMBeanServer();
                }
                project.addReference("jmx.server", server);
            }

            ObjectName oname=new ObjectName(objectName);

            // XXX convert value, use meta data to find type
            if( objValue==null && valueRef != null ) {
                 objValue=project.getReference(valueRef);
             }
             if( objValue==null ) {
                 if( type==null) {// string is default
                     objValue=value;
                 } else if( "ObjectName".equals( type )) {
                     if( log.isTraceEnabled())
                        log.trace("Convert to ObjectName " + value);
                     objValue=new ObjectName( value );
                 } else if( "int".equals( type )) {
                     objValue=new Integer( value );
                 }

            }
            if( log.isDebugEnabled())
                log.debug("Setting " + oname + " " + attribute + " " +
                        objValue );
            server.setAttribute(oname, new Attribute(attribute, objValue));

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}