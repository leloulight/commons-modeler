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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.Registry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Group a set of mbeans in a service, and perform actions on
 * it.
 *
 *
 */
public class ServiceTask extends Task {
    private static Log log = LogFactory.getLog(ServiceTask.class);
    List mbeans=new ArrayList();
    String action;
    String refId;

    public ServiceTask() {
    }

    public void addMbean(MLETTask mbean) {
        mbeans.add(mbean);
    }

    public List getMbeans() {
        return mbeans;
    }

    /** Set action to be executed on the mbean collection. 
     * If null - we'll perform init and start.
     * 
     * @param action
     */ 
    public void setAction(String action) {
        this.action=action;
    }

    /** Perform the action on a previously declared service
     * 
     */ 
    public void setRefId( String ref ) {
        this.refId=ref;
    }

    public void execute() throws BuildException {
        try {
            Registry reg=Registry.getRegistry();

            if( refId != null ) {
                ServiceTask stask=(ServiceTask)project.getReference(refId);
            }
            // create the mbeans
            List onames=new ArrayList();

            for( int i=0; i<mbeans.size(); i++ ) {
                MLETTask mbean=(MLETTask)mbeans.get(i);
                mbean.execute();
                onames.add( mbean.getObjectName());
            }

            if( action==null ) {
                // default: init and start
                reg.invoke(onames, "init", false);
                reg.invoke(onames, "start", false);
            } else {
                reg.invoke(onames, action, false );
            }

        } catch(Exception ex) {
            log.error("Error ", ex);
        }
    }
}
