/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
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
 */

package org.apache.commons.modeler.ant;

import org.apache.tools.ant.*;
import javax.management.*;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.loading.MLet;
import java.util.*;
import java.net.URL;

/**
 * Load an MBean. The syntax is similar with the <mlet>, with few
 * ant-specific extensions.
 *
 * A separate classloader can be used, the mechanism is similar with
 * what taskdef is using.
 *
 * Note that mlet will use the arguments in the constructor.
 *
 *
 */
public class MLETTask extends Task {

    String code;
    String archive;
    String codebase;
    String name;

    Vector args;

    // ant specific
    String loaderRef; // class loader ref


    public static class Arg {
        String type;
        String value;

        public Arg() {
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public MLETTask() {
    }

    public void addArg(Arg arg ) {
        if( args==null ) args=new Vector();
        args.addElement(arg);
    }


    public void setCode(String code) {
        this.code = code;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public void setCodebase(String codebase) {
        this.codebase = codebase;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void execute() throws BuildException {
        try {
            ObjectName defaultLoader= new ObjectName("ant", "mlet", "default");
            MBeanServer server=(MBeanServer)project.getReference("jmx.server");

            if (server == null) {
                if( MBeanServerFactory.findMBeanServer(null).size() > 0 ) {
                    server=(MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
                } else {
                    server=MBeanServerFactory.createMBeanServer();
                    MLet mlet=new MLet( new URL[0], this.getClass().getClassLoader());
                    server.registerMBean(mlet, defaultLoader);
                    System.out.println("Creating mbean server and loader "+ mlet +
                            " " + this.getClass().getClassLoader());
                }
                project.addReference("jmx.server", server);
                // Create the MLet object
            }

            System.out.println("Using Mserver " + server );

            ObjectName oname=new ObjectName( name );
            if( args==null ) {
                // XXX Use the loader ref, if any
                server.createMBean(code, oname, defaultLoader);
            } else {
                // XXX Use the loader ref, if any
                Object argsA[]=new Object[ args.size()];
                String sigA[]=new String[args.size()];
                for( int i=0; i<args.size(); i++ ) {
                    Arg arg=(Arg)args.elementAt(i);
                    if( arg.type==null )
                        arg.type="java.lang.String";
                    sigA[i]=arg.type;
                    argsA[i]=arg.value;
                    // XXX Deal with not string types - IntrospectionUtils
                }
                server.createMBean(code, oname, defaultLoader, argsA, sigA );
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
