/*
 * Created on 08.05.2003
 *
 */
package net.raysforge.rayscript.rni;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import net.raysforge.commons.FileUtils;
import net.raysforge.commons.RayString;
import net.raysforge.rayscript.RayClass;
import net.raysforge.rayscript.RayFile;
import net.raysforge.rayscript.RayHook;
import net.raysforge.rayscript.RayRef;
import net.raysforge.rayscript.RayRefVector;
import net.raysforge.rayscript.RayScript;
import net.raysforge.rayscript.RayUtils;

public class NativeHash implements NativeClass
{
    HashMap hm = new HashMap();

    public NativeClass getNativeClass() throws Exception
    {
        return new NativeHash();
    }

    public RayClass getNativeRayClass(RayHook hook) throws Exception
    {
        return getNativeRayClass_(hook);
    }

    public static RayClass getNativeRayClass_(RayHook hook) throws Exception
    {
        RayClass rayClass = null;
        char source[] = FileUtils.readCompleteFile(new File(RayScript.class.getResource("objtree/Hash.ray").getFile()), "utf-8").toString().toCharArray();
        RayFile rf = RayFile.parse(source, hook, false);
        rayClass = (RayClass) rf.classes.get(new RayString("Hash"));
        rayClass.nativeClass = new NativeHash();
        return rayClass;
    }

    public RayRef getNewRayRef(RayHook hook)
    {
        return NativeHash.getNewRayRef(this, hook);
    }

    public static RayRef getNewRayRef(NativeHash current, RayHook hook)
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

//***************

    public RayRef put(RayString rs, RayRef ref)
    {
        hm.put(rs, ref);
        return ref;
    }

    public RayRef put(RayString rs)
    {
        return (RayRef) hm.get(rs);
    }

    public RayRef put(RayClass instance, RayRefVector parameterValues)
    {
        RayRef rv = parameterValues.getRayRef(1);
        hm.put(parameterValues.getNativeString(0).getStr(), rv);
        return rv;
    }

    public RayRef get(RayClass instance, RayRefVector parameterValues)
    {
        RayRef rv = (RayRef) hm.get(parameterValues.getNativeString(0).getStr());
        return rv;
    }

    public RayRef keys(RayClass instance, RayRefVector parameterValues)
    {
        Object t[] = hm.keySet().toArray();
        RayString rva[] = new RayString[t.length];
        for (int i = 0; i < rva.length; i++)
        {
            rva[i] = (RayString) t[i];
        }
        NativeStringArray na = new NativeStringArray();
        na.setArray(rva);
        RayRef rv = NativeStringArray.getNewRayRef(na, null);
        return rv;
    }

    public static RayRef getHashRayVar(RayFile rf, HashMap strings)
    {
        NativeHash nh = new NativeHash();
        RayString rs;
        RayRef ref;

        Iterator iter = strings.keySet().iterator();
        while (iter.hasNext())
        {
            Object o = (Object) iter.next();
            Object value = strings.get(o);
            
            rs = new RayString(""+o);
            ref = new NativeString().setStr(""+value).getNewRayRef(null);
            nh.put(rs, ref);
        }
        return nh.getNewRayRef(null);
    }
/*
    public static RayRef getHashRayVar(RayFile rf, HashMap strings)
    {
        HashMap hm = strings;
        RayClass hash = (RayClass) rf.classes.get("Hash");
        hash = hash.instantiate();
        RayRefVector rvv;

        Iterator iter = hm.keySet().iterator();
        while (iter.hasNext())
        {
            Object o = (Object) iter.next();
            Object value = hm.get(o);

            rvv = new RayRefVector();
            rvv.add(new RayRef().setStr("" + o));
            rvv.add(new RayRef().setStr("" + value));
            RayNative.call(new RayString("Hash"), new RayString("put"), hash, rvv);
        }

        RayRef rvar = new RayRef();
        rvar.setType(hash);
        return rvar;
    }
*/
    public static RayRef execFunc(RayRef ref, String func, RayRefVector params, RayHook hook)
    {
        //        hook.print(current.toString() + ":" + func + " " + params[0].toString());
        if (ref == null)
            RayUtils.RunExp("ref == null");
        NativeHash current = (NativeHash) ref.getType().nativeClass;

        if (func.equals("put"))
        {
            RayRef rv = params.getRayRef(1);
            current.hm.put(params.getNativeString(0).strval, rv);
            return rv;
        }
        else if (func.equals("get"))
        {
            RayRef rv = (RayRef) current.hm.get(params.getNativeString(0).strval);
            return rv;
        }
        else if (func.equals("isEmpty"))
        {
            if (current.hm.isEmpty())
                return NativeString.trueRef;
            return NativeString.falseRef;
        }
        else if (func.equals("delete"))
        {
            RayRef rv = (RayRef) current.hm.remove(params.getNativeString(0).strval);
            return rv;
        }
        else if (func.equals("keys"))
        {
            Object t[] = current.hm.keySet().toArray();
            RayString rva[] = new RayString[t.length];
            for (int i = 0; i < rva.length; i++)
            {
                rva[i] = (RayString) t[i];
            }
            NativeStringArray na = new NativeStringArray();
            na.setArray(rva);
            RayRef rv = NativeStringArray.getNewRayRef(na, hook);
            return rv;
        }
        else if (func.equals("strValues"))
        {
            Object t[] = current.hm.values().toArray();
            RayString rva[] = new RayString[t.length];
            for (int i = 0; i < rva.length; i++)
            {
                rva[i] = ((RayRef) t[i]).getNativeString().getStr();
            }
            NativeStringArray na = new NativeStringArray();
            na.setArray(rva);
            RayRef rv = NativeStringArray.getNewRayRef(na, hook);
            return rv;
        }
        else
        {
            System.err.println("unknown native function " + func);
        }

        return ref;
    }

    public RayRef call(RayString className, RayString func, RayRef instance, RayRefVector parameterValues, RayHook hokk)
    {
        if (className.equals("Hash"))
        {
            return execFunc(instance, "" + func, parameterValues, hokk);
        }
        throw new RuntimeException();
    }
}
