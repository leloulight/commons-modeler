package org.apache.commons.modeler.mbeans;

import org.apache.commons.modeler.BaseModelMBean;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.AttributeInfo;
import org.apache.commons.modeler.OperationInfo;
import org.apache.commons.modeler.Registry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.List;
import java.util.HashMap;
import java.io.InputStream;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.Attribute;

/** Use the same metadata, except that we replace the attribute
 * get/set methods.
 */
class MBeanProxy extends BaseModelMBean {
    private static Log log = LogFactory.getLog(MBeanProxy.class);

    HashMap atts=new HashMap();

    SimpleRemoteConnector jkmx;

    public MBeanProxy(SimpleRemoteConnector jkmx, String code) throws Exception {
        this.jkmx=jkmx;
        initModelInfo(code);
    }

    /** Called by the connector - will update the value when a chunk of
     * data is received
     */
    protected void update( String name, String val ) {
        if( log.isTraceEnabled() )
            log.trace( "Updating " + oname + " " + name + " " + val);
        // XXX Conversions !!!
        atts.put( name, val);
    }

    public Object getAttribute(String name)
        throws AttributeNotFoundException, MBeanException,
            ReflectionException
    {
        // If we're stale - refresh values
        jkmx.refresh();
        return atts.get(name);
    }

    public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException, MBeanException,
        ReflectionException
    {
        try {
            jkmx.setAttribute(oname, attribute);
        } catch( Exception ex ) {
            throw new MBeanException(ex);
        }
    }

    public Object invoke(String name, Object params[], String signature[])
        throws MBeanException, ReflectionException
    {
        try {
            jkmx.invoke(oname, name, params, signature);
        } catch( Exception ex ) {
            throw new MBeanException(ex);
        }
        return null;
    }
}
