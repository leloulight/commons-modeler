package org.apache.commons.modeler.modules;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.commons.modeler.util.DomUtil;
import org.apache.commons.modeler.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.EOFException;
import java.net.URL;


public class MbeansDescriptorsSerSource extends ModelerSource
{
    private static Log log = LogFactory.getLog(MbeansDescriptorsSerSource.class);
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
        long t1=System.currentTimeMillis();
        try {
            InputStream stream=null;
            if( source instanceof URL ) {
                stream=((URL)source).openStream();
            }
            if( source instanceof InputStream ) {
                stream=(InputStream)source;
            }
            if( stream==null ) {
                throw new Exception( "Can't process "+ source);
            }
            ObjectInputStream ois=new ObjectInputStream(stream);
            Thread.currentThread().setContextClassLoader(ManagedBean.class.getClassLoader());
            Object obj=ois.readObject();
            //log.info("Reading " + obj);
            ManagedBean beans[]=(ManagedBean[])obj;
            // after all are read without error
            for( int i=0; i<beans.length; i++ ) {
                registry.addManagedBean(beans[i]);
            }

        } catch( Exception ex ) {
            log.error( "Error reading descriptors " + source + " " +  ex.toString(),
                    ex);
            throw ex;
        }
        long t2=System.currentTimeMillis();
        log.info( "Reading descriptors ( ser ) " + (t2-t1));
    }
}
