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

import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.tools.ant.Task;

/**
 * Set mbean properties.
 *
 */
public class JmxInvoke extends Task {
    String objectName;

    String method;
    Vector args;

    public JmxInvoke() {

    }

    public void setObjectName(String name) {
        this.objectName = name;
    }

    public void setOperation(String method) {
            this.method = method;
    }

    public void execute() {
        try {
            MBeanServer server=(MBeanServer)project.getReference("jmx.server");

            if (server == null) {
                if( MBeanServerFactory.findMBeanServer(null).size() > 0 ) {
                    server=(MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
                } else {
                    System.out.println("Creating mbean server");
                    server=MBeanServerFactory.createMBeanServer();
                }
                project.addReference("jmx.server", server);
            }

            ObjectName oname=new ObjectName(objectName);

            if( args==null ) {
                server.invoke(oname, method, null, null);
            } else {
                // XXX Use the loader ref, if any
                Object argsA[]=new Object[ args.size()];
                String sigA[]=new String[args.size()];
                for( int i=0; i<args.size(); i++ ) {
                    Arg arg=(Arg)args.elementAt(i);
                    if( arg.type==null )
                        arg.type="java.lang.String";
                    sigA[i]=arg.getType();
                    argsA[i]=arg.getValue();
                    // XXX Deal with not string types - IntrospectionUtils
                }
                server.invoke(oname, method, argsA, sigA);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
