/*
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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
package org.apache.commons.modeler.util;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.*;


/**
 *  Few simple utils to read DOM
 *
 * @author Costin Manolache
 */
public class DomUtil {
    private static org.apache.commons.logging.Log log=
        org.apache.commons.logging.LogFactory.getLog( DomUtil.class );

    // -------------------- DOM utils --------------------

    /** Get the trimed text content of a node or null if there is no text
     */
    public static String getContent(Node n ) {
        if( n==null ) return null;
        Node n1=DomUtil.getChild(n, Node.TEXT_NODE);

        if( n1==null ) return null;

        String s1=n1.getNodeValue();
        return s1.trim();
    }

    /** Get the first element child.
     * @param parent lookup direct childs
     * @param name name of the element. If null return the first element.
     */
    public static Node getChild( Node parent, String name ) {
        if( parent==null ) return null;
        Node first=parent.getFirstChild();
        if( first==null ) return null;

        for (Node node = first; node != null;
             node = node.getNextSibling()) {
            //System.out.println("getNode: " + name + " " + node.getNodeName());
            if( node.getNodeType()!=Node.ELEMENT_NODE)
                continue;
            if( name != null &&
                name.equals( node.getNodeName() ) ) {
                return node;
            }
            if( name == null ) {
                return node;
            }
        }
        return null;
    }

    public static String getAttribute(Node element, String attName ) {
        NamedNodeMap attrs=element.getAttributes();
        if( attrs==null ) return null;
        Node attN=attrs.getNamedItem(attName);
        if( attN==null ) return null;
        return attN.getNodeValue();
    }

    public static void setAttribute(Node node, String attName, String val) {
        NamedNodeMap attributes=node.getAttributes();
        Node attNode=node.getOwnerDocument().createAttribute(attName);
        attNode.setNodeValue( val );
        attributes.setNamedItem(attNode);
    }
    
    public static void removeAttribute( Node node, String attName ) {
        NamedNodeMap attributes=node.getAttributes();
        attributes.removeNamedItem(attName);                
    }
    
    
    /** Set or replace the text value 
     */ 
    public static void setText(Node node, String val) {
        Node chld=DomUtil.getChild(node, Node.TEXT_NODE);
        if( chld == null ) {
            Node textN=node.getOwnerDocument().createTextNode(val);
            node.appendChild(textN);
            return;
        }
        // change the value
        chld.setNodeValue(val);           
    }

    /** Find the first direct child with a given attribute.
     * @param parent
     * @param elemName name of the element, or null for any 
     * @param attName attribute we're looking for
     * @param attVal attribute value or null if we just want any
     */ 
    public static Node findChildWithAtt(Node parent, String elemName,
                                        String attName, String attVal) {
        
        Node child=DomUtil.getChild(parent, Node.ELEMENT_NODE);
        if( attVal== null ) {
            while( child!= null &&
                    ( elemName==null || elemName.equals( child.getNodeName())) && 
                    DomUtil.getAttribute(child, attName) != null ) {
                child=getNext(child, elemName, Node.ELEMENT_NODE );
            }
        } else {
            while( child!= null && 
                    ( elemName==null || elemName.equals( child.getNodeName())) && 
                    ! attVal.equals( DomUtil.getAttribute(child, attName)) ) {
                child=getNext(child, elemName, Node.ELEMENT_NODE );
            }
        }
        return child;        
    }    
    

    /** Get the first child's content ( ie it's included TEXT node ).
     */
    public static String getChildContent( Node parent, String name ) {
        Node first=parent.getFirstChild();
        if( first==null ) return null;
        for (Node node = first; node != null;
             node = node.getNextSibling()) {
            //System.out.println("getNode: " + name + " " + node.getNodeName());
            if( name.equals( node.getNodeName() ) ) {
                return getContent( node );
            }
        }
        return null;
    }

    /** Get the first direct child with a given type
     */
    public static Node getChild( Node parent, int type ) {
        Node n=parent.getFirstChild();
        while( n!=null && type != n.getNodeType() ) {
            n=n.getNextSibling();
        }
        if( n==null ) return null;
        return n;
    }

    /** Get the next sibling with the same name and type
     */
    public static Node getNext( Node current ) {
        String name=current.getNodeName();
        int type=current.getNodeType();
        return getNext( current, name, type);
    }

    /** Return the next sibling with a given name and type
     */ 
    public static Node getNext( Node current, String name, int type) {
        Node first=current.getNextSibling();
        if( first==null ) return null;

        for (Node node = first; node != null;
             node = node.getNextSibling()) {
            
            if( type >= 0 && node.getNodeType() != type ) continue;
            //System.out.println("getNode: " + name + " " + node.getNodeName());
            if( name==null )
                return node;
            if( name.equals( node.getNodeName() ) ) {
                return node;
            }
        }
        return null;
    }

    public static class NullResolver implements EntityResolver {
        public InputSource resolveEntity (String publicId,
                                                   String systemId)
            throws SAXException, IOException
        {
            if( log.isTraceEnabled())
                log.trace("ResolveEntity: " + publicId + " " + systemId);
            return new InputSource(new StringReader(""));
        }
    }

    public static void setAttributes( Object o, Node parent)
    {
        NamedNodeMap attrs=parent.getAttributes();
        if( attrs==null ) return;

        for (int i=0; i<attrs.getLength(); i++ ) {
            Node n=attrs.item(i);
            String name=n.getNodeName();
            String value=n.getNodeValue();

            if( log.isTraceEnabled() )
                log.trace("Attribute " + parent.getNodeName() + " " +
                            name + "=" + value);
            try {
                IntrospectionUtils.setProperty(o, name, value);
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
    }

    /** Read XML as DOM.
     */
    public static Document readXml(InputStream is)
        throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilderFactory dbf =
            DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        //dbf.setCoalescing(true);
        //dbf.setExpandEntityReferences(true);

        DocumentBuilder db = null;
        db = dbf.newDocumentBuilder();
        db.setEntityResolver( new NullResolver() );

        // db.setErrorHandler( new MyErrorHandler());

        Document doc = db.parse(is);
        return doc;
    }

    public static void writeXml( Node n, OutputStream os )
            throws TransformerException
    {
        TransformerFactory tf=TransformerFactory.newInstance();
        //identity
        Transformer t=tf.newTransformer();
        t.transform(new DOMSource( n ), new StreamResult( os ));
    }
}
