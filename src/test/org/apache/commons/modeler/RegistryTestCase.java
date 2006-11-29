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


package org.apache.commons.modeler;


import java.io.FileInputStream;
import java.util.List;

import javax.management.Descriptor;
import javax.management.MBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * <p>Test Case for the Registry class.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */

public class RegistryTestCase extends TestCase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Registry we will be testing.
     */
    protected Registry registry = null;


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public RegistryTestCase(String name) {

        super(name);

    }


    // --------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {

        registry = Registry.getRegistry();
        String names[] = registry.findManagedBeans();
        if (names.length == 0) {
            FileInputStream stream = new FileInputStream
                ("src/test/org/apache/commons/modeler/mbeans-descriptors.xml");
            Registry.loadRegistry(stream);
            stream.close();
        }

    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {

        return (new TestSuite(RegistryTestCase.class));

    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {

        registry = null;

    }


    // ------------------------------------------------ Individual Test Methods


    /**
     * Test ModelMBeanAttributeInfo information.
     */
    public void testModelMBeanAttributeInfo() throws Exception {

        // Retrieve a ManagedBean
        ManagedBean http = registry.findManagedBean("HttpConnector");
        assertNotNull("Found HttpConnector managed bean");

        // Create the associated ModelMBeanInfo
        ModelMBeanInfo info = http.createMBeanInfo();
        assertNotNull("Found HttpConnector ModelMBeanInfo", info);

        // Retrieve the specified ModelMBeanAttributeInfo
        ModelMBeanAttributeInfo mmainfo = info.getAttribute("acceptCount");
        assertNotNull("Found HttpConnector acceptCount info", mmainfo);

        // Get the Descriptor
        Descriptor desc = mmainfo.getDescriptor();
        assertNotNull("Found HttpConnector acceptCount descriptor", desc);

        // Check the configured fields
        checkDescriptor(desc, "field1", "HttpConnector.acceptCount/field1");
        checkDescriptor(desc, "field2", "HttpConnector.acceptCount/field2");

    }


    /**
     * Test ModelMBeanConstructorInfo information.
     */
    public void testModelMBeanConstructorInfo() throws Exception {

        // Retrieve a ManagedBean
        ManagedBean http = registry.findManagedBean("HttpConnector");
        assertNotNull("Found HttpConnector managed bean");

        // Create the associated ModelMBeanInfo
        ModelMBeanInfo info = http.createMBeanInfo();
        assertNotNull("Found HttpConnector ModelMBeanInfo", info);

        // Retrieve the relevant MBeanConstructorInfo array
        MBeanConstructorInfo mcinfo[] = info.getConstructors();
        assertNotNull("Found HttpConnector MBeanConstructorInfo array", mcinfo);
        assertEquals("Found HttpConnector MBeanConstructorInfo entry",
                     1, mcinfo.length);

        // Cast first entry to ModelMBeanConstructorInfo
        ModelMBeanConstructorInfo mmcinfo =
            (ModelMBeanConstructorInfo) mcinfo[0];

        // Get the Descriptor
        Descriptor desc = mmcinfo.getDescriptor();
        assertNotNull("Found HttpConnector constructor descriptor", desc);

        // Check the configured fields
        checkDescriptor(desc, "role", "constructor");
        checkDescriptor(desc, "field1", "HttpConnector.constructor/field1");
        checkDescriptor(desc, "field2", "HttpConnector.constructor/field2");

    }


    /**
     * Test descriptor entries.
     */
    public void testDescriptorEntries() {

        // Retrive the ManageBean that has descriptor info
        ManagedBean http = registry.findManagedBean("HttpConnector");
        assertNotNull("Found HttpConnector managed bean");

        // Check descriptor fields on the ManagedBean itself
        List beanFields = http.getFields();
        assertNotNull("Found HttpConnector fields");
        checkField(beanFields, "field1", "HttpConnector/field1");
        checkField(beanFields, "field2", "HttpConnector/field2");

        // Retrieve the AttributeInfo that has descriptors set
        AttributeInfo attrs[] = http.getAttributes();
        AttributeInfo attr = null;
        for (int i = 0; i < attrs.length; i++) {
            if ("acceptCount".equals(attrs[i].getName())) {
                attr = attrs[i];
                break;
            }
        }
        assertNotNull("Found attribute");

        // Check descriptor fields on the AttributeInfo
        List attrFields = attr.getFields();
        assertNotNull("Found attribute fields");
        checkField(attrFields, "field1", "HttpConnector.acceptCount/field1");
        checkField(attrFields, "field2", "HttpConnector.acceptCount/field2");

        // Retrieve the ConstructorInfo that has descriptors set
        ConstructorInfo constrs[] = http.getConstructors();
        ConstructorInfo constr = null;
        for (int i = 0; i < constrs.length; i++) {
            if ("HttpConnector".equals(constrs[i].getName())) {
                constr = constrs[i];
                break;
            }
        }
        assertNotNull("Found constructor");

        // Check descriptor fields on the ConstructorInfo
        List constrFields = constr.getFields();
        assertNotNull("Found constructor fields");
        checkField(constrFields, "field1", "HttpConnector.constructor/field1");
        checkField(constrFields, "field2", "HttpConnector.constructor/field2");

        // Retrieve the NotificationInfo that has descriptors set
        NotificationInfo notifs[] = http.getNotifications();
        NotificationInfo notif = null;
        for (int i = 0; i < notifs.length; i++) {
            if ("Problem".equals(notifs[i].getName())) {
                notif = notifs[i];
                break;
            }
        }
        assertNotNull("Found notification");

        // Check descriptor fields on the NotificationInfo
        List notifFields = notif.getFields();
        assertNotNull("Found notification fields");
        checkField(notifFields, "field1", "HttpConnector.problem/field1");
        checkField(notifFields, "field2", "HttpConnector.problem/field2");

        // Retrieve the OperationInfo that has descriptors set
        OperationInfo opers[] = http.getOperations();
        OperationInfo oper = null;
        for (int i = 0; i < opers.length; i++) {
            if ("initialize".equals(opers[i].getName())) {
                oper = opers[i];
                break;
            }
        }
        assertNotNull("Found operation");

        // Check descriptor fields on the OperationInfo
        List operFields = oper.getFields();
        assertNotNull("Found operation fields");
        checkField(operFields, "field1", "HttpConnector.initialize/field1");
        checkField(operFields, "field2", "HttpConnector.initialize/field2");

    }


    /**
     * Test ModelMBeanInfo information.
     */
    public void testModelMBeanInfo() throws Exception {

        // Retrive a ManagedBean
        ManagedBean http = registry.findManagedBean("HttpConnector");
        assertNotNull("Found HttpConnector managed bean");

        // Create the associated ModelMBeanInfo
        ModelMBeanInfo info = http.createMBeanInfo();
        assertNotNull("Found HttpConnector ModelMBeanInfo", info);

        // Check the basic properties
        assertEquals("Correct className",
                     "org.apache.catalina.mbeans.HttpConnectorModelMBean",
                     info.getClassName());
        assertEquals("Correct description",
                     "HTTP/1.1 Connector for Tomcat Standalone",
                     info.getDescription());

        // Get the Descriptor
        Descriptor desc = info.getMBeanDescriptor();
        assertNotNull("Found HttpConnector MBeanDescriptor", desc);

        // Check the configured fields
        checkDescriptor(desc, "field1", "HttpConnector/field1");
        checkDescriptor(desc, "field2", "HttpConnector/field2");

    }


    /**
     * Test ModelMBeanNotificationInfo information.
     */
    public void testModelMBeanNotificationInfo() throws Exception {

        // Retrieve a ManagedBean
        ManagedBean http = registry.findManagedBean("HttpConnector");
        assertNotNull("Found HttpConnector managed bean");

        // Create the associated ModelMBeanInfo
        ModelMBeanInfo info = http.createMBeanInfo();
        assertNotNull("Found HttpConnector ModelMBeanInfo", info);

        // Retrieve the specified ModelMBeanNotificationInfo
        ModelMBeanNotificationInfo mmninfo = info.getNotification("Problem");
        assertNotNull("Found HttpConnector problem info", mmninfo);

        // Get the Descriptor
        Descriptor desc = mmninfo.getDescriptor();
        assertNotNull("Found HttpConnector problem descriptor", desc);

        // Check the configured fields
        checkDescriptor(desc, "field1", "HttpConnector.problem/field1");
        checkDescriptor(desc, "field2", "HttpConnector.problem/field2");

    }


    /**
     * Test ModelMBeanOperationInfo information.
     */
    public void testModelMBeanOperationInfo() throws Exception {

        // Retrieve a ManagedBean
        ManagedBean http = registry.findManagedBean("HttpConnector");
        assertNotNull("Found HttpConnector managed bean");

        // Create the associated ModelMBeanInfo
        ModelMBeanInfo info = http.createMBeanInfo();
        assertNotNull("Found HttpConnector ModelMBeanInfo", info);

        // Retrieve the specified ModelMBeanOperationInfo
        ModelMBeanOperationInfo mmoinfo = info.getOperation("initialize");
        assertNotNull("Found HttpConnector initialize info", mmoinfo);

        // Get the Descriptor
        Descriptor desc = mmoinfo.getDescriptor();
        assertNotNull("Found HttpConnector initialize descriptor", desc);

        // Check the configured fields
        checkDescriptor(desc, "field1", "HttpConnector.initialize/field1");
        checkDescriptor(desc, "field2", "HttpConnector.initialize/field2");

    }


    /**
     * Test registry creation.
     */
    public void testRegistryCreation() {

        String names[] = null;

        System.out.println("Registered managed beans:");
        names = registry.findManagedBeans();
        for (int i = 0; i < names.length; i++)
            System.out.println("  " + names[i]);
        System.out.println("-------------------------");

        System.out.println("Registered managed beans for Containers:");
        names = registry.findManagedBeans("org.apache.catalina.Container");
        for (int i = 0; i < names.length; i++)
            System.out.println("  " + names[i]);
        System.out.println("-------------------------");

    }


    // ------------------------------------------------------ Protected Methods


    // Check presence of an appropriate name/value pair in the descriptor
    protected void checkDescriptor(Descriptor desc, String name,
                                   Object value) {

        String names[] = desc.getFieldNames();
        boolean found = false;
        for (int i = 0; i < names.length; i++) {
            if (name.equals(names[i])) {
                found = true;
                break;
            }
        }
        assertTrue("Found name " + name, found);
        assertEquals("Correct name " + name + " value",
                     value,
                     desc.getFieldValue(name));

    }


    // Check presence of an appropriate FieldInfo
    protected void checkField(List fields, String name, Object value) {

        int n = fields.size();
        for (int i = 0; i < n; i++) {
            FieldInfo field = (FieldInfo) fields.get(i);
            if (name.equals(field.getName())) {
                assertEquals("name=" + name + " value",
                             value,
                             field.getValue());
                return;
            }
        }
        fail("Cannot find field name=" + name + " and value=" + value);

    }


}
