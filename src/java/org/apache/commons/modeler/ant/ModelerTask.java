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

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;

/**
 * Like MLETTask, but it wraps the bean in a BaseModelMBean.
 *
 */
public class ModelerTask extends MLETTask {

    private static Log log = LogFactory.getLog(ModelerTask.class);

    public ModelerTask() {
    }

    public void execute() throws BuildException {
        try {
            Arg codeArg=new Arg();
            codeArg.setType("java.lang.String");
            codeArg.setValue( code );
            if( args==null) args=new ArrayList();
            args.add(0, codeArg);
            super.execute();
//            super.bindJmx(objectName,
//                    "org.apache.commons.modeler.BaseModelMBean",
//                    null,
//                    args );
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
