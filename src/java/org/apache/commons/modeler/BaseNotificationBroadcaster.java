/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/java/org/apache/commons/modeler/BaseNotificationBroadcaster.java,v 1.4 2003/04/14 02:08:16 costin Exp $
 * $Revision: 1.4 $
 * $Date: 2003/04/14 02:08:16 $
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


import java.util.ArrayList;
import java.util.Iterator;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;


/**
 * <p>Implementation of <code>NotificationBroadcaster</code> for attribute
 * change notifications.  This class is used by <code>BaseModelMBean</code> to
 * handle notifications of attribute change events to interested listeners.
 *</p>
 *
 * @author Craig R. McClanahan
 * @author Costin Manolache
 */

public class BaseNotificationBroadcaster implements NotificationBroadcaster {


    // ----------------------------------------------------------- Constructors


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of registered <code>BaseNotificationBroadcasterEntry</code>
     * entries.
     */
    protected ArrayList entries = new ArrayList();


    // --------------------------------------------------------- Public Methods


    /**
     * Add a notification event listener to this MBean.
     *
     * @param listener Listener that will receive event notifications
     * @param filter Filter object used to filter event notifications
     *  actually delivered, or <code>null</code> for no filtering
     * @param handback Handback object to be sent along with event
     *  notifications
     *
     * @exception IllegalArgumentException if the listener parameter is null
     */
    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
        throws IllegalArgumentException {

        synchronized (entries) {

            // Optimization to coalesce attribute name filters
            if (filter instanceof BaseAttributeFilter) {
                BaseAttributeFilter newFilter = (BaseAttributeFilter) filter;
                Iterator items = entries.iterator();
                while (items.hasNext()) {
                    BaseNotificationBroadcasterEntry item =
                        (BaseNotificationBroadcasterEntry) items.next();
                    if ((item.listener == listener) &&
                        (item.filter != null) &&
                        (item.filter instanceof BaseAttributeFilter) &&
                        (item.handback == handback)) {
                        BaseAttributeFilter oldFilter =
                            (BaseAttributeFilter) item.filter;
                        String newNames[] = newFilter.getNames();
                        String oldNames[] = oldFilter.getNames();
                        if (newNames.length == 0) {
                            oldFilter.clear();
                        } else {
                            if (oldNames.length != 0) {
                                for (int i = 0; i < newNames.length; i++)
                                    oldFilter.addAttribute(newNames[i]);
                            }
                        }
                        return;
                    }
                }
            }

            // General purpose addition of a new entry
            entries.add(new BaseNotificationBroadcasterEntry
                        (listener, filter, handback));
        }

    }


    /**
     * Return an <code>MBeanNotificationInfo</code> object describing the
     * notifications sent by this MBean.
     */
    public MBeanNotificationInfo[] getNotificationInfo() {

        return (new MBeanNotificationInfo[0]);

    }


    /**
     * Remove a notification event listener from this MBean.
     *
     * @param listener The listener to be removed (any and all registrations
     *  for this listener will be eliminated)
     *
     * @exception ListenerNotFoundException if this listener is not
     *  registered in the MBean
     */
    public void removeNotificationListener(NotificationListener listener)
        throws ListenerNotFoundException {

        synchronized (entries) {
            Iterator items = entries.iterator();
            while (items.hasNext()) {
                BaseNotificationBroadcasterEntry item =
                    (BaseNotificationBroadcasterEntry) items.next();
                if (item.listener == listener)
                    items.remove();
            }
        }

    }


    /**
     * Remove a notification event listener from this MBean.
     *
     * @param listener The listener to be removed (any and all registrations
     *  for this listener will be eliminated)
     * @param handback Handback object to be sent along with event
     *  notifications
     *
     * @exception ListenerNotFoundException if this listener is not
     *  registered in the MBean
     */
    public void removeNotificationListener(NotificationListener listener,
                                           Object handback)
        throws ListenerNotFoundException {

        removeNotificationListener(listener);

    }


    /**
     * Remove a notification event listener from this MBean.
     *
     * @param listener The listener to be removed (any and all registrations
     *  for this listener will be eliminated)
     * @param filter Filter object used to filter event notifications
     *  actually delivered, or <code>null</code> for no filtering
     * @param handback Handback object to be sent along with event
     *  notifications
     *
     * @exception ListenerNotFoundException if this listener is not
     *  registered in the MBean
     */
    public void removeNotificationListener(NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
        throws ListenerNotFoundException {

        removeNotificationListener(listener);

    }


    /**
     * Send the specified notification to all interested listeners.
     *
     * @param notification The notification to be sent
     */
    public void sendNotification(Notification notification) {

        synchronized (entries) {
            Iterator items = entries.iterator();
            while (items.hasNext()) {
                BaseNotificationBroadcasterEntry item =
                    (BaseNotificationBroadcasterEntry) items.next();
                if ((item.filter != null) &&
                    (!item.filter.isNotificationEnabled(notification)))
                    continue;
                item.listener.handleNotification(notification, item.handback);
            }
        }

    }


    // -------------------- Internal Extensions   --------------------

    // Fast access. First index is the hook type
    // ( FixedNotificationFilter.getType() ).
    NotificationListener hooks[][]=new NotificationListener[20][];
    int hookCount[]=new int[20];

    private synchronized void registerNotifications( FixedNotificationFilter filter ) {
        String names[]=filter.getNames();
        Registry reg=Registry.getRegistry();
        for( int i=0; i<names.length; i++ ) {
            int code=reg.getId(null, names[i]);
            if( hooks.length < code ) {
                // XXX reallocate
                throw new RuntimeException( "Too many hooks " + code );
            }
            NotificationListener listeners[]=hooks[code];
            if( listeners== null ) {

            }


        }
    }

}


/**
 * Utility class representing a particular registered listener entry.
 */

class BaseNotificationBroadcasterEntry {

    public BaseNotificationBroadcasterEntry(NotificationListener listener,
                                            NotificationFilter filter,
                                            Object handback) {
        this.listener = listener;
        this.filter = filter;
        this.handback = handback;
    }

    public NotificationFilter filter = null;

    public Object handback = null;

    public NotificationListener listener = null;

}
