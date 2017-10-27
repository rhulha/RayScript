package net.raysforge.rayscript;

import java.util.HashMap;
import java.util.Iterator;

import net.raysforge.commons.RayString;

public class RayFile extends RaySource
{
    public HashMap classes = new HashMap();
    public HashMap functions;
    public RayCode code;

    protected RayHook hook;

    protected RayFile(RayHook hook)
    {
        // wenn mann diese 4 news von der funktion in die classe verschieben würde
        // so könnte man mehrere Dateien Parsen und hätte die summe der classen etc in den hashes
        // und nicht nur die classen einer einzigen datei
        this.hook = hook;
        functions = new HashMap();
        code = new RayCode(hook); // empty RayCode, against null pointer excep.
    }

    public void execute()
    {
        code.execute(null);
    }

    public static RayFile parse(char source[], RayHook hook) throws Exception
    {
        RayFile rf = new RayFile(hook);
        return parse(rf, source, hook, true);
    }

    public static RayFile parse(char source[], RayHook hook, boolean addNativeClasse) throws Exception
    {
        RayFile rf = new RayFile(hook);
        return parse(rf, source, hook, addNativeClasse);
    }

    static RayClass temprc = new RayClass();
    public static RayFile parse(RayFile rf, char source[], RayHook hook, boolean addNativeClasses) throws Exception
    {

        temprc.hook = hook;
//        if( hook == null)
//          throw new RuntimeException();
        if( hook != null)
          RaySystem.hook = hook;

        rf.src = source;
        rf.pos = 0;

        while (true)
        {
            RayString name, token = RayUtils.getToken(rf);
            if (token == null || token.length() == 0)
                break;
            if (token.equals("class"))
            {
                name = RayUtils.getToken(rf);
                rf.classes.put(name, RayClass.parse(rf, hook, name));
            }
            else if (token.equals("function"))
            {
                name = RayUtils.getSourceToken(rf);
                rf.functions.put(name, RayFunction.parse(rf, hook, temprc));
            }
            else if (token.equals("comment"))
            {
                if (!RayUtils.getToken(rf).equals("{"))
                    throw new RuntimeException("damn 21 e " + token);
                RayCode.parse(rf, hook, null);
            }
            else if (token.equals("code"))
            {
                if (!RayUtils.getToken(rf).equals("{"))
                    throw new RuntimeException("damn 21 e " + token);
                rf.code = RayCode.parse(rf, hook, null);
                rf.code.parent = new RayFunction();
                rf.code.parent.parent = temprc;
                rf.code.parent.parent.functions = rf.functions;
            }
            else
            {
                throw new RuntimeException("Parse Error, unknown token in RayFile: " + token);
            }
        }
        rf.optimize(rf);
        return rf;
    }

    void optimize(RayFile tree)
    {
        Iterator i;
        Object key;

        i = this.classes.keySet().iterator();
        while (i.hasNext())
        {
            key = i.next();
            RayClass rclass = (RayClass) this.classes.get(key);
            rclass.optimize(tree);
        }
    }

    public void dump()
    {
        Iterator i;
        Object key;

        i = this.classes.keySet().iterator();
        while (i.hasNext())
        {
            key = i.next();
            hook.print("class " + key);
            RayClass rclass = (RayClass) this.classes.get(key);
            if (rclass != null)
                rclass.dump(hook);
            hook.print("} // class " + key);
        }

        i = this.functions.keySet().iterator();
        while (i.hasNext())
        {
            key = i.next();
            hook.print("function " + key + " {");
            RayFunction func = (RayFunction) this.classes.get(key);
            if (func != null)
                func.dump(hook);
            hook.print("} // function " + key);
        }

        hook.print("code {");
        code.dump(hook);
        hook.print("} // code");

    }
}
