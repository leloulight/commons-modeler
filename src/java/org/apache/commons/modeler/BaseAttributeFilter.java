/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//modeler/src/java/org/apache/commons/modeler/BaseAttributeFilter.java,v 1.3 2003/07/20 07:35:12 ggregory Exp $
 * $Revision: 1.3 $
 * $Date: 2003/07/20 07:35:12 $
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


import java.util.HashSet;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;


/**
 * <p>Implementation of <code>NotificationFilter</code> for attribute change
 * notifications.  This class is used by <code>BaseModelMBean</code> to
 * construct attribute change notification event filters when a filter is not
 * supplied by the application.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2003/07/20 07:35:12 $
 */

public class BaseAttributeFilter implements NotificationFilter {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new filter that accepts only the specified attribute
     * name.
     *
     * @param name Name of the attribute to be accepted by this filter, or
     *  <code>null</code> to accept all attribute names
     */
    public BaseAttributeFilter(String name) {

        super();
        if (name != null)
            addAttribute(name);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of attribute names that are accepted by this filter.  If this
     * list is empty, all attribute names are accepted.
     */
    private HashSet names = new HashSet();


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new attribute name to the set of names accepted by this filter.
     *
     * @param name Name of the attribute to be accepted
     */
    public void addAttribute(String name) {

        synchronized (names) {
            names.add(name);
        }

    }


    /**
     * Clear all accepted names from this filter, so that it will accept
     * all attribute names.
     */
    public void clear() {

        synchronized (names) {
            names.clear();
        }

    }


    /**
     * Return the set of names that are accepted by this filter.  If this
     * filter accepts all attribute names, a zero length array will be
     * returned.
     */
    public String[] getNames() {

        synchronized (names) {
            return ((String[]) names.toArray(new String[names.size()]));
        }

    }


    /**
     * <p>Test whether notification enabled for this event.
     * Return true if:</p>
     * <ul>
     * <li>This is an attribute change notification</li>
     * <li>Either the set of accepted names is empty (implying that all
     *     attribute names are of interest) or the set of accepted names
     *     includes the name of the attribute in this notification</li>
     * </ul>
     */
    public boolean isNotificationEnabled(Notification notification) {

        if (notification == null)
            return (false);
        if (!(notification instanceof AttributeChangeNotification))
            return (false);
        AttributeChangeNotification acn =
            (AttributeChangeNotification) notification;
        if (!AttributeChangeNotification.ATTRIBUTE_CHANGE.equals(acn.getType()))
            return (false);
        synchronized (names) {
            if (names.size() < 1)
                return (true);
            else
                return (names.contains(acn.getAttributeName()));
        }

    }


    /**
     * Remove an attribute name from the set of names accepted by this
     * filter.
     *
     * @param name Name of the attribute to be removed
     */
    public void removeAttribute(String name) {

        synchronized (names) {
            names.remove(name);
        }

    }


}
