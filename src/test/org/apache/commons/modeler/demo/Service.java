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


import java.util.HashMap;


/**
 * <p>Sample managed object for the Modeler Demonstration Application,
 * based on the Catalina architecture of Tomcat 4.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
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
