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


package org.apache.commons.modeler;


import java.io.*;
import java.net.*;
import java.util.*;

import javax.management.*;
import javax.management.modelmbean.ModelMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.modules.ModelerSource;

/**
 * This is the main entry point into modeler. It provides methods to manipulate
 * the metadata used by the ModelMBeans and helpers to simplify the use.
 *
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
 */
public final class Registry {

    /**
     * The Log instance to which we will write our log messages.
     */
    private static Log log = LogFactory.getLog(Registry.class);

    /** Will be used to isolate different apps and enhance security
     */
    private static HashMap perLoaderRegistries=null;

    /**
     * The registry instance created by our factory method the first time
     * it is called.
     */
    private static Registry registry = null;

    /**
     * The <code>MBeanServer</code> instance that we will use to register
     * management beans.
     */
    private MBeanServer server = null;

    /**
     * The set of ManagedBean instances for the beans this registry
     * knows about, keyed by name.
     */
    private HashMap descriptors = new HashMap();

    /** List of managed byeans, keyed by class name
     */
    private HashMap descriptorsByClass = new HashMap();

    // ----------------------------------------------------------- Constructors


    /**
     * Constructor to require use of the factory create method.
     */
     protected Registry() {
        super();
    }

    /**
     * Factory method to create (if necessary) and return our
     * <code>Registry</code> instance.
     *
     * Use this method to obtain a Registry - all other static methods
     * are deprecated and shouldn't be used.
     *
     * The current version uses a static - future versions could use
     * the thread class loader.
     *
     * @since 1.1
     */
    public synchronized static Registry getRegistry() {
        /* Experimental */
        if( perLoaderRegistries!=null ) {
            ClassLoader cl=Thread.currentThread().getContextClassLoader();
            if( cl != null ) {
                Registry localRegistry=(Registry)perLoaderRegistries.get(cl);
                if( localRegistry != null ) return localRegistry;
                localRegistry=new Registry();
                perLoaderRegistries.put( cl, localRegistry );
                return localRegistry;
            }
        }
        if (registry == null) {
            registry = new Registry();
        }
        return (registry);
    }

    /** Experimental. Will allow containers to isolate apps
     * @param enable
     */
    public void setUseContextClassLoader( boolean enable ) {
        if( enable ) {
            perLoaderRegistries=new HashMap();
        }
    }

    /** Called by a registry or by the container to unload a loader
     * @param loader
     */
    public void unregisterRegistry(ClassLoader loader ) {
        // XXX Cleanup ?
        perLoaderRegistries.remove(loader);
    }

    // -------------------- Metadata   --------------------

    /**
     * Add a new bean metadata to the set of beans known to this registry.
     *
     * @param bean The managed bean to be added
     * @since 1.0
     */
    public void addManagedBean(ManagedBean bean) {
        // XXX Use group + name
        descriptors.put(bean.getName(), bean);
        if( bean.getType() != null ) {
            descriptorsByClass.put( bean.getType(), bean );
        }
    }


    /**
     * Find and return the managed bean definition for the specified
     * bean name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the managed bean to be returned. Since 1.1, both
     *   short names or the full name of the class can be used.
     * @since 1.0
     */
    public ManagedBean findManagedBean(String name) {
        // XXX Group ?? Use Group + Type
        ManagedBean mb=((ManagedBean) descriptors.get(name));
        if( mb==null )
            mb=(ManagedBean)descriptorsByClass.get(name);
        return mb;
    }

    public ManagedBean findManagedBean(Class bean, String type)
        throws Exception
    {
        if( type==null )
            type=bean.getName();
        ManagedBean managed = registry.findManagedBean(type);

        // Search for a descriptor in the same package
        if( managed==null ) {
            // check package and parent packages
            if( log.isDebugEnabled() )
                log.debug( "Looking for descriptor ");

            findDescriptor( bean );

            managed=findManagedBean(type);
            // TODO: check super-class
        }


        if( log.isDebugEnabled() )
            log.debug( "Managed= "+ managed);

        // Still not found - use introspection
        if( managed==null ) {
            // introspection
            loadDescriptors("MbeansDescriptorsIntrospectionSource",
                    bean, type);

            managed=findManagedBean(type);
            managed.setName( type );
            addManagedBean(managed);
        }
        return managed;
    }
    /**
     * Return the set of bean names for all managed beans known to
     * this registry.
     *
     * @since 1.0
     */
    public String[] findManagedBeans() {
        return ((String[]) descriptors.keySet().toArray(new String[0]));
    }


    /**
     * Return the set of bean names for all managed beans known to
     * this registry that belong to the specified group.
     *
     * @param group Name of the group of interest, or <code>null</code>
     *  to select beans that do <em>not</em> belong to a group
     * @since 1.0
     */
    public String[] findManagedBeans(String group) {

        ArrayList results = new ArrayList();
        Iterator items = descriptors.values().iterator();
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
     * @since 1.0
     */
    public void removeManagedBean(ManagedBean bean) {
       // TODO: change this to use group/name
        descriptors.remove(bean.getName());
        descriptorsByClass.remove( bean.getType());
    }

    // -------------------- Access the mbean server  --------------------

    /**
     * Factory method to create (if necessary) and return our
     * <code>MBeanServer</code> instance.
     *
     * @since 1.0
     * @deprecated Use the instance method
     */
    public static MBeanServer getServer() {
        return Registry.getRegistry().getMBeanServer();
    }

    /**
     * Set the <code>MBeanServer</code> to be utilized for our
     * registered management beans.
     *
     * @param mbeanServer The new <code>MBeanServer</code> instance
     * @since 1.0
     * @deprecated Use the instance method
     */
    public static void setServer(MBeanServer mbeanServer) {
        Registry.getRegistry().setServer(mbeanServer);
    }

    /**
     * Set the <code>MBeanServer</code> to be utilized for our
     * registered management beans.
     *
     * @param server The new <code>MBeanServer</code> instance
     * @since 1.1
     */
    public void setMBeanServer( MBeanServer server ) {
        this.server=server;
    }

    /**
     * Factory method to create (if necessary) and return our
     * <code>MBeanServer</code> instance.
     *
     * @since 1.1
     */
    public synchronized MBeanServer getMBeanServer() {
        long t1=System.currentTimeMillis();

        if (server == null) {
            if( MBeanServerFactory.findMBeanServer(null).size() > 0 ) {
                server=(MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
                if( log.isDebugEnabled() )
                    log.debug("Using existing MBeanServer " + (System.currentTimeMillis() - t1 ));
            } else {
                server=MBeanServerFactory.createMBeanServer();
                if( log.isDebugEnabled() )
                    log.debug("Creating MBeanServer"+ (System.currentTimeMillis() - t1 ));
            }
        }
        return (server);
    }

    // -------------------- Loading data from different sources  --------------

    /**
     * Load the registry from the XML input found in the specified input
     * stream.
     *
     * @param stream InputStream containing the registry configuration
     *  information
     *
     * @exception Exception if any parsing or processing error occurs
     * @deprecated use normal class method instead
     * @since 1.0
     */
    public static void loadRegistry(InputStream stream) throws Exception {
        Registry registry = getRegistry();
        registry.loadDescriptors( "MbeansDescriptorsDOMSource", stream, null );
    }


    /**
     * Load the registry from the XML input found in the specified input
     * stream.
     *
     * @param source Source to be used to load. Can be an InputStream or URL.
     *
     * @exception Exception if any parsing or processing error occurs
     * @since 1.1
     */
    public void loadDescriptors( Object source )
            throws Exception
    {
        loadDescriptors("MbeansDescriptorsDOMSource", source, null );
    }

    /** Lookup the component descriptor in the package and
     * in the parent packages.
     *
     * @param packageName
     */
    public void loadDescriptors( String packageName, ClassLoader classLoader  ) {
        String res=packageName.replace( '.', '/');

        if( log.isTraceEnabled() )
            log.trace("Finding descriptor " + res );

        if( searchedPaths.get( packageName ) != null ) {
            return;
        }
        String descriptors=res + "/mbeans-descriptors.ser";

        URL dURL=classLoader.getResource( descriptors );

        if( dURL == null ) {
            descriptors=res + "/mbeans-descriptors.xml";
            dURL=classLoader.getResource( descriptors );
        }
        if( dURL == null ) return;

        log.debug( "Found " + dURL);
        searchedPaths.put( packageName,  dURL );
        try {
            if( descriptors.endsWith(".xml" ))
                loadDescriptors("MbeansDescriptorsDOMSource", dURL, null);
            else
                loadDescriptors("MbeansDescriptorsSerSource", dURL, null);
            return;
        } catch(Exception ex ) {
            log.error("Error loading " + dURL);
        }

        return;
    }

    /**
     * Load the registry from a cached .ser file. This is typically 2-3 times
     * faster than parsing the XML.
     *
     * @param source Source to be used to load. Can be an InputStream or URL.
     *
     * @exception Exception if any parsing or processing error occurs
     * @since 1.1
     */
    public void loadCachedDescriptors( Object source )
            throws Exception
    {
        loadDescriptors("MbeansDescriptorsSerSource", source, null );
    }

    /** Experimental. Will become private, some code may still use it
     *
     * @param sourceType
     * @param source
     * @param param
     * @throws Exception
     * @deprecated
     */
    public void loadDescriptors( String sourceType, Object source, String param)
        throws Exception
    {
        List mbeans=load( sourceType, source, param );
        if( mbeans == null) return;

        Iterator itr=mbeans.iterator();
        while( itr.hasNext() ) {
            Object mb=itr.next();
            if( mb instanceof ManagedBean) {
                addManagedBean((ManagedBean)mb);
            }
        }
    }

    /** Load an extended mlet file. A similar syntax is supported by the
     * ant tasks.
     *
     * @param source InputStream or URL of the file
     * @return List of ObjectName for the loaded mbeans
     * @throws Exception
     * @since 1.1
     */
    public List loadMBeans( Object source )
            throws Exception
    {
        return load("MBeansSource", source, null );
    }

    /** Experimental.
     *
     * @param sourceType
     * @param source
     * @param param
     * @return
     * @throws Exception
     */
    public List load( String sourceType, Object source, String param)
        throws Exception
    {
        if( log.isTraceEnabled())
            log.trace("load " + source );

        ModelerSource ds=getModelerSource(sourceType);
        List mbeans=null;

        if( source instanceof URL ) {
            URL url=(URL)source;
            InputStream stream=url.openStream();
            mbeans=ds.loadDescriptors(this, url.toString(), param, stream);
        }

        if( source instanceof InputStream ) {
            mbeans=ds.loadDescriptors(this, null, param, source);
        }

        if( source instanceof Class ) {
            mbeans=ds.loadDescriptors(this, ((Class)source).getName(), param, source);
        }
        return mbeans;
    }

    /** Invoke a operation on a list of mbeans. Can be used to implement
     * lifecycle operations.
     *
     * @param mbeans list of ObjectName on which we'll invoke the operations
     * @param operation  Name of the operation ( init, start, stop, etc)
     * @param failFirst  If false, exceptions will be ignored
     * @throws Exception
     * @since 1.1
     */
    public void invoke( List mbeans, String operation, boolean failFirst )
            throws Exception
    {
        if( mbeans==null ) return;
        Iterator itr=mbeans.iterator();
        while(itr.hasNext()) {
            Object current=itr.next();
            ObjectName oN=null;
            try {
                if( current instanceof ObjectName) {
                    oN=(ObjectName)current;
                }
                if( current instanceof String ) {
                    oN=new ObjectName( (String)current );
                }
                if( oN==null ) continue;
                if( getMethodInfo(oN, operation) == null)
                    continue;
                getMBeanServer().invoke(oN, operation,
                        new Object[] {}, new String[] {});

            } catch( Exception t ) {
                if( failFirst ) throw t;
                log.info("Error initializing " + current + " " + t.toString());
            }
        }
    }

    // -------------------- Instance registration  --------------------

    /** @deprecated - may still be used in code using unstable builds
     */
    public void registerComponent(Object bean, String domain, String type,
                                  String name)
            throws Exception
    {
        StringBuffer sb=new StringBuffer();
        sb.append( domain ).append(":");
        sb.append( name );
        String nameStr=sb.toString();
        ObjectName oname=new ObjectName( nameStr );
        registerComponent(bean, oname, type );
    }

    /** Main registration method
     *
     * If metadata is not loaded, we'll look up for a file named
     * mbeans-descriptors.ser or "mbeans-descriptors.xml" in the same package
     * or parent.
     *
     * If the metadata is still not found, introspection will be used to generate
     * it automatically.
     *
     * If the mbean is already registered under this name, it'll be first
     * unregistered.
     *
     * @param bean Object to be registered
     * @param oname Name used for registration
     * @param type The type of the mbean, as declared in mbeans-descriptors. If
     * null, the name of the class will be used.
     *
     * @since 1.1
     */
    public void registerComponent(Object bean, ObjectName oname, String type)
           throws Exception
    {
        if( log.isDebugEnabled() )
            log.debug( "Managed= "+ oname);

        if( bean ==null ) {
            log.error("Null component " + oname );
            return;
        }

        try {
            if( type==null ) {
                type=bean.getClass().getName();
            }

            ManagedBean managed = registry.findManagedBean(bean.getClass(), type);

            // The real mbean is created and registered
            ModelMBean mbean = managed.createMBean(bean);

            if(  getMBeanServer().isRegistered( oname )) {
                if( log.isDebugEnabled())
                    log.debug("Unregistering existing component " + oname );
                getMBeanServer().unregisterMBean( oname );
            }

            getMBeanServer().registerMBean( mbean, oname);
        } catch( Exception ex) {
            log.error("Error registering " + oname, ex );
            throw ex;
        }
    }

    /** Unregister a component. This is just a helper that
     * avoids exceptions by checking if the mbean is already registered
     *
     * @param oname
     * @since 1.1
     */
    public void unregisterComponent( ObjectName oname ) {
        try {
            if( getMBeanServer().isRegistered(oname))
                getMBeanServer().unregisterMBean(oname);
        } catch( Throwable t ) {
            log.error( "Error unregistering mbean ", t);
        }
    }

    // should be removed
    public void unregisterComponent( String domain, String name ) {
        try {
            ObjectName oname=new ObjectName( domain + ":" + name );

            // XXX remove from our tables.
            getMBeanServer().unregisterMBean( oname );
        } catch( Throwable t ) {
            log.error( "Error unregistering mbean ", t );
        }
    }

    // -------------------- Helpers  --------------------

    /** Get the type of an attribute of the object, from the metadata.
     *
     * @param oname
     * @param attName
     * @return null if metadata about the attribute is not found
     * @since 1.1
     */
    public String getType( ObjectName oname, String attName )
    {
        String type=null;
        MBeanInfo info=null;
        try {
            info=server.getMBeanInfo(oname);
        } catch (Exception e) {
            log.info( "Can't find metadata for object" + oname );
            return null;
        }

        MBeanAttributeInfo attInfo[]=info.getAttributes();
        for( int i=0; i<attInfo.length; i++ ) {
            if( attName.equals(attInfo[i].getName())) {
                type=attInfo[i].getType();
                return type;
            }
        }
        return null;
    }

    public MBeanOperationInfo getMethodInfo( ObjectName oname, String opName )
    {
        String type=null;
        MBeanInfo info=null;
        try {
            info=server.getMBeanInfo(oname);
        } catch (Exception e) {
            log.info( "Can't find metadata " + oname );
            return null;
        }
        MBeanOperationInfo attInfo[]=info.getOperations();
        for( int i=0; i<attInfo.length; i++ ) {
            if( opName.equals(attInfo[i].getName())) {
                return attInfo[i];
            }
        }
        return null;
    }

    // -------------------- Experimental: discovery  --------------------
    /** Experimental support for manifest-based discovery.
     */
    public static String MODELER_MANIFEST="/META-INF/mbeans-descriptors.xml";

    /** Discover all META-INF/modeler.xml files in classpath and register
     * the components
     *
     * @since EXPERIMENTAL
     */
    public void loadMetaInfDescriptors(ClassLoader cl) {
        try {
            Enumeration en=cl.getResources(MODELER_MANIFEST);
            while( en.hasMoreElements() ) {
                URL url=(URL)en.nextElement();
                InputStream is=url.openStream();
                if( log.isDebugEnabled()) log.debug("Loading " + url);
                loadDescriptors("MBeansDescriptorDOMSource", is, null );
            }
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    // -------------------- Support for unique names  --------------------

    // Store all objects that have been registered via modeler
    // it is used to generate unique names automatically - using seq=
    // scheme
    private Hashtable instances=new Hashtable();

    /**
     * Help manage multiple instances with the same name prefix.
     * @since experimental
     */
    public int generateSeqNumber(String name) {
        //String name=getType( domain, realClass );

        Integer iInt=(Integer)instances.get(name );
        int seq=0;
        if( iInt!= null ) {
            seq=iInt.intValue();
            seq++;
            instances.put( name, new Integer( seq ));
        } else {
            instances.put( name, new Integer( 0 ));
        }
        return seq;
    }

    // -------------------- Notification codes  --------------------

    // per registry
    private int notificationId=0;
    private Hashtable notificationCodes=new Hashtable();

    /** Return an int ID for the notification name. Used for indexed
     * access.
     *
     * @param domain Not used currently
     * @param name  Type of the notification
     * @return
     */
    public synchronized int getNotificationCode( String domain, String name) {
        Integer i=(Integer)notificationCodes.get(name);
        if( i!= null ) return i.intValue();

        int code=notificationId++;
        notificationCodes.put( name, new Integer( code ));
        return code;
    }

    // -------------------- Implementation methods  --------------------

    private HashMap searchedPaths=new HashMap();

    /** Lookup the component descriptor in the package and
     * in the parent packages.
     *
     * @param bean
     * @return
     */
    private boolean findDescriptor( Class beanClass ) {
        String className=beanClass.getName();
        String pkg=className;
        while( pkg.indexOf( ".") > 0 ) {
            int lastComp=pkg.lastIndexOf( ".");
            if( lastComp <= 0 ) return false;
            pkg=pkg.substring(0, lastComp);
            if( searchedPaths.get( pkg ) != null ) {
                return false;
            }
            loadDescriptors(pkg, beanClass.getClassLoader());
        }
        return false;
    }


    private ModelerSource getModelerSource( String type )
            throws Exception
    {
        if( type==null ) type="MbeansDescriptorsDOMSource";
        if( type.indexOf( ".") < 0 ) {
            type="org.apache.commons.modeler.modules." + type;
        }

        Class c=Class.forName( type );
        ModelerSource ds=(ModelerSource)c.newInstance();
        return ds;
    }


}
