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


/**
 * <p>Sample managed object for the Modeler Demonstration Application,
 * based on the Catalina architecture of Tomcat 4.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class Connector {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a default instance of this class.
     */
    public Connector() {

        super();

    }


    /**
     * Construct a configured instance of this class.
     *
     * @param port Port number
     * @param scheme Protocol (scheme)
     * @param secure Secure flag
     * @param service Associated service
     * @param container Associated container
     */
    public Connector(int port, String scheme, boolean secure,
                     Service service, Container container) {

        super();
        setPort(port);
        setScheme(scheme);
        setSecure(secure);
        setService(service);
        setContainer(container);

    }


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------------- Properties


    /**
     * The Container for this Connector.
     */
    private Container container = null;

    public Container getContainer() {
        return (this.container);
    }

    public void setContainer(Container container) {
        this.container = container;
    }


    /**
     * The port number of this Connector.
     */
    private int port = 8080;

    public int getPort() {
        return (this.port);
    }

    public void setPort(int port) {
        this.port = port;
    }


    /**
     * The scheme of this Connector.
     */
    private String scheme = "http";

    public String getScheme() {
        return (this.scheme);
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }


    /**
     * The secure flag of this Connector.
     */
    private boolean secure = false;

    public boolean getSecure() {
        return (this.secure);
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }


    /**
     * The associated Service of this Connector.
     */
    private Service service = null;

    public Service getService() {
        return (this.service);
    }

    public void setService(Service service) {
        this.service = service;
    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("Connector[");
        sb.append("port=");
        sb.append(port);
        sb.append(", scheme=");
        sb.append(scheme);
        sb.append(", secure=");
        sb.append(secure);
        sb.append("]");
        return (sb.toString());

    }


}
