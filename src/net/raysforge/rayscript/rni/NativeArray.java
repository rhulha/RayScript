// contains icontains, sublist, delete/remove
package net.raysforge.rayscript.rni;

import java.io.File;

import net.raysforge.commons.FileUtils;
import net.raysforge.commons.RayString;
import net.raysforge.rayscript.RayClass;
import net.raysforge.rayscript.RayFile;
import net.raysforge.rayscript.RayHook;
import net.raysforge.rayscript.RayRef;
import net.raysforge.rayscript.RayRefVector;
import net.raysforge.rayscript.RayScript;
import net.raysforge.rayscript.RayUtils;

public class NativeArray implements NativeClass
{
    RayRefVector vector = new RayRefVector();

    public NativeClass getNativeClass() throws Exception
    {
        return new NativeArray();
    }

    public RayClass getNativeRayClass(RayHook hook) throws Exception
    {
        return getNativeRayClass_(hook);
    }

    public static RayClass getNativeRayClass_(RayHook hook) throws Exception
    {
        RayClass rayClass = null;
        char source[] = FileUtils.readCompleteFile(new File(RayScript.class.getResource("objtree/Array.ray").getFile()), "utf-8").toString().toCharArray();
        RayFile rf = RayFile.parse(source, hook, false);
        rayClass = (RayClass) rf.classes.get(new RayString("Array"));
        rayClass.nativeClass = new NativeArray();
        return rayClass;
    }

    public static RayRef getNewRayRef(NativeArray current, RayHook hook)
    {
        RayRef rayRef = new RayRef();
        RayClass rayClass;
        try
        {
            rayClass = current.getNativeRayClass(hook);
            rayClass.nativeClass = current;
            rayRef.setType(rayClass);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rayRef;
    }

    // **********

    public void setArray(RayRef[] ref)
    {
        vector.clear();
        for (int i = 0; i < ref.length; i++)
        {
            vector.add( ref[i].cloneRef());
        }
    }

    public void add(RayRef ref)
    {
        vector.add(ref.cloneRef());
    }

    public void add(RayRefVector params)
    {
        for (int i = 0; i < params.size(); i++)
        {
            vector.add(params.getRayRef(i).cloneRef());
        }
    }

    public boolean isTrue()
    {
        if (vector == null)
            return false;
        if (vector.size() == 0)
            return false;
        return true;
    }

    public RayString[] getRayStringArray()
    {
        RayString rsa[] = new RayString[vector.size()];
        for (int i = 0; i < vector.size(); i++)
        {
            rsa[i] = (RayString) vector.get(i); // TODO (RayRef).getNativeString.strval
        }
        return rsa;
    }

    public void assign(NativeArray var)
    {
        vector = var.vector;
    }

    public Object clone()
    {
        NativeArray v = new NativeArray();
        v.vector = (RayRefVector) vector.clone();
        return v;
    }

    public static RayRef execFunc(RayRef ref, String func, RayRefVector params, RayHook hook)
    {
        //        hook.print(current.toString() + ":" + func + " " + params[0].toString());
        if (ref == null)
            RayUtils.RunExp("ref == null");

        hook = ref.getType().hook;
        NativeArray current = (NativeArray) ref.getType().nativeClass;

        if (func.equals("join"))
        {
            NativeString rv = new NativeString();
            rv.setStr(RayString.join(params.getNativeString(0).getStr().toString(), current.getRayStringArray()));
            return NativeString.getNewRayRef(rv, hook);
        }
        else if (func.equals("length") || func.equals("size"))
        {
            NativeString rv = new NativeString();
            rv.setInt(current.vector.size());
            return NativeString.getNewRayRef(rv, hook);
        }
        else if (func.equals("clear"))
        {
            current.vector.clear();
        }
        else if (func.equals("get"))
        {
            int i = params.getNativeString(0).getInt();
            if (i < 0)
                i = current.vector.size() + i;
            if (i < 0 || i >= current.vector.size())
                throw new RuntimeException("array index out of bounds");
            return current.vector.getRayRef(i);
        }
        else if (func.equals("add"))
        {
            current.add(params);
        }
        else if (func.equals("grep")) // TODO: grep
        {
        }
        else if (func.equals("map")) // TODO: map
        {
        }
        else if (func.equals("sort"))
        {
            NativeArray rv = (NativeArray) current.clone();
//            Sorter.qsort(rv.vector);
//            current.array = rv.array; // TODO: copy values ?
        }
        else
            throw new RuntimeException("Unknown function: " + func);
        return ref;
    }

    public RayRef call(RayString className, RayString func, RayRef instance, RayRefVector parameterValues, RayHook hook)
    {
        return execFunc(instance, "" + func, parameterValues, hook);
    }
}
