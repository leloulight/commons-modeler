/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/java/org/apache/commons/modeler/ManagedBean.java,v 1.3 2002/12/26 18:22:19 costin Exp $
 * $Revision: 1.3 $
 * $Date: 2002/12/26 18:22:19 $
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


import java.util.ArrayList;
import javax.management.*;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;


/**
 * <p>Internal configuration information for a managed bean (MBean)
 * descriptor.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2002/12/26 18:22:19 $
 */

public class ManagedBean implements java.io.Serializable
{
    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>ModelMBeanInfo</code> object that corresponds
     * to this <code>ManagedBean</code> instance.
     */
    ModelMBeanInfo info = null;


    // ------------------------------------------------------------- Properties


    /**
     * The collection of attributes for this MBean.
     */
    protected AttributeInfo attributes[] = new AttributeInfo[0];

    public AttributeInfo[] getAttributes() {
        return (this.attributes);
    }


    /**
     * The fully qualified name of the Java class of the MBean
     * described by this descriptor.  If not specified, the standard JMX
     * class (<code>javax.management.modelmbean.RequiredModeLMBean</code>)
     * will be utilized.
     */
    protected String className =
        //        "javax.management.modelmbean.RequiredModelMBean";
        "org.apache.commons.modeler.BaseModelMBean";
    public String getClassName() {
        return (this.className);
    }

    public void setClassName(String className) {
        this.className = className;
        this.info = null;
    }


    /**
     * The collection of constructors for this MBean.
     */
    protected ConstructorInfo constructors[] = new ConstructorInfo[0];

    public ConstructorInfo[] getConstructors() {
        return (this.constructors);
    }


    /**
     * The human-readable description of this MBean.
     */
    protected String description = null;

    public String getDescription() {
        return (this.description);
    }

    public void setDescription(String description) {
        this.description = description;
        this.info = null;
    }


    /**
     * The (optional) <code>ObjectName</code> domain in which this MBean
     * should be registered in the MBeanServer.
     */
    protected String domain = null;

    public String getDomain() {
        return (this.domain);
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }


    /**
     * The (optional) group to which this MBean belongs.
     */
    protected String group = null;

    public String getGroup() {
        return (this.group);
    }

    public void setGroup(String group) {
        this.group = group;
    }


    /**
     * The name of this managed bean, which must be unique among all
     * MBeans managed by a particular MBeans server.
     */
    protected String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
        this.info = null;
    }


    /**
     * The collection of notifications for this MBean.
     */
    protected NotificationInfo notifications[] = new NotificationInfo[0];

    public NotificationInfo[] getNotifications() {
        return (this.notifications);
    }


    /**
     * The collection of operations for this MBean.
     */
    protected OperationInfo operations[] = new OperationInfo[0];

    public OperationInfo[] getOperations() {
        return (this.operations);
    }


    /**
     * The fully qualified name of the Java class of the resource
     * implementation class described by the managed bean described
     * by this descriptor.
     */
    protected String type = null;

    public String getType() {
        return (this.type);
    }

    public void setType(String type) {
        this.type = type;
        this.info = null;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new attribute to the set of attributes for this MBean.
     *
     * @param attribute The new attribute descriptor
     */
    public void addAttribute(AttributeInfo attribute) {

        synchronized (attributes) {
            AttributeInfo results[] =
                new AttributeInfo[attributes.length + 1];
            System.arraycopy(attributes, 0, results, 0, attributes.length);
            results[attributes.length] = attribute;
            attributes = results;
            this.info = null;
        }

    }


    /**
     * Add a new constructor to the set of constructors for this MBean.
     *
     * @param constructor The new constructor descriptor
     */
    public void addConstructor(ConstructorInfo constructor) {

        synchronized (constructors) {
            ConstructorInfo results[] =
                new ConstructorInfo[constructors.length + 1];
            System.arraycopy(constructors, 0, results, 0, constructors.length);
            results[constructors.length] = constructor;
            constructors = results;
            this.info = null;
        }

    }


    /**
     * Add a new notification to the set of notifications for this MBean.
     *
     * @param notification The new notification descriptor
     */
    public void addNotification(NotificationInfo notification) {

        synchronized (notifications) {
            NotificationInfo results[] =
                new NotificationInfo[notifications.length + 1];
            System.arraycopy(notifications, 0, results, 0,
                             notifications.length);
            results[notifications.length] = notification;
            notifications = results;
            this.info = null;
        }

    }


    /**
     * Add a new operation to the set of operations for this MBean.
     *
     * @param operation The new operation descriptor
     */
    public void addOperation(OperationInfo operation) {
        synchronized (operations) {
            OperationInfo results[] =
                new OperationInfo[operations.length + 1];
            System.arraycopy(operations, 0, results, 0, operations.length);
            results[operations.length] = operation;
            operations = results;
            this.info = null;
        }

    }


    /**
     * Create and return a <code>ModelMBean</code> that has been
     * preconfigured with the <code>ModelMBeanInfo</code> information
     * for this managed bean, but is not associated with any particular
     * managed resource.  The returned <code>ModelMBean</code> will
     * <strong>NOT</strong> have been registered with our
     * <code>MBeanServer</code>.
     *
     * @exception InstanceNotFoundException if the managed resource
     *  object cannot be found
     * @exception InvalidTargetObjectTypeException if our MBean cannot
     *  handle object references (should never happen)
     * @exception MBeanException if a problem occurs instantiating the
     *  <code>ModelMBean</code> instance
     * @exception RuntimeOperationsException if a JMX runtime error occurs
     */
    public ModelMBean createMBean()
        throws InstanceNotFoundException,
        InvalidTargetObjectTypeException,
        MBeanException, RuntimeOperationsException {

        return (createMBean(null));

    }


    /**
     * Create and return a <code>ModelMBean</code> that has been
     * preconfigured with the <code>ModelMBeanInfo</code> information
     * for this managed bean, and is associated with the specified
     * managed object instance.  The returned <code>ModelMBean</code>
     * will <strong>NOT</strong> have been registered with our
     * <code>MBeanServer</code>.
     *
     * @param instance Instanced of the managed object, or <code>null</code>
     *  for no associated instance
     *
     * @exception InstanceNotFoundException if the managed resource
     *  object cannot be found
     * @exception InvalidTargetObjectTypeException if our MBean cannot
     *  handle object references (should never happen)
     * @exception MBeanException if a problem occurs instantiating the
     *  <code>ModelMBean</code> instance
     * @exception RuntimeOperationsException if a JMX runtime error occurs
     */
    public ModelMBean createMBean(Object instance)
        throws InstanceNotFoundException,
        InvalidTargetObjectTypeException,
        MBeanException, RuntimeOperationsException {

        // Load the ModelMBean implementation class
        Class clazz = null;
        try {
            clazz = Class.forName(getClassName());
        } catch (Exception e) {
            throw new MBeanException
                (e, "Cannot load ModelMBean class " + getClassName());
        }

        // Create a new ModelMBean instance
        ModelMBean mbean = null;
        try {
            mbean = (ModelMBean) clazz.newInstance();
            mbean.setModelMBeanInfo(createMBeanInfo());
        } catch (MBeanException e) {
            throw e;
        } catch (RuntimeOperationsException e) {
            throw e;
        } catch (Exception e) {
            throw new MBeanException
                (e, "Cannot instantiate ModelMBean of class " +
                 getClassName());
        }

        // Set the managed resource (if any)
        try {
            if (instance != null)
                mbean.setManagedResource(instance, "objectReference");
        } catch (InstanceNotFoundException e) {
            throw e;
        } catch (InvalidTargetObjectTypeException e) {
            throw e;
        }
        return (mbean);

    }


    /**
     * Create and return a <code>ModelMBeanInfo</code> object that
     * describes this entire managed bean.
     */
    public ModelMBeanInfo createMBeanInfo() {

        // Return our cached information (if any)
        if (info != null)
            return (info);

        // Create subordinate information descriptors as required
        AttributeInfo attrs[] = getAttributes();
        ModelMBeanAttributeInfo attributes[] =
            new ModelMBeanAttributeInfo[attrs.length];
        for (int i = 0; i < attrs.length; i++)
            attributes[i] = attrs[i].createAttributeInfo();
        ConstructorInfo consts[] = getConstructors();
        ModelMBeanConstructorInfo constructors[] =
            new ModelMBeanConstructorInfo[consts.length];
        for (int i = 0; i < consts.length; i++)
            constructors[i] = consts[i].createConstructorInfo();
        NotificationInfo notifs[] = getNotifications();
        ModelMBeanNotificationInfo notifications[] =
            new ModelMBeanNotificationInfo[notifs.length];
        for (int i = 0; i < notifs.length; i++)
            notifications[i] = notifs[i].createNotificationInfo();
        OperationInfo opers[] = getOperations();
        ModelMBeanOperationInfo operations[] =
            new ModelMBeanOperationInfo[opers.length];
        for (int i = 0; i < opers.length; i++)
            operations[i] = opers[i].createOperationInfo();

        /*
        // Add operations for attribute getters and setters as needed
        ArrayList list = new ArrayList();
        for (int i = 0; i < operations.length; i++)
            list.add(operations[i]);
        for (int i = 0; i < attributes.length; i++) {
            Descriptor descriptor = attributes[i].getDescriptor();
            String getMethod = (String) descriptor.getFieldValue("getMethod");
            if (getMethod != null) {
                OperationInfo oper =
                    new OperationInfo(getMethod, true,
                                      attributes[i].getType());
                list.add(oper.createOperationInfo());
            }
            String setMethod = (String) descriptor.getFieldValue("setMethod");
            if (setMethod != null) {
                OperationInfo oper =
                    new OperationInfo(setMethod, false,
                                      attributes[i].getType());
                list.add(oper.createOperationInfo());
            }
        }
        if (list.size() > operations.length)
            operations =
                (ModelMBeanOperationInfo[]) list.toArray(operations);
        */
        
        // Construct and return a new ModelMBeanInfo object
        info = new ModelMBeanInfoSupport
            (getClassName(), getDescription(),
             attributes, constructors, operations, notifications);
        return (info);

    }


    /**
     * Return a string representation of this managed bean.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ManagedBean[");
        sb.append("name=");
        sb.append(name);
        sb.append(", className=");
        sb.append(className);
        sb.append(", description=");
        sb.append(description);
        if (group != null) {
            sb.append(", group=");
            sb.append(group);
        }
        sb.append(", type=");
        sb.append(type);
        sb.append("]");
        return (sb.toString());

    }


}
