/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/test/org/apache/commons/modeler/demo/Demo.java,v 1.3 2003/10/09 21:39:05 rdonkin Exp $
 * $Revision: 1.3 $
 * $Date: 2003/10/09 21:39:05 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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


package org.apache.commons.modeler.demo;


import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;

import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;


/**
 * <p>Demonstration program for the Modeller package.  Instantiates a set
 * of simple managed objects, and a set of corresponding MBeans, then
 * manipulates the objects through the MBean interfaces.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2003/10/09 21:39:05 $
 */

public class Demo implements NotificationListener {


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------- Instance Methods


    /**
     * Handle the notification of a JMX event.
     *
     * @param notification The event that has occurred
     * @param handback The handback object for this event
     */
    public void handleNotification(Notification notification,
                                   Object handback) {

        System.out.println("NOTIFICATION=" + notification +
                           ", HANDBACK=" + handback);

    }


    // ------------------------------------------------------- Static Variables


    /**
     * The attribute notification listener.
     */
    private static Demo demo = null;


    /**
     * The configuration information registry for our managed beans.
     */
    private static Registry registry = null;


    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer server = null;


    /**
     * The managed object tree.
     */
    private static Server tree = null;


    // ----------------------------------------------------------- Main Program


    /**
     * The main program for this demonstration.
     *
     * @param args Command line arguments
     */
    public static void main(String arg[]) {

        createRegistry();
        createServer();
        createTree();
        createMBeans();
        listMBeans();
        dumpServer();
        updateServer();

    }


    // -------------------------------------------------------- Support Methods


    /**
     * Create the MBeans that correspond to every node of our tree.
     */
    private static void createMBeans() {

        try {

            // NOTE:  JMXRI crashes on server.setAttribute() unless there has
            // been an attribute change listener registered at some point
            // on every ModelMBean. :-(
            // NOTE:  Despite the documentation, you cannot register an
            // attribute change listener for all attributes.  :-(
            Demo demo = new Demo();

            System.out.println("Creating MBeans ...");

            // Create the MBean for the top-level Server object
            String domain = server.getDefaultDomain();
            ManagedBean managed = registry.findManagedBean("StandardServer");
            ModelMBean mm = managed.createMBean(tree);
            mm.addAttributeChangeNotificationListener(demo, "shutdown", tree);
            mm.addAttributeChangeNotificationListener(demo, "port", tree);
            server.registerMBean(mm, createName(domain, tree));

            // Create the MBean for each associated Service and friendes
            Service services[] = tree.findServices();
            for (int i = 0; i < services.length; i++) {

                // The MBean for the Service itself
                managed = registry.findManagedBean("StandardService");
                mm = managed.createMBean(services[i]);
                mm.addAttributeChangeNotificationListener(demo, "name",
                                                          services[i]);
                server.registerMBean(mm, createName(domain, services[i]));

                // The MBean for the corresponding Engine
                managed = registry.findManagedBean("StandardEngine");
                Engine container = (Engine) services[i].getContainer();
                mm = managed.createMBean(container);
                mm.addAttributeChangeNotificationListener(demo, "name",
                                                          container);
                server.registerMBean(mm, createName(domain, container));

                // The MBeans for the corresponding Connectors
                managed = registry.findManagedBean("HttpConnector");
                Connector connectors[] = services[i].findConnectors();
                for (int j = 0; j < connectors.length; j++) {
                    mm = managed.createMBean(connectors[j]);
                    mm.addAttributeChangeNotificationListener(demo, "port",
                                                              connectors[j]);
                    server.registerMBean
                        (mm, createName(domain, connectors[j]));
                }

            }

        } catch (MBeanException t) {

            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);

        } catch (Throwable t) {

            System.out.println(t.getMessage());
            t.printStackTrace(System.out);

        }

    }


    /**
     * Create an <code>ObjectName</code> for this object.
     *
     * @param domain Domain in which this name is to be created
     * @param connector The Connector to be named
     */
    private static ObjectName createName(String domain, Connector connector) {

        ObjectName name = null;
        try {
            name = new ObjectName(domain + ":type=Connector,port=" +
                                  connector.getPort() + ",service=" +
                                  connector.getService().getName());
        } catch (Throwable t) {
            System.out.println("Creating name for " + connector);
            t.printStackTrace(System.out);
            System.exit(1);
        }
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this object.
     *
     * @param domain Domain in which this name is to be created
     * @param engine The Engine to be named
     */
    private static ObjectName createName(String domain, Engine engine) {

        ObjectName name = null;
        try {
            name = new ObjectName(domain + ":type=Engine,service=" +
                                  engine.getService().getName());
        } catch (Throwable t) {
            System.out.println("Creating name for " + engine);
            t.printStackTrace(System.out);
            System.exit(1);
        }
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this object.
     *
     * @param domain Domain in which this name is to be created
     * @param server The Server to be named
     */
    private static ObjectName createName(String domain, Server server) {

        ObjectName name = null;
        try {
            name = new ObjectName(domain + ":type=Server");
        } catch (Throwable t) {
            System.out.println("Creating name for " + server);
            t.printStackTrace(System.out);
            System.exit(1);
        }
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this object.
     *
     * @param domain Domain in which this name is to be created
     * @param service The Service to be named
     */
    private static ObjectName createName(String domain, Service service) {

        ObjectName name = null;
        try {
            name = new ObjectName(domain + ":type=Service,name=" +
                                  service.getName());
        } catch (Throwable t) {
            System.out.println("Creating name for " + server);
            t.printStackTrace(System.out);
            System.exit(1);
        }
        return (name);

    }


    /**
     * Create and configure the registry of managed objects.
     */
    private static void createRegistry() {

        System.out.println("Create configuration registry ...");
        try {
            URL url = Demo.class.getResource
                ("/org/apache/commons/modeler/demo/mbeans-descriptors.xml");
            InputStream stream = url.openStream();
            Registry.loadRegistry(stream);
            stream.close();
            registry = Registry.getRegistry();
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit(1);
        }

    }


    /**
     * Create the <code>MBeanServer</code> with which we will be
     * registering our <code>ModelMBean</code> implementations.
     */
    private static void createServer() {

        System.out.println("Creating MBeanServer ...");
        try {
            //            System.setProperty("LEVEL_TRACE", "true");
            server = MBeanServerFactory.createMBeanServer();
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit(1);
        }

    }


    /**
     * Create the tree of managed objects.
     */
    private static void createTree() {

        System.out.println("Create managed object tree ...");
        tree = new Server(8005, "SHUTDOWN");

        Service service = new Service("Standard Service", tree);
        tree.addService(service);

        Engine engine = new Engine("Standard Engine", "localhost", service);
        service.setContainer(engine);

        Connector conn1 = new Connector(8080, "http", false,
                                        service, engine);
        service.addConnector(conn1);
        Connector conn2 = new Connector(8443, "https", true,
                                        service, engine);
        service.addConnector(conn2);

    }


    /**
     * Dump known information about the "Server" we are managing.
     */
    private static void dumpServer() {

        try {

            System.out.println("Dump ModelMBeanInfo for Server:");
            ObjectName name =
                new ObjectName(server.getDefaultDomain() + ":type=Server");

            // Return static ModelMBeanInfo information
            ModelMBeanInfo info = (ModelMBeanInfo) server.getMBeanInfo(name);
            System.out.println("  className=" + info.getClassName());
            System.out.println("  description=" + info.getDescription());
            System.out.println("  mbeanDescriptor=" + info.getMBeanDescriptor());
            MBeanAttributeInfo attrs[] = info.getAttributes();
            for (int i = 0; i < attrs.length; i++)
                System.out.println("  AttributeInfo=" + attrs[i]);
            MBeanConstructorInfo consts[] = info.getConstructors();
            for (int i = 0; i < consts.length; i++)
                System.out.println("  ConstructorInfo=" + consts[i]);
            Descriptor descs[] = info.getDescriptors(null);
            for (int i = 0; i < descs.length; i++)
                System.out.println("  Descriptor=" + descs[i]);
            MBeanNotificationInfo notifs[] = info.getNotifications();
            for (int i = 0; i < notifs.length; i++)
                System.out.println("  Notification=" + notifs[i]);
            MBeanOperationInfo opers[] = info.getOperations();
            for (int i = 0; i < opers.length; i++)
                System.out.println("  Operation=" + opers[i]);

        } catch (MBeanException t) {

            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);

        } catch (Throwable t) {

            System.out.println(t.getMessage());
            t.printStackTrace(System.out);

        }


    }


    /**
     * List information about all registered MBeans.
     */
    private static void listMBeans() {

        System.out.println("There are " + server.getMBeanCount().intValue() +
                           " registered MBeans");
        Iterator instances = server.queryMBeans(null, null).iterator();
        while (instances.hasNext()) {
            ObjectInstance instance = (ObjectInstance) instances.next();
            System.out.println("  objectName=" + instance.getObjectName() +
                               ", className=" + instance.getClassName());
        }


    }


    /**
     * Test updating an attribute through the JMX interfaces.
     */
    private static void updateServer() {

        try {

            System.out.println("===========================================");

            System.out.println("Test updating Server properties ...");
            ObjectName name =
                new ObjectName(server.getDefaultDomain() + ":type=Server");

            System.out.println("  Retrieving current value of 'shutdown'");
            String value = (String) server.getAttribute(name, "shutdown");
            if (!"SHUTDOWN".equals(value))
                throw new IllegalStateException("Current shutdown value is '" +
                                                value + "'");

            System.out.println("  Setting new value of 'shutdown'");
            server.setAttribute(name,
                                new Attribute("shutdown", "NEW VALUE"));

            System.out.println("  Checking new value of 'shutdown'");
            value = (String) server.getAttribute(name, "shutdown");
            if (!"NEW VALUE".equals(value))
                throw new IllegalStateException("New shutdown value is '" +
                                                value + "'");

            System.out.println("===========================================");

            System.out.println("Test updating Server properties ...");

            System.out.println("  Retrieving current value of 'port'");
            Integer ivalue = (Integer) server.getAttribute(name, "port");
            if (ivalue.intValue() != 8005)
                throw new IllegalStateException("Current port value is '" +
                                                ivalue + "'");

            System.out.println("  Setting new value of 'port'");
            server.setAttribute(name,
                                new Attribute("port", new Integer(8765)));
            /*
            server.invoke(name, "setPort",
                          new Object[] { new java.lang.Integer(8765) },
                          new String[] { "int" });

            */
            System.out.println("  Checking new value of 'port'");
            ivalue = (Integer) server.getAttribute(name, "port");
            if (ivalue.intValue() != 8765)
                throw new IllegalStateException("New port value is '" +
                                                ivalue + "'");

        } catch (MBeanException t) {

            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);

        } catch (Throwable t) {

            System.out.println(t.getMessage());
            t.printStackTrace(System.out);

        }


    }


}
