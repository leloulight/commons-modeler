/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.commons.modeler.ant;


import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;
import org.apache.tools.ant.BuildException;

/**
 * Load descriptors into registry.
 *
 * @author Costin Manolache
 */
public final class RegistryTask  {
    private static Log log = LogFactory.getLog(RegistryTask.class);

    public RegistryTask() {
    }

    String resource;
    String file;
    String type="MbeansDescriptorsDOMSource";

    /** Set the resource type that will be loaded
     *
     * @param type
     */
    public void setType( String type ) {
        this.type=type;
    }

    public void setFile( String file ) {
        this.file=file;
    }

    public void setResource( String res ) {
        this.resource=res;
    }

    String outFile;

    public void setOut( String outFile ) {
        this.outFile=outFile;
    }

    public void execute() throws Exception {
        URL url=null;

        if( resource != null ) {
            url=this.getClass().getClassLoader().getResource(resource);
        } else if( file != null ) {
            File f=new File(file);
            url=new URL("file", null, f.getAbsolutePath());
        } else {
            throw new BuildException( "Resource or file attribute required");
        }

        Registry.getRegistry().loadDescriptors( type, url, null);

        if( outFile !=null ) {
            FileOutputStream fos=new FileOutputStream(outFile);
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            Registry reg=Registry.getRegistry();
            String beans[]=reg.findManagedBeans();
            ManagedBean mbeans[]=new ManagedBean[beans.length];
            for( int i=0; i<beans.length; i++ ) {
                mbeans[i]=reg.findManagedBean(beans[i]);
            }
            oos.writeObject( mbeans );
            oos.flush();
            oos.close();
            fos.close();
        }
    }
}
 