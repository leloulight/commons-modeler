/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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


package org.apache.commons.modeler.modules;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.commons.modeler.util.DomUtil;
import org.apache.commons.modeler.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;


public class MbeansDescriptorsDOMSource extends ModelerSource
{
    private static Log log = LogFactory.getLog(MbeansDescriptorsDOMSource.class);

    Registry registry;
    String location;
    String type;
    Object source;

    public void setRegistry(Registry reg) {
        this.registry=reg;
    }

    public void setLocation( String loc ) {
        this.location=loc;
    }

    /** Used if a single component is loaded
     *
     * @param type
     */
    public void setType( String type ) {
       this.type=type;
    }

    public void setSource( Object source ) {
        this.source=source;
    }

    public void loadDescriptors( Registry registry, String location,
                                 String type, Object source)
            throws Exception
    {
        setRegistry(registry);
        setLocation(location);
        setType(type);
        setSource(source);
        execute();
    }

    public void execute() throws Exception {
        if( registry==null ) registry=Registry.getRegistry();

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

            // Process each <mbean> element
            for (Node mbeanN = firstMbeanN; mbeanN != null;
                 mbeanN= DomUtil.getNext(mbeanN))
            {

                // Create a new managed bean info
                ManagedBean managed=new ManagedBean();
                DomUtil.setAttributes(managed, mbeanN);
                Node firstN;

                // Process descriptor subnode
                Node mbeanDescriptorN =
                    DomUtil.getChild(mbeanN, "descriptor");
                if (mbeanDescriptorN != null) {
                    Node firstFieldN =
                        DomUtil.getChild(mbeanDescriptorN, "field");
                    for (Node fieldN = firstFieldN; fieldN != null;
                         fieldN = DomUtil.getNext(fieldN)) {
                        FieldInfo fi = new FieldInfo();
                        DomUtil.setAttributes(fi, fieldN);
                        managed.addField(fi);
                    }
                }

                // process attribute nodes
                firstN=DomUtil.getChild( mbeanN, "attribute");
                for (Node descN = firstN; descN != null;
                     descN = DomUtil.getNext( descN ))
                {

                    // Create new attribute info
                    AttributeInfo ai=new AttributeInfo();
                    DomUtil.setAttributes(ai, descN);

                    // Process descriptor subnode
                    Node descriptorN =
                        DomUtil.getChild(descN, "descriptor");
                    if (descriptorN != null) {
                        Node firstFieldN =
                            DomUtil.getChild(descriptorN, "field");
                        for (Node fieldN = firstFieldN; fieldN != null;
                             fieldN = DomUtil.getNext(fieldN)) {
                            FieldInfo fi = new FieldInfo();
                            DomUtil.setAttributes(fi, fieldN);
                            ai.addField(fi);
                        }
                    }

                    // Add this info to our managed bean info
                    managed.addAttribute( ai );
                    if (log.isTraceEnabled()) {
                        log.trace("Create attribute " + ai);
                    }

                }

                // process constructor nodes
                firstN=DomUtil.getChild( mbeanN, "constructor");
                for (Node descN = firstN; descN != null;
                     descN = DomUtil.getNext( descN )) {

                    // Create new constructor info
                    ConstructorInfo ci=new ConstructorInfo();
                    DomUtil.setAttributes(ci, descN);

                    // Process descriptor subnode
                    Node firstDescriptorN =
                        DomUtil.getChild(descN, "descriptor");
                    if (firstDescriptorN != null) {
                        Node firstFieldN =
                            DomUtil.getChild(firstDescriptorN, "field");
                        for (Node fieldN = firstFieldN; fieldN != null;
                             fieldN = DomUtil.getNext(fieldN)) {
                            FieldInfo fi = new FieldInfo();
                            DomUtil.setAttributes(fi, fieldN);
                            ci.addField(fi);
                        }
                    }

                    // Process parameter subnodes
                    Node firstParamN=DomUtil.getChild( descN, "parameter");
                    for (Node paramN = firstParamN;  paramN != null;
                         paramN = DomUtil.getNext(paramN))
                    {
                        ParameterInfo pi=new ParameterInfo();
                        DomUtil.setAttributes(pi, paramN);
                        ci.addParameter( pi );
                    }

                    // Add this info to our managed bean info
                    managed.addConstructor( ci );
                    if (log.isTraceEnabled()) {
                        log.trace("Create constructor " + ci);
                    }

                }

                // process notification nodes
                firstN=DomUtil.getChild( mbeanN, "notification");
                for (Node descN = firstN; descN != null;
                     descN = DomUtil.getNext( descN ))
                {

                    // Create new notification info
                    NotificationInfo ni=new NotificationInfo();
                    DomUtil.setAttributes(ni, descN);

                    // Process descriptor subnode
                    Node firstDescriptorN =
                        DomUtil.getChild(descN, "descriptor");
                    if (firstDescriptorN != null) {
                        Node firstFieldN =
                            DomUtil.getChild(firstDescriptorN, "field");
                        for (Node fieldN = firstFieldN; fieldN != null;
                             fieldN = DomUtil.getNext(fieldN)) {
                            FieldInfo fi = new FieldInfo();
                            DomUtil.setAttributes(fi, fieldN);
                            ni.addField(fi);
                        }
                    }

                    // Process notification-type subnodes
                    Node firstParamN=DomUtil.getChild( descN, "notification-type");
                    for (Node paramN = firstParamN;  paramN != null;
                         paramN = DomUtil.getNext(paramN))
                    {
                        ni.addNotifType( DomUtil.getContent(paramN) );
                    }

                    // Add this info to our managed bean info
                    managed.addNotification( ni );
                    if (log.isTraceEnabled()) {
                        log.trace("Created notification " + ni);
                    }

                }

                // process operation nodes
                firstN=DomUtil.getChild( mbeanN, "operation");
                for (Node descN = firstN; descN != null;
                     descN = DomUtil.getNext( descN ))

                {

                    // Create new operation info
                    OperationInfo oi=new OperationInfo();
                    DomUtil.setAttributes(oi, descN);

                    // Process descriptor subnode
                    Node firstDescriptorN =
                        DomUtil.getChild(descN, "descriptor");
                    if (firstDescriptorN != null) {
                        Node firstFieldN =
                            DomUtil.getChild(firstDescriptorN, "field");
                        for (Node fieldN = firstFieldN; fieldN != null;
                             fieldN = DomUtil.getNext(fieldN)) {
                            FieldInfo fi = new FieldInfo();
                            DomUtil.setAttributes(fi, fieldN);
                            oi.addField(fi);
                        }
                    }

                    // Process parameter subnodes
                    Node firstParamN=DomUtil.getChild( descN, "parameter");
                    for (Node paramN = firstParamN;  paramN != null;
                         paramN = DomUtil.getNext(paramN))
                    {
                        ParameterInfo pi=new ParameterInfo();
                        DomUtil.setAttributes(pi, paramN);
                        if( log.isTraceEnabled())
                            log.trace("Add param " + pi.getName());
                        oi.addParameter( pi );
                    }

                    // Add this info to our managed bean info
                    managed.addOperation( oi );
                    if( log.isTraceEnabled()) {
                        log.trace("Create operation " + oi);
                    }

                }

                // Add the completed managed bean info to the registry
                registry.addManagedBean(managed);

            }

            long t2=System.currentTimeMillis();
            log.info( "Reading descriptors ( dom ) " + (t2-t1));
        } catch( Exception ex ) {
            log.error( "Error reading descriptors ", ex);
        }
    }
}
