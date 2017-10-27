package net.raysforge.rayscript;

import net.raysforge.rayscript.rni.NativeString;
import net.raysforge.rayscript.rni.NativeStringArray;

public class RayRef
{
    RayClass type = null;
    
    public void setType(RayClass rclass) // todo: setType returns RayRef/this ? ie. for cloneRef();
    {
        type = rclass;
    }

    public RayClass getType()
    {
        return type;
    }

    public void assign(RayRef var)
    {
        setType(var.getType());
    }

    public RayRef cloneRef()
    {
        RayRef ref = new RayRef();
        ref.type = type;
        return ref;
    }

    public Object clone()
    {
        RayRef ref = new RayRef();
        try
        {
            ref.type = type.instantiate();
            if( type.nativeClass instanceof NativeString)
              ((NativeString)ref.type.nativeClass).assign((NativeString)type.nativeClass);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ref;
    }
    
    public boolean isTrue()
    {
        boolean doIf = false;
        if (this.type != null) // TODO: && val.type.nativeClass == null)
            doIf = true;
        if (this.type.nativeClass instanceof NativeString)
            doIf = this.getNativeString().isTrue();
        if (this.type.nativeClass instanceof NativeStringArray)
            doIf = this.getNativeArray().isTrue();
        return doIf;
    }

    public NativeString getNativeString()
    {
        return (NativeString) type.nativeClass;
    }

    public NativeStringArray getNativeArray()
    {
        return (NativeStringArray) type.nativeClass;
    }

    public boolean isNull()
    {
        return type == null;
    }
    

}
