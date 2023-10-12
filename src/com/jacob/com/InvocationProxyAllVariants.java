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
package com.jacob.com;

/**
 * This class acts as a proxy between the windows event callback mechanism and
 * the Java classes that are looking for events. It assumes that all of the Java
 * classes that are looking for events implement methods with the same names as
 * the windows events and that the implemented methods accept an array of
 * variant objects. The methods can return void or a Variant that will be
 * returned to the calling layer. All Event methods that will be recognized by
 * InvocationProxyAllEvents have the signature
 *
 * <code> void eventMethodName(Variant[])</code> or
 * <code> Variant eventMethodName(Variant[])</code>
 */
public class InvocationProxyAllVariants extends InvocationProxy {

    /*
     * (non-Javadoc)
     *
     * @see com.jacob.com.InvocationProxy#invoke(java.lang.String,
     *      com.jacob.com.Variant[])
     */
    public Variant invoke(String methodName, Variant[] targetParameters) {
        if (targetParameters == null) {
            throw new IllegalArgumentException("InvocationProxy: missing Variant parameters");
        }
        Class<?>[] types = {Variant[].class};
        Object[] args = {targetParameters};
        return doInvoke(methodName, types, args);
    }
}
