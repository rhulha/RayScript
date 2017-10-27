package net.raysforge.rayscript;

import java.util.HashMap;
import java.util.Iterator;

import net.raysforge.commons.RayString;
import net.raysforge.rayscript.rni.NativeClass;
import net.raysforge.rayscript.rni.NativeString;

public class RayClass
{
    public RayHook hook;
    HashMap vars = new HashMap();
    HashMap staticVars = new HashMap();
    HashMap functions = new HashMap();
    public RayClass extendsClass = null;
    RayString extendsClassName = null;
    public RayString className = null;
    public NativeClass nativeClass = null;

    void optimize(RayFile tree) // sollte aufgerufen werden, wenn alle Dateien geparst wurden.
    {
        // TODO: rekursive vererbung prüfen !
        if (extendsClassName != null && extendsClass == null)
            extendsClass = (RayClass) tree.classes.get(extendsClassName);
    }

    // Hier wird die Klasse kopiert und alle Oberklassen.
    // damit jede variable ihre eigenen klassen variablen hat.
    // ausser funktionen und static vars !
    public RayClass instantiate() throws Exception // TODO: kann man das noch optimieren ? umbenennen in clone ?
    {
        RayClass rc = this;
        RayClass tempClass = new RayClass();
        RayClass returnNewClass = tempClass;
        while (rc != null)
        {
            tempClass.staticVars = rc.staticVars;
            tempClass.className = rc.className;
            if( rc.nativeClass != null)
              tempClass.nativeClass = rc.nativeClass.getNativeClass();
            Iterator iter;

            iter = rc.vars.keySet().iterator();
            while (iter.hasNext())
            {
                Object key = iter.next();
                tempClass.vars.put(key, ((RayRef) rc.vars.get(key)).clone());
            }
            iter = rc.functions.keySet().iterator();
            while (iter.hasNext())
            {
                Object key = iter.next();
                RayFunction rf = ((RayFunction) rc.functions.get(key));
                RayFunction temprf = (RayFunction) rf.clone();
                //                temprf.parent = tempClass;
                temprf.parent = returnNewClass;
                tempClass.functions.put(key, temprf);
            }

            rc = rc.extendsClass;
            if (rc != null)
            {
                tempClass.extendsClass = new RayClass();
                tempClass = tempClass.extendsClass;
                tempClass.vars = new HashMap();

            }
        }
        return returnNewClass;
    }

    protected static RayClass parse(RayFile rs, RayHook hook, RayString className) throws EndOfCodeException
    {
        RayClass rc = new RayClass();
        RayString name, token;

        rc.className = className;
        rc.hook = hook;
        token = RayUtils.getToken(rs);

        if (token.equals("extends")) // or ":" for c++ coders :-)
        {
            rc.extendsClassName = RayUtils.getSourceToken(rs);
            token = RayUtils.getSourceToken(rs);
        }
        else if (token.equals("{"))
        {
            // TODO: auto extends Object
        }
        else
        {
            throw new RuntimeException("no { after class definition found: " + token);
        }

        while (true)
        {
            token = RayUtils.getSourceToken(rs);
            if (token.length() == 0 || token.equals("}"))
                break;
            else if (token.equals("function"))
            {
                name = RayUtils.getSourceToken(rs);
                rc.functions.put(name, RayFunction.parse(rs, hook, rc));
            }
            else if (token.equals("native"))
            {
                name = RayUtils.getSourceToken(rs);
                token = RayUtils.getSourceToken(rs);
                if (!token.equals(";"))
                    throw new RuntimeException("no ; after native function definition");
                RayFunction rfunc = new RayFunction();
                rfunc.isNative = true;
                rfunc.name = name;
                rfunc.parent = rc;
                rc.functions.put(name, rfunc);
            }
            else if (token.equals("var"))
            {
                name = RayUtils.getSourceToken(rs);
                token = RayUtils.getSourceToken(rs);
                if (token.equals("="))
                {
                    token = RayUtils.getSourceToken(rs);
                    if (token.charAt(0) == '"')
                        token = token.substringto(1, -2);
                    rc.vars.put(name, NativeString.getNewRayRef(""+token, hook)); // TODO: put parseVar()
                    RayUtils.getSourceToken(rs);
                }
                else if (token.equals(";"))
                    rc.vars.put(name, NativeString.getNewRayRef("", hook));
                else
                    throw new RuntimeException("missing ; after class var definition: " + name);
            }
            else if (token.equals("static"))
            {
                name = RayUtils.getSourceToken(rs);
                token = RayUtils.getSourceToken(rs);
                if (token.equals("="))
                {
                    rc.staticVars.put(name, NativeString.getNewRayRef(""+RayUtils.getSourceToken(rs), hook) );
                    RayUtils.getSourceToken(rs); // ";"
                }
                else if (token.equals(";"))
                    rc.staticVars.put(name, NativeString.getNewRayRef("", hook));
                else
                    throw new RuntimeException("missing ; after class var definition: " + name);
            }
            else
            {
                throw new RuntimeException("unknown keyword in class: " + token);
            }
        }
        return rc;
    }

    public void dump(RayHook hook)
    {
        Iterator i;
        Object key;

        if (extendsClassName != null)
            hook.print("  extends " + extendsClassName);

        hook.print("{");
        i = this.vars.keySet().iterator();
        //        hook.print("// vars hashCode: " +this.vars.hashCode());
        while (i.hasNext())
        {
            key = i.next();
            hook.print("  var " + key + " = " + vars.get(key));
        }

        i = this.functions.keySet().iterator();
        //        hook.print("// funcs hashCode: " +this.functions.hashCode());
        while (i.hasNext())
        {
            key = i.next();
            hook.print("  function " + key + " {");
            RayFunction rfunc = (RayFunction) this.functions.get(key);
            rfunc.dump(hook);
            hook.print("  } // function " + key);
        }
    }

    public RayRef classFuncCall( RayString funcName, RayRefVector params, RayRef ref)
    {
        RayFunction rfunc;
        RayClass rc;
        rfunc = (RayFunction) functions.get(funcName);
        while (rfunc == null && extendsClass != null)
        {
            rc = extendsClass;
            rfunc = (RayFunction) rc.functions.get(funcName);
        }
        if (rfunc == null)
            throw new RuntimeException("Function '" + funcName + "' not found: " + rfunc);
        RayRef retval = rfunc.execute(className, params, ref, funcName);
        return retval == null ? new RayRef() : retval;
    }
}
