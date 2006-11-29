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

public interface Container {


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------------- Properties


    /**
     * Return the name of this Container.
     */
    public String getName();


    /**
     * Set the name of this Container.
     *
     * @param name The new name
     */
    public void setName(String name);


    /**
     * Return the parent Container of this Container.
     */
    public Container getParent();


    /**
     * Set the parent Container of this Container.
     *
     * @param parent The new parent container
     */
    public void setParent(Container parent);


}



