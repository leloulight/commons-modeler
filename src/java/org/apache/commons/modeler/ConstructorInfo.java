/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/java/org/apache/commons/modeler/ConstructorInfo.java,v 1.5 2003/02/20 05:56:00 costin Exp $
 * $Revision: 1.5 $
 * $Date: 2003/02/20 05:56:00 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import java.io.Serializable;


/**
 * <p>Internal configuration information for a <code>Constructor</code>
 * descriptor.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2003/02/20 05:56:00 $
 */

public class ConstructorInfo extends FeatureInfo implements Serializable {
    static final long serialVersionUID = -5735336213417238238L;

    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>ModelMBeanConstructorInfo</code> object that corresponds
     * to this <code>ConstructorInfo</code> instance.
     */
    transient ModelMBeanConstructorInfo info = null;
    protected String displayName = null;
    protected ParameterInfo parameters[] = new ParameterInfo[0];


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
     * The display name of this attribute.
     */
    public String getDisplayName() {
        return (this.displayName);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     * The set of parameters for this constructor.
     */
    public ParameterInfo[] getSignature() {
        return (this.parameters);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new parameter to the set of parameters for this constructor.
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
     * Create and return a <code>ModelMBeanConstructorInfo</code> object that
     * corresponds to the attribute described by this instance.
     */
    public ModelMBeanConstructorInfo createConstructorInfo() {

        // Return our cached information (if any)
        if (info != null)
            return (info);

        // Create and return a new information object
        ParameterInfo params[] = getSignature();
        MBeanParameterInfo parameters[] =
            new MBeanParameterInfo[params.length];
        for (int i = 0; i < params.length; i++)
            parameters[i] = params[i].createParameterInfo();
        info = new ModelMBeanConstructorInfo
            (getName(), getDescription(), parameters);
        Descriptor descriptor = info.getDescriptor();
        descriptor.removeField("class");
        if (getDisplayName() != null)
            descriptor.setField("displayName", getDisplayName());
        addFields(descriptor);
        info.setDescriptor(descriptor);
        return (info);

    }


    /**
     * Return a string representation of this constructor descriptor.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ConstructorInfo[");
        sb.append("name=");
        sb.append(name);
        sb.append(", description=");
        sb.append(description);
        sb.append(", parameters=");
        sb.append(parameters.length);
        sb.append("]");
        return (sb.toString());

    }


}
