package org.apache.commons.modeler.modules;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.commons.modeler.util.DomUtil;
import org.apache.commons.modeler.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.*;
import java.io.InputStream;


/** This will create mbeans based on a config file.
 *  The format is an extended version of MLET.
 *
 */
public class MbeansSource extends Registry.DescriptorSource
{
    private static Log log = LogFactory.getLog(MbeansSource.class);

    public void loadDescriptors( Registry registry, String location,
                                 String type, Object source)
        throws Exception
    {
        try {
            InputStream stream=(InputStream)source;
            long t1=System.currentTimeMillis();
            Document doc=DomUtil.readXml(stream);

            // We don't care what the root node is.
            Node descriptorsN=doc.getDocumentElement();

            if( descriptorsN == null ) {
                log.error("No descriptors found");
                return;
            }

            Node firstMbeanN=DomUtil.getChild(descriptorsN, null);

            if( firstMbeanN==null ) {
                // maybe we have a single mlet
                if( log.isDebugEnabled() )
                    log.debug("No child " + descriptorsN);
                firstMbeanN=descriptorsN;
            }

            MBeanServer server=(MBeanServer)Registry.getServer();

            // We'll process all nodes at the same level.
            for (Node mbeanN = firstMbeanN; mbeanN != null;
                 mbeanN= DomUtil.getNext(mbeanN, null, Node.ELEMENT_NODE))
            {
                String nodeName=mbeanN.getNodeName();

                if( "mbean".equals(nodeName) || "MLET".equals(nodeName)) {
                    String code=DomUtil.getAttribute( mbeanN, "code" );
                    String objectName=DomUtil.getAttribute( mbeanN, "objectName" );
                    if( objectName==null ) {
                        objectName=DomUtil.getAttribute( mbeanN, "name" );
                    }

                    if( log.isDebugEnabled())
                        log.debug( "Processing mbean objectName=" + objectName +
                                " code=" + code);

                    // args can be grouped in constructor or direct childs
                    Node constructorN=DomUtil.getChild(mbeanN, "constructor");
                    if( constructorN == null ) constructorN=mbeanN;

                    processArg(constructorN);

                    try {
                        ObjectName oname=new ObjectName(objectName);
                        server.createMBean(code, oname);
                        // XXX Arguments, loader !!!
                    } catch( Exception ex ) {
                        log.error( "Error creating mbean " + objectName, ex);
                    }

                    Node firstAttN=DomUtil.getChild(mbeanN, "attribute");
                    for (Node descN = firstAttN; descN != null;
                         descN = DomUtil.getNext( descN ))
                    {
                        processAttribute(server, descN, objectName);
                    }

                } else if("classpath".equals(nodeName) ) {
                    // TODO support classpath ( standard )
                } else if("jmx-attribute".equals(nodeName) ) {
                    // <jmx-attribute objectName="..." name="..." value="..."/>
                    String objectName=DomUtil.getAttribute(mbeanN, "objectName");
                    processAttribute(server, mbeanN, objectName);
                } else if("jmx-operation".equals(nodeName) ) {
                    String name=DomUtil.getAttribute(mbeanN, "objectName");
                    if( name==null )
                        name=DomUtil.getAttribute(mbeanN, "name");

                    String operation=DomUtil.getAttribute(mbeanN, "operation");

                    if( log.isDebugEnabled())
                        log.debug( "Processing invoke objectName=" + name +
                                " code=" + operation);
                    try {
                        ObjectName oname=new ObjectName(name);

                        processArg( mbeanN );
                        server.invoke( oname, operation, null, null);
                    } catch (Exception e) {
                        log.error( "Error in invoke " + name + " " + operation);
                    }
                }

                ManagedBean managed=new ManagedBean();
                DomUtil.setAttributes(managed, mbeanN);
                Node firstN;

                // process attribute info
                firstN=DomUtil.getChild( mbeanN, "attribute");
                for (Node descN = firstN; descN != null;
                     descN = DomUtil.getNext( descN ))
                {
                    AttributeInfo ci=new AttributeInfo();
                    DomUtil.setAttributes(ci, descN);
                    managed.addAttribute( ci );
                }

            }

            long t2=System.currentTimeMillis();
            log.info( "Reading mbeans  " + (t2-t1));
        } catch( Exception ex ) {
            log.error( "Error reading mbeans ", ex);
        }
    }

    private void processAttribute(MBeanServer server,
                                  Node descN, String objectName ) {
        String attName=DomUtil.getAttribute(descN, "name");
        String value=DomUtil.getAttribute(descN, "value");
        String type=DomUtil.getAttribute(descN, "type");
        if( value==null ) {
            // The value may be specified as CDATA
            value=DomUtil.getContent(descN);
        }
        try {
            if( log.isDebugEnabled())
                log.debug("Set attribute " + objectName + " " + attName +
                        " " + value);
            ObjectName oname=new ObjectName(objectName);
            Object valueO=getValueObject( value, type);
            server.setAttribute(oname, new Attribute(attName, valueO));
        } catch( Exception ex) {
            log.error("Error processing attribute " + objectName + " " +
                    attName + " " + value, ex);
        }

    }

    // XXX We should know the type from the mbean metadata
    private Object getValueObject( String valueS, String type )
            throws MalformedObjectNameException
    {
        if( type==null )
            return valueS;
        if( "int".equals( type ) || "java.lang.Integer".equals(type) ) {
            return new Integer( valueS);
        }
        if( "ObjectName".equals( type ) || "javax.management.ObjectName".equals(type) ) {
            return new ObjectName( valueS);
        }
        return valueS;
    }

    private void processArg(Node mbeanN) {
        Node firstArgN=DomUtil.getChild(mbeanN, "arg" );
        // process all args
        for (Node argN = firstArgN; argN != null;
             argN = DomUtil.getNext( argN ))
        {
            String type=DomUtil.getAttribute(argN, "type");
            String value=DomUtil.getAttribute(argN, "value");
            if( value==null ) {
                // The value may be specified as CDATA
                value=DomUtil.getContent(argN);
            }


        }
    }
}
