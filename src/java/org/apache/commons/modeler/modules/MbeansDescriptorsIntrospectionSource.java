package org.apache.commons.modeler.modules;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.commons.modeler.util.DomUtil;
import org.apache.commons.modeler.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Enumeration;


public class MbeansDescriptorsIntrospectionSource extends Registry.DescriptorSource
{
    private static Log log = LogFactory.getLog(MbeansDescriptorsIntrospectionSource.class);

    public void loadDescriptors( Registry registry, String location,
                                 String type, Object source)
        throws Exception
    {
        try {
            ManagedBean managed=createManagedBean(registry, null, (Class)source, type);
            if( managed==null ) return;

            registry.addManagedBean(managed);

        } catch( Exception ex ) {
            log.error( "Error reading descriptors ", ex);
        }
    }



    // ------------ Implementation for non-declared introspection classes


    // createMBean == registerClass + registerMBean

    private boolean supportedType( Class ret ) {
        return ret == String.class ||
            ret == Integer.class ||
            ret == Integer.TYPE ||
            ret == Long.class ||
            ret == Long.TYPE ||
            ret == java.io.File.class ||
            ret == Boolean.class ||
            ret == Boolean.TYPE
            ;
    }

    /** Process the methods and extract 'attributes', methods, etc
      *
      */
    private void initMethods(Class realClass,
                             Method methods[],
                             Hashtable attMap, Hashtable getAttMap,
                             Hashtable setAttMap, Hashtable invokeAttMap)
    {
        for (int j = 0; j < methods.length; ++j) {
            String name=methods[j].getName();

            if( name.startsWith( "get" ) ) {
                Class params[]=methods[j].getParameterTypes();
                if( params.length != 0 ) {
                    if( log.isDebugEnabled())
                        log.debug("Wrong param count " + name + " " + params.length);
                    continue;
                }
                if( ! Modifier.isPublic( methods[j].getModifiers() ) ) {
                    if( log.isDebugEnabled())
                        log.debug("Not public " + methods[j] );
                    continue;
                }
                Class ret=methods[j].getReturnType();
                if( ! supportedType( ret ) ) {
                    if( log.isDebugEnabled() )
                        log.debug("Unsupported type " + methods[j] + " " + ret );
                    continue;
                }
                name=unCapitalize( name.substring(3));

                getAttMap.put( name, methods[j] );
                // just a marker, we don't use the value
                attMap.put( name, methods[j] );
            } else if( name.startsWith( "is" ) ) {
                // not used in our code. Add later

            } else if( name.startsWith( "set" ) ) {
                Class params[]=methods[j].getParameterTypes();
                if( params.length != 1 ) {
                    if( log.isDebugEnabled())
                        log.debug("Wrong param count " + name + " " + params.length);
                    continue;
                }
                if( ! Modifier.isPublic( methods[j].getModifiers() ) ) {
                    if( log.isDebugEnabled())
                        log.debug("Not public " + name);
                    continue;
                }
                name=unCapitalize( name.substring(3));
                setAttMap.put( name, methods[j] );
                attMap.put( name, methods[j] );
            } else {
                if( methods[j].getParameterTypes().length != 0 ) {
                    continue;
                }
                if( methods[j].getDeclaringClass() == Object.class )
                    continue;
                if( ! Modifier.isPublic( methods[j].getModifiers() ) )
                    continue;
                invokeAttMap.put( name, methods[j]);
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
     */
    public ManagedBean createManagedBean(Registry registry, String domain, Class realClass, String type) {
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

        if( type==null) type=registry.generateSeqName(domain, realClass);

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
                    Class parms[]=m.getParameterTypes();
                    for(int i=0; i<parms.length; i++ ) {
                        ParameterInfo pi=new ParameterInfo();
                        pi.setType(parms[i].getName());
                        op.addParameter(pi);
                    }
                    mbean.addOperation(op);
                } else {
                    log.error("Null arg " + name + " " + m );
                }
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

    private static String unCapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

}