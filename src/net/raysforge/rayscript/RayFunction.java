package net.raysforge.rayscript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.raysforge.commons.RayString;

public class RayFunction // extends RayCode ?
{
    public RayCode code = null;
    Vector parameterNames = new Vector(); // Vector of RayStrings;
    public RayClass parent = null;

    boolean isNative = false;
    RayString name = null;

    protected RayRef execute(RayString className, RayRefVector parameterValues, RayRef ref, RayString funcName)
    {
        if (isNative)
        {
//          return RayNative.call(parent.className, name, parent, parameterValues);
            return parent.nativeClass.call(className, funcName, ref, parameterValues, parent.hook);
        }
        else
        {
            HashMap parameters = new HashMap(); // FIXED: Muss hier sein, damit rekursive funktionen gehen!
            for (int i = 0; i < parameterNames.size(); i++)
            {
                if (i < parameterValues.size())
                    parameters.put(parameterNames.get(i), parameterValues.get(i));
            }
            return code.execute(parameters);
        }
    }

    protected Object clone() // TODO: temprf.parent = this.parent; ??
    {
        RayFunction temprf = new RayFunction();
        temprf.name = this.name;
        temprf.isNative = this.isNative;
        temprf.parameterNames = this.parameterNames;
        if (this.code != null)
        {
            temprf.code = (RayCode) this.code.clone();
            temprf.code.parent = temprf;
        }
        return temprf;
    }

    protected static RayFunction parse(RayFile rs, RayHook hook) throws EndOfCodeException
    {
        return parse(rs, hook, null);
    }

    protected static RayFunction parse(RayFile rs, RayHook hook, RayClass parent) throws EndOfCodeException
    {
        RayFunction func = new RayFunction();
        func.parent = parent;

        RayString name, token;
        token = RayUtils.getSourceToken(rs);

        if (token.equals("("))
        {
            token = RayUtils.getSourceToken(rs);
            while (!token.equals(")"))
            {
                if (token.equals(","));
                else if (token.startsWithLetter())
                    func.parameterNames.add(token);
                else
                    throw new RuntimeException("unknown token in function paramter list: " + token);
                token = RayUtils.getSourceToken(rs);
            }
            token = RayUtils.getSourceToken(rs);
        }

        if (!token.equals("{"))
            throw new RuntimeException("damn 21 c " + token);

        func.code = RayCode.parse(rs, hook, func);
        if (func.code == null)
            throw new RuntimeException("RayCode.parse shouldn't return null");

        return func;
    }

    public void dump(RayHook hook)
    {
        Iterator i;
        Object key;
//        if (this == null)            return;
        if (isNative)
            hook.print("is Native");
        if (code != null)
            code.dump(hook);
    }
}
