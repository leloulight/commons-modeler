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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.loading.MLet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

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
    private static Log log = LogFactory.getLog(MLETTask.class);
    String code;
    String archive;
    String codebase;
    String objectName;
    ObjectName oname;

    List args=new ArrayList();
    List attributes=new ArrayList();

    // ant specific
    String loaderRef; // class loader ref

    public MLETTask() {
    }

    public void addArg(Arg arg ) {
        args.add(arg);
    }

    public void addAttribute( JmxSet arg ) {
        attributes.add( arg );
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
        this.objectName = name;
    }

    MBeanServer server;

    public MBeanServer getMBeanServer() {
        if( server!= null ) return server;

        server=(MBeanServer)project.getReference("jmx.server");

        if (server != null) return server;

        try {
            if( MBeanServerFactory.findMBeanServer(null).size() > 0 ) {
                server=(MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
            } else {
                server=MBeanServerFactory.createMBeanServer();

                // Register a loader that will be find ant classes.
                ObjectName defaultLoader= new ObjectName("modeler-ant",
                        "loader", "ant");
                MLet mlet=new MLet( new URL[0], this.getClass().getClassLoader());
                server.registerMBean(mlet, defaultLoader);

                if( log.isDebugEnabled())
                    log.debug("Creating mbean server and loader "+ mlet +
                            " " + this.getClass().getClassLoader());
            }
            project.addReference("jmx.server", server);

            // Create the MLet object
        } catch( JMException ex ) {
            log.error("Error creating server", ex);
        }

        if( log.isDebugEnabled()) log.debug("Using Mserver " + server );

        return server;
    }

    boolean modeler=false;

    public void setModeler(boolean modeler) {
        this.modeler = modeler;
    }

    protected void bindJmx(String objectName, String code,
                           String arg0, List args)
            throws Exception
    {
        MBeanServer server=getMBeanServer();
        oname=new ObjectName( objectName );
        if( modeler ) {
            Arg codeArg=new Arg();
            codeArg.setType("java.lang.String");
            codeArg.setValue( code );
            if( args==null) args=new ArrayList();
            args.add(0, codeArg);
            code="org.apache.commons.modeler.BaseModelMBean";
        }

        Object argsA[]=new Object[ args.size()];
        String sigA[]=new String[args.size()];
        for( int i=0; i<args.size(); i++ ) {
            Arg arg=(Arg)args.get(i);
            if( arg.type==null )
                arg.type="java.lang.String";
            sigA[i]=arg.getType();
            argsA[i]=arg.getValue();
            // XXX Deal with not string types - IntrospectionUtils
        }

        // XXX Use the loader ref, if any
        if( args.size()==0 ) {
            server.createMBean(code, oname);
        } else {
            server.createMBean(code, oname, argsA, sigA );
        }
        log.debug( "Created MBEAN " + oname + " " + code);
    }

    public ObjectName getObjectName() {
        return oname;
    }

    public void execute() throws BuildException {
        try {
            // create the mbean
            bindJmx( objectName, code, null, args);

            // process attributes
            for( int i=0; i<attributes.size(); i++ ) {
                JmxSet att=(JmxSet)attributes.get(i);
                att.setObjectName( oname );
                log.info("Setting attribute " + oname + " " + att.getName());
                att.execute();
            }
        } catch(Exception ex) {
            log.error("Can't create mbean " + objectName, ex);
        }
    }

}
