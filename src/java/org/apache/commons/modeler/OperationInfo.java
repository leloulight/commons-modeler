/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/java/org/apache/commons/modeler/OperationInfo.java,v 1.1 2002/04/30 20:58:52 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2002/04/30 20:58:52 $
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
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


package org.apache.commons.modeler;


import javax.management.Descriptor;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;


/**
 * <p>Internal configuration information for an <code>Operation</code>
 * descriptor.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2002/04/30 20:58:52 $
 */

public class OperationInfo extends FeatureInfo {


    // ----------------------------------------------------------- Constructors


    /**
     * Standard zero-arguments constructor.
     */
    public OperationInfo() {

        super();

    }


    /**
     * Special constructor for setting up getter and setter operations.
     *
     * @param name Name of this operation
     * @param getter Is this a getter (as opposed to a setter)?
     * @param type Data type of the return value (if this is a getter)
     *  or the parameter (if this is a setter)
     * 
     */
    public OperationInfo(String name, boolean getter, String type) {

        super();
        setName(name);
        if (getter) {
            setDescription("Attribute getter method");
            setImpact("INFO");
            setReturnType(type);
            setRole("getter");
        } else {
            setDescription("Attribute setter method");
            setImpact("ACTION");
            setReturnType("void");
            setRole("setter");
            addParameter(new ParameterInfo("value", type,
                                           "New attribute value"));
        }

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>ModelMBeanOperationInfo</code> object that corresponds
     * to this <code>OperationInfo</code> instance.
     */
    ModelMBeanOperationInfo info = null;


    // ------------------------------------------------------------- Properties


    /**
     * Override the <code>description</code> property setter.
     *
     * @param description The new description
     */
    public void setDescription(String description) {
        super.setDescription(description);
        this.info = null;
    }


    /**
     * Override the <code>name</code> property setter.
     *
     * @param name The new name
     */
    public void setName(String name) {
        super.setName(name);
        this.info = null;
    }


    /**
     * The "impact" of this operation, which should be a (case-insensitive)
     * string value "ACTION", "ACTION_INFO", "INFO", or "UNKNOWN".
     */
    protected String impact = "UNKNOWN";

    public String getImpact() {
        return (this.impact);
    }

    public void setImpact(String impact) {
        if (impact == null)
            this.impact = null;
        else
            this.impact = impact.toUpperCase();
    }


    /**
     * The role of this operation ("getter", "setter", "operation", or
     * "constructor").
     */
    protected String role = "operation";

    public String getRole() {
        return (this.role);
    }

    public void setRole(String role) {
        this.role = role;
    }


    /**
     * The fully qualified Java class name of the return type for this
     * operation.
     */
    protected String returnType = "void";    // FIXME - Validate

    public String getReturnType() {
        return (this.returnType);
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }


    /**
     * The set of parameters for this operation.
     */
    protected ParameterInfo parameters[] = new ParameterInfo[0];

    public ParameterInfo[] getSignature() {
        return (this.parameters);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new parameter to the set of arguments for this operation.
     *
     * @param parameter The new parameter descriptor
     */
    public void addParameter(ParameterInfo parameter) {

        synchronized (parameters) {
            ParameterInfo results[] = new ParameterInfo[parameters.length + 1];
            System.arraycopy(parameters, 0, results, 0, parameters.length);
            results[parameters.length] = parameter;
            parameters = results;
            this.info = null;
        }

    }


    /**
     * Create and return a <code>ModelMBeanOperationInfo</code> object that
     * corresponds to the attribute described by this instance.
     */
    public ModelMBeanOperationInfo createOperationInfo() {

        // Return our cached information (if any)
        if (info != null)
            return (info);

        // Create and return a new information object
        ParameterInfo params[] = getSignature();
        MBeanParameterInfo parameters[] =
            new MBeanParameterInfo[params.length];
        for (int i = 0; i < params.length; i++)
            parameters[i] = params[i].createParameterInfo();
        int impact = ModelMBeanOperationInfo.UNKNOWN;
        if ("ACTION".equals(getImpact()))
            impact = ModelMBeanOperationInfo.ACTION;
        else if ("ACTION_INFO".equals(getImpact()))
            impact = ModelMBeanOperationInfo.ACTION_INFO;
        else if ("INFO".equals(getImpact()))
            impact = ModelMBeanOperationInfo.INFO;
        info = new ModelMBeanOperationInfo
            (getName(), getDescription(), parameters,
             getReturnType(), impact);
        Descriptor descriptor = info.getDescriptor();
        descriptor.removeField("class");
        descriptor.setField("role", getRole());
        info.setDescriptor(descriptor);
        return (info);

    }


    /**
     * Return a string representation of this operation descriptor.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("OperationInfo[");
        sb.append("name=");
        sb.append(name);
        sb.append(", description=");
        sb.append(description);
        sb.append(", returnType=");
        sb.append(returnType);
        sb.append(", parameters=");
        sb.append(parameters.length);
        sb.append("]");
        return (sb.toString());

    }


}
