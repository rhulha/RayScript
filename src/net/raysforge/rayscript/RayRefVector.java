/*
 * Created on 17.04.2003
 */
package net.raysforge.rayscript;

import java.util.Vector;

import net.raysforge.rayscript.rni.NativeString;


/**
 * @author Ray
 */
public class RayRefVector extends Vector
{
    public synchronized RayRef getRayRef(int index)
    {
        return (RayRef) super.get(index);
    }

    public synchronized NativeString getNativeString(int index)
    {
        RayRef ref = (RayRef) super.get(index);
        RayClass rc = ref.getType();
        return (NativeString) rc.nativeClass;
    }
}
