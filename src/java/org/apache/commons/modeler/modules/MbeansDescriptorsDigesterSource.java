package org.apache.commons.modeler.modules;

import org.apache.commons.digester.Digester;
import org.apache.commons.modeler.Registry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.net.URL;

public class MbeansDescriptorsDigesterSource extends Registry.DescriptorSource
{
    private static Log log =
            LogFactory.getLog(MbeansDescriptorsDigesterSource.class);

    public void loadDescriptors( Registry registry, String location,
                                 String type, Object source)
            throws Exception
    {
        InputStream stream=(InputStream)source;
        long t1=System.currentTimeMillis();

        Digester digester = new Digester();
        digester.setNamespaceAware(false);
        digester.setValidating(false);
        URL url = registry.getClass().getResource
                ("/org/apache/commons/modeler/mbeans-descriptors.dtd");
        digester.register
                ("-//Apache Software Foundation//DTD Model MBeans Configuration File",
                        url.toString());

        // Push our registry object onto the stack
        digester.push(registry);

        // Configure the parsing rules
        digester.addObjectCreate
                ("mbeans-descriptors/mbean",
                        "org.apache.commons.modeler.ManagedBean");
        digester.addSetProperties
                ("mbeans-descriptors/mbean");
        digester.addSetNext
                ("mbeans-descriptors/mbean",
                        "addManagedBean",
                        "org.apache.commons.modeler.ManagedBean");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/attribute",
                        "org.apache.commons.modeler.AttributeInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/attribute");
        digester.addSetNext
                ("mbeans-descriptors/mbean/attribute",
                        "addAttribute",
                        "org.apache.commons.modeler.AttributeInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/constructor",
                        "org.apache.commons.modeler.ConstructorInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/constructor");
        digester.addSetNext
                ("mbeans-descriptors/mbean/constructor",
                        "addConstructor",
                        "org.apache.commons.modeler.ConstructorInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/constructor/parameter",
                        "org.apache.commons.modeler.ParameterInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/constructor/parameter");
        digester.addSetNext
                ("mbeans-descriptors/mbean/constructor/parameter",
                        "addParameter",
                        "org.apache.commons.modeler.ParameterInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/notification",
                        "org.apache.commons.modeler.NotificationInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/notification");
        digester.addSetNext
                ("mbeans-descriptors/mbean/notification",
                        "addNotification",
                        "org.apache.commons.modeler.NotificationInfo");
        digester.addCallMethod
                ("mbeans-descriptors/mbean/notification/notification-type",
                        "addNotifType", 0);

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/operation",
                        "org.apache.commons.modeler.OperationInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/operation");
        digester.addSetNext
                ("mbeans-descriptors/mbean/operation",
                        "addOperation",
                        "org.apache.commons.modeler.OperationInfo");

        digester.addObjectCreate
                ("mbeans-descriptors/mbean/operation/parameter",
                        "org.apache.commons.modeler.ParameterInfo");
        digester.addSetProperties
                ("mbeans-descriptors/mbean/operation/parameter");
        digester.addSetNext
                ("mbeans-descriptors/mbean/operation/parameter",
                        "addParameter",
                        "org.apache.commons.modeler.ParameterInfo");

        // Process the input file to configure our registry
        try {
            digester.parse(stream);
        } catch (Exception e) {
            log.error("Error digesting Registry data", e);
            throw e;
        }
        long t2=System.currentTimeMillis();
//        if( t2-t1 > 500 )
        log.info("Loaded registry information (digester) " + ( t2 - t1 ) + " ms");
    }
}
