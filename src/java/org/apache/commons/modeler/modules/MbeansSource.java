package org.apache.commons.modeler.modules;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.commons.modeler.util.DomUtil;
import org.apache.commons.modeler.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.InputStream;


/** This will create mbeans based on a config file.
 *  The format is an extended version of MLET.
 *
 */
public class MbeansSource extends Registry.DescriptorSource
{
    private static Log log = LogFactory.getLog(MbeansSource.class);

    public void loadDescriptors( Registry registry, String location,
                                 String type, InputStream stream)
        throws Exception
    {
        try {
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
                    String name=DomUtil.getAttribute( mbeanN, "name" );

                    // args can be grouped in constructor or direct childs
                    Node constructorN=DomUtil.getChild(mbeanN, "constructor");
                    if( constructorN == null ) constructorN=mbeanN;

                    processArg(constructorN);

                    Node firstAttN=DomUtil.getChild(mbeanN, "attribute");
                    for (Node descN = firstAttN; descN != null;
                         descN = DomUtil.getNext( descN ))
                    {
                        String attName=DomUtil.getAttribute(descN, "name");
                        String value=DomUtil.getAttribute(descN, "value");
                        if( value==null ) {
                            // The value may be specified as CDATA
                            value=DomUtil.getContent(descN);
                        }
                    }

                } else if("mbeans-descriptors".equals(nodeName) ) {
                    // TODO support descriptors fragments inside <mbeans>
                } else if("classpath".equals(nodeName) ) {
                    // TODO support classpath ( standard )
                } else if("jmx-attribute".equals(nodeName) ) {
                    // Do we need this ?
                } else if("jmx-operation".equals(nodeName) ) {
                    String name=DomUtil.getAttribute(mbeanN, "name");
                    String operation=DomUtil.getAttribute(mbeanN, "operation");

                    ObjectName oname=new ObjectName(name);

                    processArg( mbeanN );
                    server.invoke( oname, operation, null, null);
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
