/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/java/org/apache/commons/modeler/Registry.java,v 1.1 2002/04/30 20:58:52 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2002/04/30 20:58:52 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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


package org.apache.commons.modeler;


import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>Registry for MBean descriptor information.  This class implements
 * the Singleton design pattern.</p>
 *
 * <p><strong>WARNING</strong> - It is expected that this registry will be
 * initially populated at startup time, and then provide read-only access
 * in a multithreaded environment.  Therefore, no instance operations are
 * synchronized.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2002/04/30 20:58:52 $
 */

public final class Registry {


    // ----------------------------------------------------------- Constructors


    /**
     * Private constructor to require use of the factory create method.
     */
    private Registry() {

        super();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of ManagedBean instances for the beans this registry
     * knows about, keyed by name.
     */
    private HashMap beans = new HashMap();


    /**
     * The Log instance to which we will write our log messages.
     */
    private static Log log = LogFactory.getLog(Registry.class);


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new bean to the set of beans known to this registry.
     *
     * @param bean The managed bean to be added
     */
    public void addManagedBean(ManagedBean bean) {

        beans.put(bean.getName(), bean);

    }


    /**
     * Find and return the managed bean definition for the specified
     * bean name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the managed bean to be returned
     */
    public ManagedBean findManagedBean(String name) {

        return ((ManagedBean) beans.get(name));

    }


    /**
     * Return the set of bean names for all managed beans known to
     * this registry.
     */
    public String[] findManagedBeans() {

        return ((String[]) beans.keySet().toArray(new String[0]));

    }


    /**
     * Return the set of bean names for all managed beans known to
     * this registry that belong to the specified group.
     *
     * @param group Name of the group of interest, or <code>null</code>
     *  to select beans that do <em>not</em> belong to a group
     */
    public String[] findManagedBeans(String group) {

        ArrayList results = new ArrayList();
        Iterator items = beans.values().iterator();
        while (items.hasNext()) {
            ManagedBean item = (ManagedBean) items.next();
            if ((group == null) && (item.getGroup() == null)) {
                results.add(item.getName());
            } else if (group.equals(item.getGroup())) {
                results.add(item.getName());
            }
        }
        String values[] = new String[results.size()];
        return ((String[]) results.toArray(values));

    }


    /**
     * Remove an existing bean from the set of beans known to this registry.
     *
     * @param bean The managed bean to be removed
     */
    public void removeManagedBean(ManagedBean bean) {

        beans.remove(bean.getName());

    }


    // ------------------------------------------------------- Static Variables


    /**
     * The registry instance created by our factory method the first time
     * it is called.
     */
    private static Registry registry = null;


    /**
     * The <code>MBeanServer</code> instance that we will use to register
     * management beans.
     */
    private static MBeanServer server = null;


    // --------------------------------------------------------- Static Methods


    /**
     * Factory method to create (if necessary) and return our
     * <code>Registry</code> instance.
     */
    public synchronized static Registry getRegistry() {

        if (registry == null) {
            log.info("Creating new Registry instance");
            registry = new Registry();
        }
        return (registry);

    }


    /**
     * Factory method to create (if necessary) and return our
     * <code>MBeanServer</code> instance.
     */
    public synchronized static MBeanServer getServer() {

        if (server == null) {
            log.info("Creating MBeanServer");
            server = MBeanServerFactory.createMBeanServer();
        }
        return (server);

    }


    /**
     * Load the registry from the XML input found in the specified input
     * stream.
     *
     * @param stream InputStream containing the registry configuration
     *  information
     *
     * @exception Exception if any parsing or processing error occurs
     */
    public static void loadRegistry(InputStream stream)
        throws Exception {

        log.info("Loading registry information");

        // Create a digester to use for parsing
        Registry registry = getRegistry();
        Digester digester = new Digester();
        digester.setNamespaceAware(false);
        digester.setValidating(true);
        URL url = registry.getClass().getResource
            ("/org/apache/commons/modeler/mbeans-descriptors.dtd");
        digester.register
            ("-//Apache Software Foundation//DTD Model MBeans Configuration File",
             url.toString());
             
        // Push our registry object onto the stack
        digester.push(registry);

        // Configure the parsing rules
        digester.addObjectCreate
            ("mbeans-descriptors/mbean",
             "org.apache.commons.modeler.ManagedBean");
        digester.addSetProperties
            ("mbeans-descriptors/mbean");
        digester.addSetNext
            ("mbeans-descriptors/mbean",
             "addManagedBean",
             "org.apache.commons.modeler.ManagedBean");

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
            ("mbeans-descriptors/mbean/constructor",
             "org.apache.commons.modeler.ConstructorInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/constructor");
        digester.addSetNext
            ("mbeans-descriptors/mbean/constructor",
             "addConstructor",
             "org.apache.commons.modeler.ConstructorInfo");

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
            ("mbeans-descriptors/mbean/notification",
             "org.apache.commons.modeler.NotificationInfo");
        digester.addSetProperties
            ("mbeans-descriptors/mbean/notification");
        digester.addSetNext
            ("mbeans-descriptors/mbean/notification",
             "addNotification",
             "org.apache.commons.modeler.NotificationInfo");
        digester.addCallMethod
            ("mbeans-descriptors/mbean/notification/notification-type",
             "addNotificationType", 0);

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

    }


    /**
     * Set the <code>MBeanServer</code> to be utilized for our
     * registered management beans.
     *
     * @param mbeanServer The new <code>MBeanServer</code> instance
     */
    public static void setServer(MBeanServer mbeanServer) {

        server = mbeanServer;

    }


}
