package org.apache.commons.modeler.modules;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.commons.modeler.util.DomUtil;
import org.apache.commons.modeler.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;


public class MbeansDescriptorsDOMSource extends Registry.DescriptorSource
{
    private static Log log = LogFactory.getLog(MbeansDescriptorsDOMSource.class);

    public void loadDescriptors( Registry registry, String location,
                                 String type, Object source)
        throws Exception
    {
        try {
            InputStream stream=(InputStream)source;
            long t1=System.currentTimeMillis();
            Document doc=DomUtil.readXml(stream);
            // Ignore for now the name of the root element
            Node descriptorsN=doc.getDocumentElement();
            //Node descriptorsN=DomUtil.getChild(doc, "mbeans-descriptors");
            if( descriptorsN == null ) {
                log.error("No descriptors found");
                return;
            }

            Node firstMbeanN=null;
            if( "mbean".equals( descriptorsN.getNodeName() ) ) {
                firstMbeanN=descriptorsN;
            } else {
                firstMbeanN=DomUtil.getChild(descriptorsN, "mbean");
            }

            if( firstMbeanN==null ) {
                log.error(" No mbean tags ");
                return;
            }

            for (Node mbeanN = firstMbeanN; mbeanN != null;
                 mbeanN= DomUtil.getNext(mbeanN))
            {
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
                // process constructor info
                firstN=DomUtil.getChild( mbeanN, "constructor");
                for (Node descN = firstN; descN != null;
                     descN = DomUtil.getNext( descN ))
                {
                    ConstructorInfo ci=new ConstructorInfo();
                    DomUtil.setAttributes(ci, descN);

                    Node firstParamN=DomUtil.getChild( descN, "parameter");
                    for (Node paramN = firstParamN;  paramN != null;
                         paramN = DomUtil.getNext(paramN))
                    {
                        ParameterInfo pi=new ParameterInfo();
                        DomUtil.setAttributes(pi, paramN);
                        ci.addParameter( pi );
                    }
                    managed.addConstructor( ci );
                }
                // process method info
                firstN=DomUtil.getChild( mbeanN, "operation");
                for (Node descN = firstN; descN != null;
                     descN = DomUtil.getNext( descN ))
                {
                    OperationInfo ci=new OperationInfo();
                    DomUtil.setAttributes(ci, descN);

                    Node firstParamN=DomUtil.getChild( descN, "parameter");
                    for (Node paramN = firstParamN;  paramN != null;
                         paramN = DomUtil.getNext(paramN))
                    {
                        ParameterInfo pi=new ParameterInfo();
                        DomUtil.setAttributes(pi, paramN);
                        if( log.isTraceEnabled())
                            log.trace("Add param " + pi.getName());
                        ci.addParameter( pi );
                    }
                    managed.addOperation( ci );
                    if( log.isTraceEnabled())
                        log.trace("Create operation " + ci.getName());
                }
                // process notificatio info
                firstN=DomUtil.getChild( mbeanN, "notification");
                for (Node descN = firstN; descN != null;
                     descN = DomUtil.getNext( descN ))
                {
                    NotificationInfo ci=new NotificationInfo();
                    DomUtil.setAttributes(ci, descN);
                    managed.addNotification( ci );

                    Node firstParamN=DomUtil.getChild( descN, "notification-type");
                    for (Node paramN = firstParamN;  paramN != null;
                         paramN = DomUtil.getNext(paramN))
                    {
                        ci.addNotifType( DomUtil.getContent(paramN) );
                    }
                }

                registry.addManagedBean(managed);
            }

            long t2=System.currentTimeMillis();
            log.info( "Reading descriptors ( dom ) " + (t2-t1));
        } catch( Exception ex ) {
            log.error( "Error reading descriptors ", ex);
        }
    }
}
