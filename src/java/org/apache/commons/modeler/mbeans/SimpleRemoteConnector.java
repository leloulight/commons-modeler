/*
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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
package org.apache.commons.modeler.mbeans;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.Registry;

/**
 * Based on jk2 proxy.
 *
 * Proxy using a very simple HTTP based protocol.
 *
 * For efficiency, it'll get bulk results and cache them - you
 * can force an update by calling the refreshAttributes and refreshMetadata
 * operations on this mbean.
 *
 * TODO: implement the user/pass auth ( right now you must use IP based security )
 * TODO: eventually support https
 * TODO: support for metadata ( mbean-descriptors ) for description and type conversions
 * TODO: filter out trivial components ( mutexes, etc )
 *
 * @author Costin Manolache
 */
public class SimpleRemoteConnector
{
    private static Log log = LogFactory.getLog(SimpleRemoteConnector.class);

    // HTTP port of the remote JMX
    String webServerHost="localhost";
    int webServerPort=8080;

    // URL of the remote JMX servlet ( or equivalent )
    String statusPath="/jkstatus";

    // Not used right now
    String user;
    String pass;

    // Domain we mirror
    String domain;

    // XXX Not used - allow translations
    String localDomain;
    String filter;

    //
    long lastRefresh=0;
    long updateInterval=5000; // 5 sec - it's min time between updates

    String prefix="";

    Registry reg;

    MBeanServer mserver;

    // Last view
    HashMap mbeans=new HashMap();

    public SimpleRemoteConnector()
    {
    }

    /* -------------------- Public methods -------------------- */

    public String getWebServerHost() {
        return webServerHost;
    }

    public void setWebServerHost(String webServerHost) {
        this.webServerHost = webServerHost;
    }

    public int getWebServerPort() {
        return webServerPort;
    }

    public void setWebServerPort(int webServerPort) {
        this.webServerPort = webServerPort;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public void setStatusPath(String statusPath) {
        this.statusPath = statusPath;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    /* ==================== Start/stop ==================== */

    public void destroy() {
        try {
            // We should keep track of loaded beans and call stop.
            // Modeler should do it...
            Iterator mbeansIt=mbeans.values().iterator();
            while( mbeansIt.hasNext()) {
                MBeanProxy proxy=(MBeanProxy)mbeansIt.next();
                ObjectName oname=proxy.getObjectName();
                Registry.getRegistry().getMBeanServer().unregisterMBean(oname);
            }
        } catch( Throwable t ) {
            log.error( "Destroy error", t );
        }
    }

    public void init() throws IOException {
        try {
            //if( log.isDebugEnabled() )
            log.info("init " + webServerHost + " " + webServerPort);
            reg=Registry.getRegistry();
            // Get metadata for all mbeans on the remote side
            //refreshMetadata();
            // Get current values and mbeans
            refreshAttributes();
        } catch( Throwable t ) {
            log.error( "Init error", t );
        }
    }

    public void start() throws IOException {
        System.out.println("XXX start");
        if( reg==null)
            init();
    }

    /** Refresh the proxies, if updateInterval passed
     *
     */
    public void refresh()  {
        long time=System.currentTimeMillis();
        if( time - lastRefresh < updateInterval ) {
            return;
        }
        System.out.println("refresh... ");
        lastRefresh=time;
        //refreshMetadata();
        refreshAttributes();
    }

    public void refreshAttributes()  {
        try {
            int cnt=0;
            // connect to apache, get a list of mbeans
            if( filter==null ) {
                filter=domain + ":*";
            }

            InputStream is=getStream( "qry=" + filter);
            if( is==null ) return;

            Manifest mf=new Manifest(is);
            
            HashMap currentObjects=new HashMap(); // used to remove older ones
            Map entries=mf.getEntries();
            Iterator it=entries.keySet().iterator();
            while( it.hasNext() ) {
                String name=(String)it.next();
                Attributes attrs=(Attributes)entries.get( name );

                ObjectName oname=new ObjectName(name);
                currentObjects.put( oname, "");
                MBeanProxy proxy=(MBeanProxy)mbeans.get(oname);
                if( proxy==null ) {
                    log.debug( "New object " + name);
                    String code=attrs.getValue("modelerType");
                    if(log.isDebugEnabled())
                        log.debug("Register " + name  + " " + code );

                    proxy= new MBeanProxy(this, code);
                    mbeans.put( oname, proxy );

                    // Register
                    MBeanServer mserver=Registry.getRegistry().getMBeanServer();
                    if( ! mserver.isRegistered(oname ) ) {
                        mserver.registerMBean(proxy, oname);
                    }
                }
                Iterator it2=attrs.keySet().iterator();
                while( it2.hasNext() ) {
                    Object o=it2.next();
                    String att=(o==null) ? null : o.toString();
                    if( "modelerType".equals( att )) continue;
                    String val=attrs.getValue(att);
                    proxy.update(att, val);
                    cnt++;
                }
            }
            
            // Now we have to remove all the mbeans that are no longer there
            Iterator existingIt=mbeans.keySet().iterator();
            while( existingIt.hasNext() ) {
                ObjectName on=(ObjectName)existingIt.next();
                if(currentObjects.get( on ) != null )
                    continue; // still alive
                if( log.isDebugEnabled() )
                    log.debug("No longer alive " + on);
                try {
                    mserver.unregisterMBean(on);
                } catch( Throwable t ) {
                    log.info("Error unregistering " + on + " " + t.toString());
                }
            }
            
            log.info( "Refreshing attributes " + cnt);
        } catch( Exception ex ) {
            log.info("Error ", ex);
        }
    }

    // Not used right now - assume the metadata is available locally
    // Could use mbeans-descriptors.xml or other formats.
    // Will be called if code= is not found locally
    public void refreshMetadata() {
        try {
            int cnt=0;
            int newCnt=0;
            InputStream is=getStream("getMetadata=" + domain + ":*");
            if( is==null ) return;

            log.info( "Refreshing metadata " + cnt + " " +  newCnt);
        } catch( Exception ex ) {
            log.info("Error ", ex);
        }
    }

    public Object invoke(Object oname, String name, Object params[], String signature[])
        throws MBeanException, ReflectionException {
        try {
            // we support only string values
            InputStream is=this.getStream("invoke=" + name + "&name=" + oname.toString() );
            if( is==null ) return null;
//                String res=is.readLine();
//                if( log.isDebugEnabled())
//                    log.debug( "Invoking " + jkName + " " + name + " result " + res);

            //this.refreshMetadata();
            this.refreshAttributes();
        } catch( Exception ex ) {
            throw new MBeanException(ex);
        }
        return null;
    }


    public void setAttribute(ObjectName oname, Attribute attribute)
        throws AttributeNotFoundException, MBeanException,
        ReflectionException
    {
        try {
            // we support only string values
            String val=(String)attribute.getValue();
            String name=attribute.getName();
            InputStream is=this.getStream("set=" + name + "&name=" + oname.toString()
                    + "&value=" + val);
            if( is==null ) return;
//                String res=is.readLine();
//                if( log.isDebugEnabled())
//                    log.debug( "Setting " + jkName + " " + name + " result " + res);

            //this.refreshMetadata();
            this.refreshAttributes();
        } catch( Exception ex ) {
            throw new MBeanException(ex);
        }
    }

    /** connect to apache using http, get a list of mbeans. Can be
     * overriten to support different protocols ( jk/Unix domain sockets, etc )
      */
    protected InputStream getStream(String qry) throws Exception {
        try {
            String path=statusPath + "?" + qry;
            URL url=new URL( "http", webServerHost, webServerPort, path);
            log.debug( "Connecting to " + url);
            URLConnection urlc=url.openConnection();
            InputStream is=urlc.getInputStream();
            return is;
        } catch (IOException e) {
            log.info( "Can't connect to jkstatus " + webServerHost + ":" + webServerPort
            + " " + e.toString());
            return null;
        }
    }


}

