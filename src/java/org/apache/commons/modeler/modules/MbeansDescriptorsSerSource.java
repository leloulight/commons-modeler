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


public class MbeansDescriptorsSerSource extends Registry.DescriptorSource
{
    private static Log log = LogFactory.getLog(MbeansDescriptorsSerSource.class);

    public void loadDescriptors( Registry registry, String location,
                                 String type, Object source)
        throws Exception
    {
        long t1=System.currentTimeMillis();
        try {
            InputStream stream=(InputStream)source;
            ObjectInputStream ois=new ObjectInputStream(stream);
            Object obj=ois.readObject();
            log.info("Reading " + obj);
            ManagedBean beans[]=(ManagedBean[])obj;
            // after all are read without error
            Thread.currentThread().setContextClassLoader(ManagedBean.class.getClassLoader());
            for( int i=0; i<beans.length; i++ ) {
                registry.addManagedBean(beans[i]);
            }

        } catch( Exception ex ) {
            log.error( "Error reading descriptors " +  ex.toString());
            throw ex;
        }
        long t2=System.currentTimeMillis();
        log.info( "Reading descriptors ( ser ) " + (t2-t1));
    }
}
