package org.apache.commons.modeler.modules;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.commons.modeler.util.DomUtil;
import org.apache.commons.modeler.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.*;
import javax.management.loading.MLet;
import javax.xml.transform.TransformerException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;


/**
 * This mbean will load an extended mlet file ( similar in syntax with jboss ).
 * It'll keep track of all attribute changes and update the file when attributes
 * change. 
 */
public interface MbeansSourceMBean 
{
    /** Set the source to be used to load the mbeans
     * 
     * @param source File or URL
     */ 
    public void setSource( Object source );
    
    public Object getSource();
    
    /** Return the list of loaded mbeans names
     * 
     * @return List of ObjectName
     */ 
    public List getMBeans();

    /** Load the mbeans from the source. Called automatically on init() 
     * 
     * @throws Exception
     */ 
    public void load() throws Exception;
    
    /** Call the init method on all mbeans. Will call load if not done already
     * 
     * @throws Exception
     */ 
    public void init() throws Exception;

    /** Save the file.
     */ 
    public void save();
}
