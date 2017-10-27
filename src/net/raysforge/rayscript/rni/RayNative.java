/*
 * Created on 26.04.2003
 * 
 * 
 * idee: NativRayString meldet sich selbst an:
 * über reflection erzeugt es alle nativ funktions ?
 * call führt call ebenfalls per reflection aus ?!
 * 
 * unbedingt diese version speichern
 * evtl noch bei c++ wichtig !!!
 * 
 * TODO
 * 2. idee: alle classen geben bei toString ein RayVar zurück
 * somit wäre der wichtige Übergang am einfachsten
 */
package net.raysforge.rayscript.rni;

import net.raysforge.rayscript.*;

public class RayNative
{
    public static RayHook hook;

    public static void addNatives(RayFile rf, RayHook hook) throws Exception
    {
        RayNative.hook = hook;
        rf.classes.put("Object", NativeObject.getNativeRayClass_(hook));
        rf.classes.put("String", NativeString.getNativeRayClass_(hook));
        rf.classes.put("Array", NativeArray.getNativeRayClass_(hook));
        rf.classes.put("StringArray", NativeStringArray.getNativeRayClass_(hook));
        rf.classes.put("Hash", NativeHash.getNativeRayClass_(hook));
        rf.classes.put("Sql", NativeSql.getNativeRayClass_(hook));
    }

    /*
    public static RayRef getNewRayRef(NativeClass current, RayHook hook)
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
    public static RayRef call(RayString className, RayString func, RayClass instance, RayRefVector parameterValues)
    {
        return null;
    }

    private static HashMap asd(RayClass instance, RayString key)
    {
        HashMap hm;
        if (!instance.nativeVars.containsKey(key))
        {
            instance.nativeVars.put(key, hm = new HashMap());
        }
        else
        {
            hm = (HashMap) instance.nativeVars.get(key);
        }
        return hm;
    }
*/

/*
    public static RayClass getHashClass2()
    {
        RayFunction rf;
        RayClass rc = new RayClass();
    
        rc.className = new RayString("Hash");
    
        rf = new RayFunction();
        rf.isNative = true;
        rf.parent = rc;
        rf.name = new RayString("get");
        rc.functions.put(rf.name, rf);
    
        rf = new RayFunction();
        rf.isNative = true;
        rf.parent = rc;
        rf.name = new RayString("put");
        rc.functions.put(rf.name, rf);
    
        rf = new RayFunction();
        rf.isNative = true;
        rf.parent = rc;
        rf.name = new RayString("keys");
        rc.functions.put(rf.name, rf);
    
        return rc;
    }
*/
}
