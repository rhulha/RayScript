// contains icontains, sublist, delete/remove
package net.raysforge.rayscript.rni;

import java.io.File;
import java.util.Arrays;

import net.raysforge.commons.FileUtils;
import net.raysforge.commons.RayString;
import net.raysforge.rayscript.RayClass;
import net.raysforge.rayscript.RayFile;
import net.raysforge.rayscript.RayHook;
import net.raysforge.rayscript.RayRef;
import net.raysforge.rayscript.RayRefVector;
import net.raysforge.rayscript.RayScript;
import net.raysforge.rayscript.RayUtils;

public class NativeStringArray implements NativeClass
{

    public NativeString array[] = new NativeString[0];
    //   Vector vec = new Vector();

    public NativeClass getNativeClass() throws Exception
    {
        return new NativeStringArray();
    }

    public RayClass getNativeRayClass(RayHook hook) throws Exception
    {
        return getNativeRayClass_(hook);
    }

    public static RayClass getNativeRayClass_(RayHook hook) throws Exception
    {
        RayClass rayClass = null;
        char source[] = FileUtils.readCompleteFile(new File(RayScript.class.getResource("objtree/StringArray.ray").getFile()), "utf-8").toString().toCharArray();
        RayFile rf = RayFile.parse(source, hook, false);
        rayClass = (RayClass) rf.classes.get(new RayString("StringArray"));
        rayClass.nativeClass = new NativeStringArray();
        return rayClass;
    }

    public static RayRef getNewRayRef(NativeStringArray current, RayHook hook)
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

    public void setArray(NativeString[] strings)
    {
        array = strings;
    }

    public void setArray(RayString[] str)
    {
        array = new NativeString[str.length];
        for (int i = 0; i < str.length; i++)
        {
            array[i] = new NativeString();
            array[i].setStr(str[i]);
        }
    }

    public void add(RayRef ref)
    {
        NativeString rsa[] = new NativeString[array.length + 1];
        //            System.arraycopy(current.array, 0, rsa, 0, current.array.length);
        RayString.arraycopy(array, rsa);
        rsa[array.length] = (NativeString) ref.getNativeString().clone(); // TODO: cloneRef() ?
        array = rsa;
    }

    public void add(RayRefVector params)
    {
        NativeString rsa[] = new NativeString[array.length + params.size()];
        //            System.arraycopy(current.array, 0, rsa, 0, current.array.length);
        RayString.arraycopy(array, rsa);
        for (int i = 0; i < params.size(); i++)
        {
            rsa[array.length + i] = (NativeString) params.getNativeString(i).clone(); // TODO: cloneRef() ?
        }
        array = rsa;
    }

    public boolean isTrue()
    {
        if (array == null)
            return false;
        if (array.length == 0)
            return false;
        return true;
    }

    public RayString[] getRayStringArray()
    {
        RayString rsa[] = new RayString[array.length];
        for (int i = 0; i < array.length; i++)
        {
            rsa[i] = array[i].getStr();
        }
        return rsa;
    }

    public void assign(NativeStringArray var)
    {
        array = var.array;
    }

    public Object clone()
    {
        NativeStringArray v = new NativeStringArray();
        v.array = new NativeString[array.length];
        RayString.arraycopy(array, v.array);
        return v;
    }

    public static RayRef execFunc(RayRef ref, String func, RayRefVector params, RayHook hook)
    {
        //        hook.print(current.toString() + ":" + func + " " + params[0].toString());
        if (ref == null)
            RayUtils.RunExp("ref == null");

        hook = ref.getType().hook;
        NativeStringArray current = (NativeStringArray) ref.getType().nativeClass;

        if (func.equals("join"))
        {
            NativeString rv = new NativeString();
            rv.setStr(RayString.join(params.getNativeString(0).getStr().toString(), current.getRayStringArray()));
            return NativeString.getNewRayRef(rv, hook);
        }
        else if (func.equals("length") || func.equals("size"))
        {
            NativeString rv = new NativeString();
            rv.setInt(current.array.length);
            return NativeString.getNewRayRef(rv, hook);
        }
        else if (func.equals("clear"))
        {
            current.array = new NativeString[0];
        }
        else if (func.equals("get"))
        {
            //            new java.util.Vector().;
            int i = params.getNativeString(0).getInt();
            if (i < 0)
                i = current.array.length + i;
            if (i < 0 || i >= current.array.length)
                throw new RuntimeException("array index out of bounds");
            return NativeString.getNewRayRef(current.array[i], hook);
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
            NativeStringArray rv = (NativeStringArray) current.clone();
            //Sorter.qsort(rv.array);
            Arrays.sort(rv.array);
            current.array = rv.array; // TODO: copy values ?
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
