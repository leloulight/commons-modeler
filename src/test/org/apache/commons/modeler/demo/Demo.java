/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @version $Revision: 1.4 $ $Date: 2004/02/28 13:31:22 $
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
