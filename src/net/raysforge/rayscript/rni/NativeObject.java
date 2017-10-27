/*
 * Created on 08.05.2003
 */
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
import net.raysforge.rayscript.RaySystem;

public class NativeObject implements NativeClass
{

    public NativeClass getNativeClass() throws Exception
    {
        return new NativeObject();
    }

    public RayClass getNativeRayClass(RayHook hook) throws Exception
    {
        return getNativeRayClass_(hook);
    }

    public static RayClass getNativeRayClass_(RayHook hook) throws Exception
    {
        char source[] = FileUtils.readCompleteFile(new File(RayScript.class.getResource("objtree/Object.ray").getFile()), "utf-8").toString().toCharArray();
        RayFile rf = RayFile.parse(source, hook, false);
        return (RayClass) rf.classes.get(new RayString("Object"));
    }

    public RayRef call(RayString className, RayString func, RayRef instance, RayRefVector parameterValues, RayHook hokk)
    {
        if (className.equals("Object"))
        {
        }
        if (func.equals("clone"))
        {
            RayRef rv = new RayRef();
            try
            {
                rv.setType(instance.getType().instantiate());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return rv;
        }
        else if (func.equals("dump"))
        {

            RaySystem.hook.print("\nclass " + instance.getType().className + " {");
            instance.getType().dump(RaySystem.hook);
        }
        else
        {
            System.err.println("unknown native function " + func);
        }
        return null;
    }
}
