package org.apache.commons.modeler.modules;

import org.apache.commons.modeler.Registry;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/** Source for descriptor data. More sources can be added.
 *
 */
public class ModelerSource {

    public List loadDescriptors( Registry registry, String location,
                                 String type, Object source)
            throws Exception
    {
        // TODO
        return null;
    }
    
    // XXX We should know the type from the mbean metadata
    protected Object getValueObject( String valueS, String type )
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
}
