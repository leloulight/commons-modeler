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


package org.apache.commons.modeler;


import java.util.List;

/**
 * Interface for modeler MBeans.
 * 
 * This is the main entry point into modeler. It provides methods to create
 * and manipulate model mbeans and simplify their use.
 *
 * Starting with version 1.1, this is no longer a singleton and the static
 * methods are strongly deprecated. In a container environment we can expect
 * different applications to use different registries.
 * 
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * 
 * @since 1.1
 */
public interface RegistryMBean {

    /** 
     * Load an extended mlet file. The source can be an URL, File or
     * InputStream. 
     * 
     * All mbeans will be instantiated, registered and the attributes will be 
     * set. The result is a list of ObjectNames.
     *
     * @param source InputStream or URL of the file
     * @param cl ClassLoader to be used to load the mbeans, or null to use the
     *        default JMX mechanism ( i.e. all registered loaders )
     * @return List of ObjectName for the loaded mbeans
     * @throws Exception
     * 
     * @since 1.1
     */ 
    public List loadMBeans( Object source, ClassLoader cl ) throws Exception;

    /** Invoke an operation on a set of mbeans. 
     * 
     * @param mbeans List of ObjectNames
     * @param operation Operation to perform. Typically "init" "start" "stop" or "destroy"
     * @param failFirst Behavior in case of exceptions - if false we'll ignore
     *      errors
     * @throws Exception
     */ 
    public void invoke( List mbeans, String operation, boolean failFirst )
            throws Exception;

    /** Register a bean by creating a modeler mbean and adding it to the 
     * MBeanServer.
     * 
     * If metadata is not loaded, we'll look up and read a file named
     * "mbeans-descriptors.ser" or "mbeans-descriptors.xml" in the same package
     * or parent.
     *
     * If the bean is an instance of DynamicMBean. it's metadata will be converted
     * to a model mbean and we'll wrap it - so modeler services will be supported
     *
     * If the metadata is still not found, introspection will be used to extract
     * it automatically. 
     * 
     * If an mbean is already registered under this name, it'll be first
     * unregistered.
     * 
     * If the component implements MBeanRegistration, the methods will be called.
     * If the method has a method "setRegistry" that takes a RegistryMBean as
     * parameter, it'll be called with the current registry.
     * 
     *
     * @param bean Object to be registered
     * @param oname Name used for registration
     * @param type The type of the mbean, as declared in mbeans-descriptors. If
     * null, the name of the class will be used. This can be used as a hint or
     * by subclasses.
     *
     * @since 1.1
     */ 
    public void registerComponent(Object bean, String oname, String type)
           throws Exception;

    /** Unregister a component. We'll first check if it is registered,
     * and mask all errors. This is mostly a helper.
     * 
     * @param oname
     * 
     * @since 1.1
     */ 
    public void unregisterComponent( String oname );


     /** Return an int ID for faster access. Will be used for notifications
      * and for other operations we want to optimize. 
      *
      * @param domain Namespace 
      * @param name  Type of the notification
      * @return  An unique id for the domain:name combination
      * @since 1.1
      */
    public int getId( String domain, String name);


    /** Reset all metadata cached by this registry. Should be called 
     * to support reloading. Existing mbeans will not be affected or modified.
     * 
     * It will be called automatically if the Registry is unregistered.
     * @since 1.1
     */ 
    public void stop();

    /** Load descriptors. The source can be a File, URL pointing to an
     * mbeans-descriptors.xml.
     * 
     * Also ( experimental for now ) a ClassLoader - in which case META-INF/ will
     * be used.
     * 
     * @param source
     */ 
    public void loadMetadata(Object source ) throws Exception;
}
