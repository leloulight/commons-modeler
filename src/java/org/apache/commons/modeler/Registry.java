/*
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


import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;

import javax.management.*;
import javax.management.modelmbean.ModelMBean;

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
 * @author Costin Manolache
 * @version $Revision: 1.6 $ $Date: 2002/11/02 06:52:12 $
 */
public final class Registry extends BaseRegistry {


    // ----------------------------------------------------------- Constructors


    /**
     * Constructor to require use of the factory create method.
     *  It is called from BaseRegistry
     */
     protected Registry() {
        
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
            log.debug("Creating new Registry instance");
            registry = new Registry();
        }
        return (registry);

    }

    // XXX This should be decoupled - Registry should only deal with
    // type info, and it may be nice to not depend directly on JMX.
    
    /**
     * Factory method to create (if necessary) and return our
     * <code>MBeanServer</code> instance.
     */
    public synchronized static MBeanServer getServer() {
        long t1=System.currentTimeMillis();

        if (server == null) {
            if( MBeanServerFactory.findMBeanServer(null).size() > 0 ) {
                server=(MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
                log.debug("Using existing MBeanServer " + (System.currentTimeMillis() - t1 ));
            } else {
                server=MBeanServerFactory.createMBeanServer();
                log.debug("Creating MBeanServer"+ (System.currentTimeMillis() - t1 ));
            }
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
     * @deprecated use normal class method instead
     */
    public static void loadRegistry(InputStream stream) throws Exception {
        Registry registry = getRegistry();
        registry.loadDescriptors( stream, "modeler" );
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
    public void loadDescriptors(InputStream stream, String type)
        throws Exception 
    {
        long t1=System.currentTimeMillis();

        // Create a digester to use for parsing
        Registry registry = this;
        
        Digester digester = new Digester();
        digester.setNamespaceAware(false);
        digester.setValidating(false); 
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
        if( t2-t1 > 500 )
            log.info("Loaded registry information " + ( t2 - t1 ) + " ms");
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


    // ---------------------- BaseRegistry overrides --------------------------
    public Object getMBeanServer() {
        return getServer();
    }


    public void registerComponent(Object bean, String domain, String type,
                                  String name)
           throws Exception
    {
        ManagedBean managed = registry.findManagedBean(type);

        ModelMBean mbean = managed.createMBean(bean);

        getServer().registerMBean( mbean, new ObjectName( domain + ": type="
                + type + " ; " + name ));
    }

    public void registerClass(Class beanClass, String domain, String className,
                                  String type, Object source)
    {
    }

    public void unregisterMBean( String name ) {
        try {
            ObjectName oname=new ObjectName( name );

            getServer().unregisterMBean( oname );
        } catch( Throwable t ) {
            log.error( "Error unregistering mbean ", t );
        }
      }


    public String registerMBean( String domain, String name ) {
        try {
            // XXX use aliases, suffix only, proxy.getName(), etc
            String fullName=domain + ": " +  name;
            ObjectName oname=new ObjectName( fullName );

            if(  getServer().isRegistered( oname )) {
                log.info("Unregistering " + oname );
                getServer().unregisterMBean( oname );
            }
            getServer().registerMBean( this, oname );
            return fullName;
        } catch( Throwable t ) {
            log.error( "Error creating mbean ", t );
            return null;
        }
    }

    public static String MODELER_MANIFEST="/META-INF/modeler-mbeans.xml";

    /** Discover all META-INF/modeler.xml files in classpath and register the components
     */
    public void loadDescriptors(ClassLoader cl, String type) {
        try {
            Enumeration en=cl.getResources(MODELER_MANIFEST);
            while( en.hasMoreElements() ) {
                URL url=(URL)en.nextElement();
                InputStream is=url.openStream();
                if( log.isDebugEnabled()) log.debug("Loading " + url);
                loadDescriptors(is, "modeler" );
            }
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    // ------------ Implementation for non-declared introspection classes
    

    // createMBean == registerClass + registerMBean 

    private boolean supportedType( Class ret ) {
        return ret == String.class ||
            ret == Integer.class ||
            ret == Integer.TYPE ||
            ret == Long.class ||
            ret == Long.TYPE ||
            ret == java.io.File.class ||
            ret == Boolean.class ||
            ret == Boolean.TYPE 
            ; 
    }

    /** Process the methods and extract 'attributes', methods, etc 
      *
      */
    private void initMethods(Class realClass, 
                             Method methods[], 
                             Hashtable attMap, Hashtable getAttMap,
                             Hashtable setAttMap, Hashtable invokeAttMap) 
    {
        for (int j = 0; j < methods.length; ++j) {
            String name=methods[j].getName();
            
            if( name.startsWith( "get" ) ) {
                if( methods[j].getParameterTypes().length != 0 ) {
                    continue;
                }
                if( ! Modifier.isPublic( methods[j].getModifiers() ) ) {
                    //log.debug("not public " + methods[j] );
                    continue;
                }
                Class ret=methods[j].getReturnType();
                if( ! supportedType( ret ) ) {
                    if( log.isDebugEnabled() )
                        log.debug("Unsupported " + ret );
                    continue;
                }
                name=unCapitalize( name.substring(3));

                getAttMap.put( name, methods[j] );
                // just a marker, we don't use the value 
                attMap.put( name, methods[j] );
            } else if( name.startsWith( "is" ) ) {
                // not used in our code. Add later

            } else if( name.startsWith( "set" ) ) {
                Class params[]=methods[j].getParameterTypes();
                if( params.length != 1 ) {
                    continue;
                }
                if( ! Modifier.isPublic( methods[j].getModifiers() ) )
                    continue;
                Class ret=params[0];
                if( ! supportedType( ret ) ) {
                    continue;
                }
                name=unCapitalize( name.substring(3));
                setAttMap.put( name, methods[j] );
                attMap.put( name, methods[j] );
            } else {
                if( methods[j].getParameterTypes().length != 0 ) {
                    continue;
                }
                if( methods[j].getDeclaringClass() == Object.class )
                    continue;
                if( ! Modifier.isPublic( methods[j].getModifiers() ) )
                    continue;
                invokeAttMap.put( name, methods[j]);
            }
        }
    }

    /**
     * @todo Find if the 'className' is the name of the MBean or
     *       the real class ( I suppose first )
     * @todo Read (optional) descriptions from a .properties, generated
     *       from source
     * @todo Deal with constructors
     *       
     */
    private ManagedBean createManagedBean(String domain, Object real, String type) {
        ManagedBean mbean= new ManagedBean();
        
        Method methods[]=null;
        
        Hashtable attMap=new Hashtable();
        // key: attribute val: getter method
        Hashtable getAttMap=new Hashtable();
        // key: attribute val: setter method
        Hashtable setAttMap=new Hashtable();
        // key: operation val: invoke method
        Hashtable invokeAttMap=new Hashtable();        
        
        Class realClass=real.getClass();
        methods = realClass.getMethods();

        initMethods(realClass, methods, attMap, getAttMap, setAttMap, invokeAttMap );
        
        if( type==null) type=super.generateSeqName(domain, realClass);

        try {
            
            Enumeration en=attMap.keys();
            while( en.hasMoreElements() ) {
                String name=(String)en.nextElement();
                AttributeInfo ai=new AttributeInfo();
                ai.setName( name );
                ai.setGetMethod( ((Method)getAttMap.get(name)).getName());
                ai.setSetMethod( ((Method)setAttMap.get(name)).getName());
                ai.setDescription("Introspected attribute " + name );
                
                mbean.addAttribute(ai);
            }
            
            en=invokeAttMap.keys();
            while( en.hasMoreElements() ) {
                String name=(String)en.nextElement();
                Method m=(Method)invokeAttMap.get(name);
                if( m!=null && name != null ) {
                    OperationInfo op=new OperationInfo();
                    op.setName(name);
                    op.setReturnType(m.getReturnType().getName());
                    Class parms[]=m.getParameterTypes();
                    for(int i=0; i<parms.length; i++ ) {
                        ParameterInfo pi=new ParameterInfo();
                        pi.setType(parms[i].getName());
                        op.addParameter(pi);
                    }
                    mbean.addOperation(op);
                } else {
                    log.error("Null arg " + name + " " + m );
                }
            }

            if( log.isDebugEnabled())
                log.debug("Setting name: " + type );
            mbean.setName( type );

            return mbean;
        } catch( Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    
    // -------------------- Utils --------------------

    private static String unCapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

}
 