/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
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
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package org.apache.commons.modeler.modules;

import org.apache.commons.digester.Digester;
import org.apache.commons.modeler.Registry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

public class MbeansDescriptorsDigesterSource extends ModelerSource
{
    private static Log log =
            LogFactory.getLog(MbeansDescriptorsDigesterSource.class);

    Registry registry;
    String location;
    String type;
    Object source;
    List mbeans=new ArrayList();

    public void setRegistry(Registry reg) {
        this.registry=reg;
    }

    public void setLocation( String loc ) {
        this.location=loc;
    }

    /** Used if a single component is loaded
     *
     * @param type
     */
    public void setType( String type ) {
       this.type=type;
    }

    public void setSource( Object source ) {
        this.source=source;
    }

    public List loadDescriptors( Registry registry, String location,
                                 String type, Object source)
            throws Exception
    {
        setRegistry(registry);
        setLocation(location);
        setType(type);
        setSource(source);
        execute();
        return mbeans;
    }

    public void execute() throws Exception {
        if( registry==null ) registry=Registry.getRegistry();

        InputStream stream=(InputStream)source;

        long t1=System.currentTimeMillis();

        Digester digester = new Digester();
        digester.setNamespaceAware(false);
        digester.setValidating(false);
        URL url = registry.getClass().getResource
                ("/org/apache/commons/modeler/mbeans-descriptors.dtd");
        digester.register
                ("-//Apache Software Foundation//DTD Model MBeans Configuration File",
                        url.toString());

        // Push our registry object onto the stack
        digester.push(mbeans);

        // Configure the parsing rules
        digester.addObjectCreate
                ("mbeans-descriptors/mbean",
                        "org.apache.commons.modeler.ManagedBean");
        digester.addSetProperties
                ("mbeans-descriptors/mbean");
        digester.addSetNext
                ("mbeans-descriptors/mbean",
                        "add",
                        "java.lang.Object");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/attribute",
                        "org.apache.commons.modeler.AttributeInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/attribute");
        digester.addSetNext
                ("mbeans-descriptors/mbean/attribute",
                        "addAttribute",
                        "org.apache.commons.modeler.AttributeInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/attribute/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/attribute/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/attribute/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/constructor",
                        "org.apache.commons.modeler.ConstructorInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/constructor");
        digester.addSetNext
                ("mbeans-descriptors/mbean/constructor",
                        "addConstructor",
                        "org.apache.commons.modeler.ConstructorInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/constructor/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/constructor/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/constructor/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/constructor/parameter",
                        "org.apache.commons.modeler.ParameterInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/constructor/parameter");
        digester.addSetNext
                ("mbeans-descriptors/mbean/constructor/parameter",
                        "addParameter",
                        "org.apache.commons.modeler.ParameterInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/notification",
                        "org.apache.commons.modeler.NotificationInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/notification");
        digester.addSetNext
                ("mbeans-descriptors/mbean/notification",
                        "addNotification",
                        "org.apache.commons.modeler.NotificationInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/notification/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/notification/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/notification/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addCallMethod
                ("mbeans-descriptors/mbean/notification/notification-type",
                        "addNotifType", 0);

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/operation",
                        "org.apache.commons.modeler.OperationInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/operation");
        digester.addSetNext
                ("mbeans-descriptors/mbean/operation",
                        "addOperation",
                        "org.apache.commons.modeler.OperationInfo");

        digester.addObjectCreate
            ("mbeans-descriptors/mbean/operation/descriptor/field",
             "org.apache.commons.modeler.FieldInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/operation/descriptor/field");
        digester.addSetNext
            ("mbeans-descriptors/mbean/operation/descriptor/field",
             "addField",
             "org.apache.commons.modeler.FieldInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/operation/parameter",
                        "org.apache.commons.modeler.ParameterInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/operation/parameter");
        digester.addSetNext
                ("mbeans-descriptors/mbean/operation/parameter",
                        "addParameter",
                        "org.apache.commons.modeler.ParameterInfo");

        // Process the input file to configure our registry
        try {
            digester.parse(stream);
        } catch (Exception e) {
            log.error("Error digesting Registry data", e);
            throw e;
        }
        long t2=System.currentTimeMillis();
//        if( t2-t1 > 500 )
        log.info("Loaded registry information (digester) " + ( t2 - t1 ) + " ms");
    }
}
