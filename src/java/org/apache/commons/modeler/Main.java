/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/java/org/apache/commons/modeler/Main.java,v 1.1 2002/12/29 18:01:42 costin Exp $
 * $Revision: 1.1 $
 * $Date: 2002/12/29 18:01:42 $
 *
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


package org.apache.commons.modeler;


import org.apache.commons.modeler.util.IntrospectionUtils;

import java.io.FileInputStream;


/**
 *  Small main that loads mbeans.
 *
 *  Requires: commons-logging-api.jar, jaxp ( including DOM ), jmx
 *
 *  Arguments:
 *   -file FILE
 *       Will load mbeans from the file
 *   -type TYPE
 *       Type of the mbeans file
 *
 * @author Costin Manolache
 */

public class Main
{
    String file;
    String home;
    String type="Mbeans";

    public void setFile( String f ) {
        this.file=f;
    }

    // shortcut
    public void setF( String f ) {
        this.file=f;
    }

    public void setType( String type ) {
        this.type=type;
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

        reg.loadDescriptors(file, type, new FileInputStream( file ));
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
