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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * DispatchEvents wraps this class around any event handlers before making the
 * JNI call that sets up the link with EventProxy. This means that
 * EventProxy.cpp just calls invoke(String,Variant[]) against the instance of
 * this class. Then this class does reflection against the event listener to
 * call the actual event methods. The event methods can return void or return a
 * Variant. Any value returned will be passed back to the calling windows module
 * by the Jacob JNI layer.
 * <p>
 * The void returning signature is the standard legacy signature. The Variant
 * returning signature was added in 1.10 to support event handlers returning
 * values.
 *
 * @author joe
 */
public abstract class InvocationProxy {

    /**
     * the object we will try and forward to.
     */
    protected Object mTargetObject = null;

    /**
     * dummy constructor for subclasses that don't actually wrap anything and
     * just want to override the invoke() method
     */
    protected InvocationProxy() {
    }

    /**
     * The method actually invoked by EventProxy.cpp. The method name is
     * calculated by the underlying JNI code from the MS windows Callback
     * function name. The method is assumed to take an array of Variant objects.
     * The method may return a Variant or be a void. Those are the only two
     * options that will not blow up.
     * <p>
     * Subclasses that override this should make sure mTargetObject is not null
     * before processing.
     *
     * @param methodName       name of method in mTargetObject we will invoke
     * @param targetParameters Variant[] that is the single parameter to the method
     * @return an object that will be returned to the com event caller
     */
    public abstract Variant invoke(String methodName,
                                   Variant[] targetParameters);

    /**
     * used by EventProxy.cpp to create variant objects in the right thread
     *
     * @return Variant object that will be used by the COM layer
     */
    public Variant getVariant() {
        return new VariantViaEvent();
    }

    /**
     * Sets the target for this InvocationProxy.
     *
     * @param pTargetObject
     * @throws IllegalArgumentException if target is not publicly accessible
     */
    public void setTarget(Object pTargetObject) {
        if (JacobObject.isDebugEnabled()) {
            JacobObject.debug("InvocationProxy: setting target " + pTargetObject);
        }
        if (pTargetObject != null) {
            // JNI code apparently bypasses this check and could operate against
            // protected classes. This seems like a security issue...
            // maybe it was because JNI code isn't in a package?
            if (!Modifier.isPublic(pTargetObject.getClass().getModifiers())) {
                throw new IllegalArgumentException("InvocationProxy only public classes can receive event notifications");
            }
        }
        mTargetObject = pTargetObject;
    }

    protected Variant doInvoke(String methodName, Class<?>[] types, Object[] args) {
        try {
            Class<?> targetClass = mTargetObject.getClass();
            Method targetMethod = targetClass.getMethod(methodName, types);
            // protected classes can't be invoked against even if they
            // let you grab the method. you could do
            // targetMethod.setAccessible(true);
            // but that should be stopped by the security manager
            Object mReturnedByInvocation = targetMethod.invoke(mTargetObject, args);
            if (mReturnedByInvocation == null) {
                return null;
            } else if (mReturnedByInvocation instanceof Variant) {
                return (Variant) mReturnedByInvocation;
            } else {
                return new Variant(mReturnedByInvocation);
            }
        } catch (NoSuchMethodException e) {
            // this happens whenever the listener doesn't implement all the
            // methods
            if (JacobObject.isDebugEnabled()) {
                JacobObject.debug("InvocationProxy: listener (" + mTargetObject + ") doesn't implement " + methodName);
            }
            return null;
        } catch (IllegalAccessException e) {
            // can't access the method on the target instance for some reason
            throw new JacobException(e);
        } catch (InvocationTargetException e) {
            // invocation of target method failed
            Throwable target = e.getTargetException();
            if (target instanceof RuntimeException) {
                throw (RuntimeException) target;
            } else if (target instanceof Error) {
                throw (Error) target;
            } else {
                throw new JacobException(target);
            }
        }
    }
}
