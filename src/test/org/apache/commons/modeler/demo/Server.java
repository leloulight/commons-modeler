/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/test/org/apache/commons/modeler/demo/Server.java,v 1.2 2003/10/09 21:39:05 rdonkin Exp $
 * $Revision: 1.2 $
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


import java.util.HashMap;


/**
 * <p>Sample managed object for the Modeler Demonstration Application,
 * based on the Catalina architecture of Tomcat 4.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2003/10/09 21:39:05 $
 */

public class Server {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a default instance of this class.
     */
    public Server() {

        super();

    }


    /**
     * Construct a configured instance of this class.
     *
     * @param port Port number of this server
     * @param shutdown Shutdown command of this server
     */
    public Server(int port, String shutdown) {

        super();
        setPort(port);
        setShutdown(shutdown);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of services associated with this Server, keyed by name.
     */
    private HashMap services = new HashMap();


    // ------------------------------------------------------------- Properties


    /**
     * The port number for our shutdown commands.
     */
    private int port = 8005;

    public int getPort() {
        return (this.port);
    }

    public void setPort(int port) {
        this.port = port;
    }


    /**
     * The shutdown command password.
     */
    private String shutdown = "SHUTDOWN";

    public String getShutdown() {
        return (this.shutdown);
    }

    public void setShutdown(String shutdown) {
        this.shutdown = shutdown;
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Service to this Server.
     *
     * @param service The service to be added
     */
    public void addService(Service service) {

        services.put(service.getName(), service);

    }


    /**
     * Find and return the specified Service associated with this Server.
     *
     * @param name Name of the requested service
     */
    public Service findService(String name) {

        return ((Service) services.get(name));

    }


    /**
     * Find and return all Services associated with this Server.
     */
    public Service[] findServices() {

        Service results[] = new Service[services.size()];
        return ((Service[]) services.values().toArray(results));

    }


    /**
     * Remove the specified Service from association with this Server.
     *
     * @param service The Service to be removed
     */
    public void removeService(Service service) {

        services.remove(service.getName());

    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("Server[");
        sb.append("port=");
        sb.append(port);
        sb.append(", shutdown=");
        sb.append(shutdown);
        sb.append("]");
        return (sb.toString());

    }


}
