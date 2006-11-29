/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

public class Engine implements Container {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a default instance of this class.
     */
    public Engine() {

        super();

    }


    /**
     * Construct a configured instance of this class.
     *
     * @param name Name of this Engine
     * @param defaultHost Default host name for this Engine
     * @param service Associated service
     */
    public Engine(String name, String defaultHost, Service service) {

        super();
        setName(name);
        setDefaultHost(defaultHost);
        setService(service);

    }


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------------- Properties


    /**
     * The default host name of this Engine.
     */
    private String defaultHost = null;

    public String getDefaultHost() {
        return (this.defaultHost);
    }

    public void setDefaultHost(String defaultHost) {
        this.defaultHost = null;
    }


    /**
     * The name of this Engine.
     */
    private String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * The parent Container of this Engine.
     */
    private Container parent = null;

    public Container getParent() {
        return (this.parent);
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }


    /**
     * The associated Service of this Engine.
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

        StringBuffer sb = new StringBuffer("Engine[");
        sb.append("name=");
        sb.append(name);
        sb.append(", defaultHost=");
        sb.append(defaultHost);
        sb.append("]");
        return (sb.toString());

    }


}
