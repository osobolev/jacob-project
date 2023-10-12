/*
 * Copyright (c) 1999-2004 Sourceforge JACOB Project.
 * All rights reserved. Originator: Dan Adler (http://danadler.com).
 * Get more information about JACOB at http://sourceforge.net/projects/jacob-project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.jacob.activeX;

import com.jacob.com.InvocationProxy;
import com.jacob.com.NotImplementedException;
import com.jacob.com.Variant;

/**
 * RELEASE 1.12 EXPERIMENTAL.
 * <p>
 * This class that lets event handlers receive events with all java objects as
 * parameters. The standard Jacob event methods all accept an array of Variant
 * objects. When using this class, you can set up your event methods as regular
 * java methods with the correct number of parameters of the correct java type.
 * This does NOT work for any event that wishes to accept a call back and modify
 * the calling parameters to tell windows what to do. An example is when an
 * event lets the receiver cancel the action by setting a boolean flag to false.
 * The java objects cannot be modified and their values will not be passed back
 * into the originating Variants even if they could be modified.
 * <p>
 * This class acts as a proxy between the windows event callback mechanism and
 * the Java classes that are looking for events. It assumes that all of the Java
 * classes that are looking for events implement methods with the same names as
 * the windows events and that the implemented methods native java objects of
 * the type and order that match the windows documentation. The methods can
 * return void or a Variant that will be returned to the calling layer. All
 * Event methods that will be recognized by InvocationProxyAllEvents have the
 * signature
 *
 * <code> void eventMethodName(Object,Object...)</code> or
 * <code> Object eventMethodName(Object,Object...)</code>
 */
public class ActiveXInvocationProxy extends InvocationProxy {

    /*
     * (non-Javadoc)
     *
     * @see com.jacob.com.InvocationProxy#invoke(java.lang.String,
     *      com.jacob.com.Variant[])
     */
    public Variant invoke(String methodName, Variant[] targetParameters) {
        Object[] args = getParametersAsJavaObjects(targetParameters);
        Class<?>[] types = getParametersAsJavaClasses(args);
        return doInvoke(methodName, types, args);
    }

    /**
     * creates a method signature compatible array of classes from an array of
     * parameters
     *
     * @param args
     * @return
     */
    private Class<?>[] getParametersAsJavaClasses(Object[] args) {
        if (args == null) {
            throw new IllegalArgumentException("This only works with an array of parameters");
        }
        int numParameters = args.length;
        Class<?>[] types = new Class[numParameters];
        for (int i = 0; i < numParameters; i++) {
            Object arg = args[i];
            if (arg == null) {
                types[i] = null;
            } else {
                types[i] = arg.getClass();
            }
        }
        return types;
    }

    /**
     * converts an array of Variants to their associated Java types
     *
     * @param targetParameters
     * @return
     */
    private Object[] getParametersAsJavaObjects(Variant[] targetParameters) {
        if (targetParameters == null) {
            throw new IllegalArgumentException("This only works with an array of parameters");
        }
        int numParameters = targetParameters.length;
        Object[] args = new Object[numParameters];
        for (int i = 0; i < numParameters; i++) {
            Variant param = targetParameters[i];
            if (param == null) {
                args[i] = null;
            } else {
                try {
                    args[i] = param.toJavaObject();
                } catch (NotImplementedException nie) {
                    throw new IllegalArgumentException(
                        "Can't convert parameter " + i + " type " + param.getvt() + " to java object: " + nie.getMessage()
                    );
                }
            }
        }
        return args;
    }
}
