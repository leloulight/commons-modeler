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


import java.util.Enumeration;
import java.util.Hashtable;

import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// EXPERIMENTAL. It may fit better in tomcat jndi impl.


/**
 *
 * Link between JNDI and JMX. JNDI can be used for persistence ( it is
 * an API for storing hierarchical data and a perfect fit for that ), as
 * well as an alternate view of the MBean registry.
 *
 * If this component is enabled, all MBeans will be registered in JNDI, and
 * all attributes that are set via JMX can be stored in a DirContext.
 *
 * This acts as a "recorder" for creation of mbeans and attribute changes
 * done via JMX.
 *
 * XXX How can we control ( filter ) which mbeans will be registere ? Or
 * attributes ?
 * XXX How can we get the beans and attributes loaded before jndijmx ?
 *
 * The intended use:
 * - do whatever you want to start the application
 * - load JndiJmx as an mbean
 * - make changes via JMX. All changes are recorded
 * - you can use JndiJmx to save the changes in a Jndi context.
 * - you can use JndiJmx to load changes from a JndiContext and replay them.
 *
 * The main benefit is that only changed attributes are saved, and the Jndi
 * layer can preserve most of the original structure of the config file. The
 * alternative is to override the config files with config info extracted
 * from the live objects - but it's very hard to save only what was actually
 * changed and preserve structure and comments.
 *
 * @author Costin Manolache
 */
public class JndiJmx extends BaseModelMBean implements NotificationListener {


    private static Log log= LogFactory.getLog(JndiJmx.class);

    protected Context componentContext;
    protected Context descriptorContext;
    protected Context configContext;

    MBeanServer mserver;

    /**
     * Protected constructor to require use of the factory create method.
     */
    public JndiJmx() throws MBeanException {
        super(JndiJmx.class.getName());
    }


    /** If a JNDI context is set, all components
     * will be registered in the context.
     *
     * @param ctx
     */
    public void setComponentContext(Context ctx) {
        this.componentContext= ctx;
    }

    /** JNDI context for component descriptors ( metadata ).
     *
     * @param ctx
     */
    public void setDescriptorContext(Context ctx) {
        this.descriptorContext= ctx;
    }

    /** JNDI context where attributes will be stored for persistence
     *
     */
    public void setConfigContext( Context ctx ) {
        this.configContext= ctx;
    }

    // --------------------  Registration/unregistration --------------------
    // temp - will only set in the jndi contexts
    Hashtable attributes=new Hashtable();
    Hashtable instances=new Hashtable();

    public void handleNotification(Notification notification, Object handback)
    {
        // register/unregister mbeans in jndi
        if( notification instanceof MBeanServerNotification ) {
            MBeanServerNotification msnot=(MBeanServerNotification)notification;

            ObjectName oname=msnot.getMBeanName();

            if( "jmx.mbean.created".equalsIgnoreCase( notification.getType() )) {
                try {
                    Object mbean=mserver.getObjectInstance(oname);

                    if( log.isDebugEnabled() )
                        log.debug( "MBean created " + oname + " " + mbean);

                    // XXX add filter support
                    if( mbean instanceof NotificationBroadcaster ) {
                        // register for attribute changes
                        NotificationBroadcaster nb=(NotificationBroadcaster)mbean;
                        nb.addNotificationListener(this, null, null);
                        if( log.isDebugEnabled() )
                            log.debug( "Add attribute change listener");
                    }

                    instances.put( oname.toString(), mbean );
                } catch( InstanceNotFoundException ex ) {
                    log.error( "Instance not found for the created object", ex );
                }
            }
            if( "jmx.mbean.deleted".equalsIgnoreCase( notification.getType() )) {
                instances.remove(oname.toString());
            }
        }

        // set attributes in jndi
       //     if( "jmx.attribute.changed".equals( notification.getType() )) {
        if( notification instanceof AttributeChangeNotification) {

            AttributeChangeNotification anotif=(AttributeChangeNotification)notification;
            String name=anotif.getAttributeName();
            Object value=anotif.getNewValue();
            Object source=anotif.getSource();
            String mname=null;

            Hashtable mbeanAtt=(Hashtable)attributes.get( source );
            if( mbeanAtt==null ) {
                mbeanAtt=new Hashtable();
                attributes.put( source, mbeanAtt);
                if( log.isDebugEnabled())
                    log.debug("First attribute for " + source );
            }
            mbeanAtt.put( name, anotif );

            log.debug( "Attribute change notification " + name + " " + value + " " + source );

        }

    }

    public String dumpStatus() throws Exception
    {
        StringBuffer sb=new StringBuffer();
        Enumeration en=instances.keys();
        while (en.hasMoreElements()) {
            String on = (String) en.nextElement();
            Object mbean=instances.get(on);
            Hashtable mbeanAtt=(Hashtable)attributes.get(mbean);

            sb.append( "<mbean class=\"").append(on).append("\">");
            sb.append( "\n");
            Enumeration attEn=mbeanAtt.keys();
            while (attEn.hasMoreElements()) {
                String an = (String) attEn.nextElement();
                AttributeChangeNotification anotif=
                        (AttributeChangeNotification)mbeanAtt.get(an);
                sb.append("  <attribute name=\"").append(an).append("\" ");
                sb.append("value=\"").append(anotif.getNewValue()).append("\">");
                sb.append( "\n");
            }


            sb.append( "</mbean>");
            sb.append( "\n");
        }
        return sb.toString();
    }

    public void replay() throws Exception
    {


    }


    public void init() throws Exception
    {

        MBeanServer mserver=(MBeanServer)Registry.getRegistry().getMBeanServer();
        ObjectName delegate=new ObjectName("JMImplementation:type=MBeanServerDelegate");

        // XXX need to extract info about previously loaded beans

        // we'll know of all registered beans
        mserver.addNotificationListener(delegate, this, null, null );

    }

}
