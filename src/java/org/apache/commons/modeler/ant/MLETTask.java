/*
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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
