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
import java.net.URL;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Depends on JNDI - if a DirContext is provided we'll push info into it.
import javax.naming.*;
import javax.naming.directory.*;

// EXPERIMENTAL for now.

// TODO: implement other 'repositories' for metadata ( or at least .ser
// caching and jboss dtd.
//

/**
 * JMX-enable components.
 *
 * This class has no dependency on JMX - if JMX is not available it'll
 * just do nothing. This allows applications to compile with a dependency
 * on commons-modeler, but without requiring jmx.jar.
 *
 * TODO: An additional feature is the 'lazy-ness'. The introspection and loading
 * of the XML files as well as registration can be delayed until a management
 * component is loaded. This reduces the startup time.
 *
 * Based on Register, MBeanUtil
 *
 * @author Craig R. McClanahan
 * @author Costin Manolache
 */
public class BaseRegistry {


    private static Log log= LogFactory.getLog(BaseRegistry.class);

    protected Context componentContext;
    protected Context descriptorContext;
    protected Context configContext;

    /**
     * Protected constructor to require use of the factory create method.
     */
    protected BaseRegistry() {
    }


    /** If a JNDI context is set, all components
     * will be registered in the context.
     *
     * @param ctx
     */
    public void setComponentContext(Context ctx) {
        this.componentContext= ctx;
    }

    public Object getMBeanServer() {
        return null;
    }

    /** Register all component descriptors in this
     * naming context.
     *
     * @param ctx
     */
    public void setDescriptorContext(Context ctx) {
        this.descriptorContext= ctx;
    }

    public void setConfigContext( Context ctx ) {
        this.configContext= ctx;
    }

    // -------------------- API exposed by Modeler --------------------

    /** The method used by applications to jmx-enable a component
     *  instance.
     *
     *  @param bean Any object. If a description is registered, it'll be used. If not
     *                          introspection will be used.
     *  @param domain Domain for the object. Default domain used if null.
     *                XXX call a getDomain() on the component ?
     *  @param name Desired name for the object. If the name is already registered, a
     *              seq=... will be appened. XXX How do we parse it ? JNDI-style ?
     *
     */
    public void registerComponent(Object bean, String domain, String name) {
        getRegistry().registerComponent(bean, domain,name );
    }

    /** The method used by applications to jmx-enable a component
     *  class.
     *  The 'source' is a meta-data source. If null, introspection will be used.
     *  Different modeler plugins may recognize different types of sources
     *  ( URLs, File, etc ) and content ( xml, .ser, etc ).
     */
    public void registerClass(Class beanClass, String domain, String className,
                              String type, Object source)
    {
        getRegistry().registerClass(beanClass, domain, className, type, source);
    }

    /** Load component descriptors from a stream.
     *
     * @param is
     * @param type The type of the descriptor. Right now only 'modeler' is
     *      supported. In future we may add 'jboss' or other dtds.
     */
    public void loadDescriptors( InputStream is, String type ) {

    }

    /** Locate descriptors in the class loader, using a discovery mechansim
     *  For "modeler" it'll use "/META-INF/modler-mbeans.xml".
     */
    public void loadDescriptors( ClassLoader cl, String type ) {

    }

    /** Find an mbean descriptor for the type
     *
     */
    public Object findDescriptor( String type ) {
        return null;
    }

    /** Create a unique name. It'll use the domain and type and add a unique
     *  sequence number if requested.
     *
     * XXX Add 'key' info to the metadata about object - so that more can be
     * automated.
     *
     * @param seq true if a sequence is needed, false if the object is singleton
     *
     */
    public String createName( String domain, String type,
                              Object o, boolean seq )
    {
        return null;
    }

    public void unregisterMBean( String name ) {
        getRegistry().unregisterMBean(name);
    }

    // Store all objects that have been registered via modeler
    // it is used to generate unique names automatically - using seq=
    // scheme
    Hashtable instances=new Hashtable();

    /** Find the 'type' for this class
     *
     */
    public String getType( String domain, Class realClass ) {
        // first look at explicit mappings ( exceptions in MBeanUtils )

        // We could also look up super classes and locate one we know about

        // We could use the domain as a discriminator

        String name=realClass.getName();
        name=name.substring( name.lastIndexOf( ".") + 1 );


        return name;
    }

    /** If a name was not provided, generate a name based on the
     *  class name and a sequence number.
     */
    public String generateSeqName(String domain, Class realClass) {
        String name=getType( domain, realClass );

        Integer iInt=(Integer)instances.get(name );
        int seq=0;
        if( iInt!= null ) {
            seq=iInt.intValue();
            seq++;
            instances.put( name, new Integer( seq ));
        } else {
            instances.put( name, new Integer( 0 ));
        }
        return "name=" + name + ",seq=" + seq;
    }


    HashMap typeMappings;

    /** Add a mapping between a className and a type. The type is the
     * key used to locate the mbean descriptors ( type=foo in the mbean name ).
     * It serve the same purpose as 'modules.xml' in tomcat3 and the exceptions
     * in tomcat4 MBeanUtils.
     *
     */
    public void addTypeMapping( String className, String type ) {
        typeMappings.put( className, type );
    }

    // -------------- Implementation ----------------------
    private static BaseRegistry reg=null;

    public static BaseRegistry getRegistry() {
        if( reg!=null ) return reg;
        try {
            Class.forName("javax.management.ObjectName");
            Class regClass=Class.forName("org.apache.commons.modeler.Registry");
            reg=(BaseRegistry)regClass.newInstance();
            if( log.isDebugEnabled() ) log.debug( "Create JMX Registry");
        } catch( Exception ex ) {
            log.info("JMX not found ");
            reg=new BaseRegistry();
            if( log.isDebugEnabled() ) log.debug( "Create non-JMX Registry");
        }
        return reg;
    }

}
