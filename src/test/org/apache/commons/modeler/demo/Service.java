/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/test/org/apache/commons/modeler/demo/Service.java,v 1.2 2003/10/09 21:39:05 rdonkin Exp $
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

public class Service {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a default instance of this class.
     */
    public Service() {

        super();

    }


    /**
     * Construct a configured instance of this class.
     *
     * @param name Name of this service
     * @param server Associated server
     */
    public Service(String name, Server server) {

        super();
        setName(name);
        setServer(server);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of connectors associated with this Service, keyed by port.
     */
    private HashMap connectors = new HashMap();


    // ------------------------------------------------------------- Properties


    /**
     * The associated Container for this Service.
     */
    public Container container = null;

    public Container getContainer() {
        return (this.container);
    }

    public void setContainer(Container container) {
        this.container = container;
    }


    /**
     * The name of this Service.
     */
    private String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * The associated Server for this Service.
     */
    private Server server = null;

    public Server getServer() {
        return (this.server);
    }

    public void setServer(Server server) {
        this.server = server;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Connector to this Service.
     *
     * @param connector The connector to be added
     */
    public void addConnector(Connector connector) {

        connectors.put(new Integer(connector.getPort()), connector);

    }


    /**
     * Find and return the specified Connector associated with this Service.
     *
     * @param port Port number of the requested connector
     */
    public Connector findConnector(int port) {

        return ((Connector) connectors.get(new Integer(port)));

    }


    /**
     * Find and return all Connectors associated with this Service.
     */
    public Connector[] findConnectors() {

        return ((Connector[]) connectors.values().toArray(new Connector[0]));

    }


    /**
     * Remove the specified Connector from association with this Service.
     *
     * @param connector The Connector to be removed
     */
    public void removeConnector(Connector connector) {

        connectors.remove(new Integer(connector.getPort()));

    }



    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("Service[");
        sb.append("name=");
        sb.append(name);
        sb.append("]");
        return (sb.toString());

    }


}
