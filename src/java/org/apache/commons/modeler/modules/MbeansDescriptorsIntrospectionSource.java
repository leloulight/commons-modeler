/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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

package org.apache.commons.modeler.modules;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.AttributeInfo;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.OperationInfo;
import org.apache.commons.modeler.ParameterInfo;
import org.apache.commons.modeler.Registry;
import org.apache.commons.modeler.ConstructorInfo;

import javax.management.ObjectName;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class MbeansDescriptorsIntrospectionSource extends ModelerSource
{
    private static Log log = LogFactory.getLog(MbeansDescriptorsIntrospectionSource.class);

    Registry registry;
    String location;
    String type;
    Object source;
    List mbeans=new ArrayList();

    public void setRegistry(Registry reg) {
        this.registry=reg;
    }

    public void setLocation( String loc ) {
        this.location=loc;
    }

    /** Used if a single component is loaded
     *
     * @param type
     */
    public void setType( String type ) {
       this.type=type;
    }

    public void setSource( Object source ) {
        this.source=source;
    }

    public List loadDescriptors( Registry registry, String location,
                                 String type, Object source)
            throws Exception
    {
        setRegistry(registry);
        setLocation(location);
        setType(type);
        setSource(source);
        execute();
        return mbeans;
    }

    public void execute() throws Exception {
        if( registry==null ) registry=Registry.getRegistry();
        try {
            ManagedBean managed=createManagedBean(registry, null, (Class)source, type);
            if( managed==null ) return;
            managed.setName( type );

            mbeans.add(managed);

        } catch( Exception ex ) {
            log.error( "Error reading descriptors ", ex);
        }
    }



    // ------------ Implementation for non-declared introspection classes

    static Hashtable specialMethods=new Hashtable();
    static {
        specialMethods.put( "preDeregister", "");
        specialMethods.put( "postDeregister", "");
    }

    private static String strArray[]=new String[0];
    private static ObjectName objNameArray[]=new ObjectName[0];
    // createMBean == registerClass + registerMBean

    private static Class[] supportedTypes  = new Class[] {
        Boolean.class,
        Boolean.TYPE,
        Byte.class,
        Byte.TYPE,
        Character.class,
        Character.TYPE,
        Short.class,
        Short.TYPE,
        Integer.class,
        Integer.TYPE,
        Long.class,
        Long.TYPE,
        Float.class, 
        Float.TYPE,
        Double.class,
        Double.TYPE,
        String.class,
        strArray.getClass(),
        BigDecimal.class,
        BigInteger.class,
        ObjectName.class,
        objNameArray.getClass(),
        java.io.File.class,
    };
    
    /**
     * Check if this class is one of the supported types.
     * If the class is supported, returns true.  Otherwise,
     * returns false.
     * @param ret The class to check
     * @return boolean True if class is supported
     */ 
    private boolean supportedType(Class ret) {
        for (int i = 0; i < supportedTypes.length; i++) {
            if (ret == supportedTypes[i]) {
                return true;
            }
        }
        if (isBeanCompatible(ret)) {
            return true;
        }
        return false;
    }

    /**
     * Check if this class conforms to JavaBeans specifications.
     * If the class is conformant, returns true.
     *
     * @param javaType The class to check
     * @return boolean True if the class is compatible.
     */
    protected boolean isBeanCompatible(Class javaType) {
        // Must be a non-primitive and non array
        if (javaType.isArray() || javaType.isPrimitive()) {
            return false;
        }

        // Anything in the java or javax package that
        // does not have a defined mapping is excluded.
        if (javaType.getName().startsWith("java.") || 
            javaType.getName().startsWith("javax.")) {
            return false;
        }

        try {
            javaType.getConstructor(new Class[]{});
        } catch (java.lang.NoSuchMethodException e) {
            return false;
        }

        // Make sure superclass is compatible
        Class superClass = javaType.getSuperclass();
        if (superClass != null && 
            superClass != java.lang.Object.class && 
            superClass != java.lang.Exception.class && 
            superClass != java.lang.Throwable.class) {
            if (!isBeanCompatible(superClass)) {
                return false;
            }
        }
        return true;
    }
    
    /** 
     * Process the methods and extract 'attributes', methods, etc
     *
     * @param realClass The class to process
     * @param methods The methods to process
     * @param attMap The attribute map (complete)
     * @param getAttMap The readable attributess map
     * @param setAttMap The settable attributes map
     * @param invokeAttMap The invokable attributes map
     */
    private void initMethods(Class realClass,
                             Method methods[],
                             Hashtable attMap, Hashtable getAttMap,
                             Hashtable setAttMap, Hashtable invokeAttMap)
    {
        for (int j = 0; j < methods.length; ++j) {
            String name=methods[j].getName();

            if( Modifier.isStatic(methods[j].getModifiers()))
                continue;
            if( ! Modifier.isPublic( methods[j].getModifiers() ) ) {
                if( log.isDebugEnabled())
                    log.debug("Not public " + methods[j] );
                continue;
            }
            if( methods[j].getDeclaringClass() == Object.class )
                continue;
            Class params[]=methods[j].getParameterTypes();

            if( name.startsWith( "get" ) && params.length==0) {
                Class ret=methods[j].getReturnType();
                if( ! supportedType( ret ) ) {
                    if( log.isDebugEnabled() )
                        log.debug("Unsupported type " + methods[j]);
                    continue;
                }
                name=unCapitalize( name.substring(3));

                getAttMap.put( name, methods[j] );
                // just a marker, we don't use the value
                attMap.put( name, methods[j] );
            } else if( name.startsWith( "is" ) && params.length==0) {
                Class ret=methods[j].getReturnType();
                if( Boolean.TYPE != ret  ) {
                    if( log.isDebugEnabled() )
                        log.debug("Unsupported type " + methods[j] + " " + ret );
                    continue;
                }
                name=unCapitalize( name.substring(2));

                getAttMap.put( name, methods[j] );
                // just a marker, we don't use the value
                attMap.put( name, methods[j] );

            } else if( name.startsWith( "set" ) && params.length==1) {
                if( ! supportedType( params[0] ) ) {
                    if( log.isDebugEnabled() )
                        log.debug("Unsupported type " + methods[j] + " " + params[0]);
                    continue;
                }
                name=unCapitalize( name.substring(3));
                setAttMap.put( name, methods[j] );
                attMap.put( name, methods[j] );
            } else {
                if( params.length == 0 ) {
                    if( specialMethods.get( methods[j].getName() ) != null )
                        continue;
                    invokeAttMap.put( name, methods[j]);
                } else {
                    boolean supported=true;
                    for( int i=0; i<params.length; i++ ) {
                        if( ! supportedType( params[i])) {
                            supported=false;
                            break;
                        }
                    }
                    if( supported )
                        invokeAttMap.put( name, methods[j]);
                }
            }
        }
    }

    /**
     * XXX Find if the 'className' is the name of the MBean or
     *       the real class ( I suppose first )
     * XXX Read (optional) descriptions from a .properties, generated
     *       from source
     * XXX Deal with constructors
     *
     * @param registry The Bean registry (not used)
     * @param domain The bean domain (not used)
     * @param realClass The class to analyze
     * @param type The bean type
     * @return ManagedBean The create MBean
     */
    public ManagedBean createManagedBean(Registry registry, String domain,
                                         Class realClass, String type)
    {
        ManagedBean mbean= new ManagedBean();

        Method methods[]=null;

        Hashtable attMap=new Hashtable();
        // key: attribute val: getter method
        Hashtable getAttMap=new Hashtable();
        // key: attribute val: setter method
        Hashtable setAttMap=new Hashtable();
        // key: operation val: invoke method
        Hashtable invokeAttMap=new Hashtable();

        methods = realClass.getMethods();

        initMethods(realClass, methods, attMap, getAttMap, setAttMap, invokeAttMap );

        try {

            Enumeration en=attMap.keys();
            while( en.hasMoreElements() ) {
                String name=(String)en.nextElement();
                AttributeInfo ai=new AttributeInfo();
                ai.setName( name );
                Method gm=(Method)getAttMap.get(name);
                if( gm!=null ) {
                    //ai.setGetMethodObj( gm );
                    ai.setGetMethod( gm.getName());
                    Class t=gm.getReturnType();
                    if( t!=null )
                        ai.setType( t.getName() );
                }
                Method sm=(Method)setAttMap.get(name);
                if( sm!=null ) {
                    //ai.setSetMethodObj(sm);
                    Class t=sm.getParameterTypes()[0];
                    if( t!=null )
                        ai.setType( t.getName());
                    ai.setSetMethod( sm.getName());
                }
                ai.setDescription("Introspected attribute " + name);
                if( log.isDebugEnabled()) log.debug("Introspected attribute " +
                        name + " " + gm + " " + sm);
                if( gm==null )
                    ai.setReadable(false);
                if( sm==null )
                    ai.setWriteable(false);
                if( sm!=null || gm!=null )
                    mbean.addAttribute(ai);
            }

            en=invokeAttMap.keys();
            while( en.hasMoreElements() ) {
                String name=(String)en.nextElement();
                Method m=(Method)invokeAttMap.get(name);
                if( m!=null && name != null ) {
                    OperationInfo op=new OperationInfo();
                    op.setName(name);
                    op.setReturnType(m.getReturnType().getName());
                    op.setDescription("Introspected operation " + name);
                    Class parms[]=m.getParameterTypes();
                    for(int i=0; i<parms.length; i++ ) {
                        ParameterInfo pi=new ParameterInfo();
                        pi.setType(parms[i].getName());
                        pi.setName( "param" + i);
                        pi.setDescription("Introspected parameter param" + i);
                        op.addParameter(pi);
                    }
                    mbean.addOperation(op);
                } else {
                    log.error("Null arg " + name + " " + m );
                }
            }

            Constructor[] constructors = realClass.getConstructors();
            for(int i=0;i<constructors.length;i++) {
                ConstructorInfo info = new ConstructorInfo();
                String className = realClass.getName();
                int nIndex = -1;
                if((nIndex = className.lastIndexOf('.'))!=-1) {
                    className = className.substring(nIndex+1);
                }
                info.setName(className);
                info.setDescription(constructors[i].getName());
                Class classes[] = constructors[i].getParameterTypes();
                for(int j=0;j<classes.length;j++) {
                    ParameterInfo pi = new ParameterInfo();
                    pi.setType(classes[j].getName());
                    pi.setName("param" + j);
                    pi.setDescription("Introspected parameter param" + j);
                    info.addParameter(pi);
                }
                mbean.addConstructor(info);
            }
            
            if( log.isDebugEnabled())
                log.debug("Setting name: " + type );
            mbean.setName( type );

            return mbean;
        } catch( Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }


    // -------------------- Utils --------------------
    /**
     * Converts the first character of the given
     * String into lower-case.
     *
     * @param name The string to convert
     * @return String
     */
    private static String unCapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

}

// End of class: MbeanDescriptorsIntrospectionSource
