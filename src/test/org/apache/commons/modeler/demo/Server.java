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
