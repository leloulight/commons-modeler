/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.commons.modeler;


import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.modeler.util.IntrospectionUtils;


/**
 *  Small main that loads mbeans.
 *
 *  Requires: commons-logging-api.jar, jaxp ( including DOM ), jmx
 *
 *  Arguments:
 *   -file FILE
 *       Will load mbeans from the file
 *
 * @author Costin Manolache
 */

public class Main
{
    String file;
    String home;

    public void setFile( String f ) {
        this.file=f;
    }

    // shortcut
    public void setF( String f ) {
        this.file=f;
    }

    public void execute( )
        throws Exception
    {
        if( home==null ) {
            home=IntrospectionUtils.guessInstall("install.dir", "home.dir",
                "commons-modeler.jar", "org.apache.commons.modeler.Main");
        }

        if( file==null ) throw new Exception( "No file, use -file file.xml");

        Registry reg=Registry.getRegistry();
        File fileF=new File( file );
        URL url=new URL("file", null, fileF.getAbsolutePath());

        // Load the mbeans defined in the file and set all
        // attributes
        List mbeans=reg.loadMBeans( url, null);
        reg.invoke(mbeans, "init", false);
        reg.invoke(mbeans, "start", false);
    }

    public static void main( String args[] ) {
        try {
            Main main=new Main();
            IntrospectionUtils.processArgs(main, args);

            main.execute();
        } catch( Exception ex ) {
            ex.printStackTrace();
        }

    }
}
