/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/test/org/apache/commons/modeler/demo/Connector.java,v 1.2 2003/10/09 21:39:05 rdonkin Exp $
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


/**
 * <p>Sample managed object for the Modeler Demonstration Application,
 * based on the Catalina architecture of Tomcat 4.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2003/10/09 21:39:05 $
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
