package org.apache.commons.modeler.modules;

import org.apache.commons.modeler.Registry;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/** Source for descriptor data. More sources can be added.
 *
 */
public class ModelerSource {

    /** Load data, returns a list of items. 
     * 
     * @param registry
     * @param location
     * @param type
     * @param source Introspected object or some other source
     * @return
     * @throws Exception
     */ 
    public List loadDescriptors( Registry registry, String location,
                                 String type, Object source)
            throws Exception
    {
        // TODO
        return null;
    }
    
    /** Callback from the BaseMBean to notify that an attribute has changed.
     * Can be used to implement persistence.
     * 
     * @param oname
     * @param name
     * @param value
     */ 
    public void updateField( ObjectName oname, String name, 
                             Object value ) {
        // nothing by default 
    }

    public void store() {
        // nothing
    }
    
}
