
package net.raysforge.rayscript.rni;

import net.raysforge.commons.RayString;
import net.raysforge.rayscript.RayHook;
import net.raysforge.rayscript.RayRef;
import net.raysforge.rayscript.RayRefVector;


public interface NativeClass
{
    public NativeClass getNativeClass() throws Exception;

 //   public RayClass getNativeRayClass(RayHook hook) throws Exception;
    
    public RayRef call(RayString className, RayString func, RayRef instance, RayRefVector parameterValues, RayHook hokk);
}
