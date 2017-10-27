
// ieq ine ilt ile igt ige indexOf

package net.raysforge.rayscript.rni;

import java.io.File;
import java.io.IOException;

import net.raysforge.commons.FileUtils;
import net.raysforge.commons.IntUtils;
import net.raysforge.commons.RayString;
import net.raysforge.rayscript.RayClass;
import net.raysforge.rayscript.RayFile;
import net.raysforge.rayscript.RayHook;
import net.raysforge.rayscript.RayRef;
import net.raysforge.rayscript.RayRefVector;
import net.raysforge.rayscript.RayScript;
import net.raysforge.rayscript.RaySystem;
import net.raysforge.rayscript.RayUtils;

public class NativeString implements Comparable, NativeClass
{
    public final static RayString emptyString = new RayString("");
    public final static NativeString trueStr = new NativeString().setInt(1);
    public final static NativeString falseStr = new NativeString().setInt(0);
    public final static RayRef trueRef = getNewRayRef(trueStr, null);
    public final static RayRef falseRef = getNewRayRef(falseStr, null);

    public final static int STR = 1;

    private int status = 0;
    RayString strval;

    //    public RayVar rvxxx = new RayVar();  // TODO erzeugt overflow error, wie mach ich das bei mir ?

    public NativeClass getNativeClass() throws Exception
    {
        return new NativeString();
    }

    public RayClass getNativeRayClass(RayHook hook) throws Exception
    {
        return getNativeRayClass_(hook);
    }
    public static RayClass getNativeRayClass_2(RayHook hook) throws Exception
    {
        RayClass rayClass = null;
        char source[] = FileUtils.readCompleteFile(new File(RayScript.class.getResource("objtree/String.ray").getFile()), "utf-8").toString().toCharArray();
        RayFile rf = RayFile.parse(source, hook, false);
        rayClass = (RayClass) rf.classes.get(new RayString("String"));
        rayClass.nativeClass = new NativeString();
        rayClass.hook = hook;
        return rayClass;
    }

    private static RayClass rayClass = null;
    public static RayClass getNativeRayClass_(RayHook hook) throws Exception
    {
        if (rayClass == null)
        {
            char source[] = FileUtils.readCompleteFile(new File(RayScript.class.getResource("objtree/String.ray").getFile()), "utf-8").toString().toCharArray();
            RayFile rf = RayFile.parse(source, hook, false);
            rayClass = (RayClass) rf.classes.get(new RayString("String"));
            rayClass.nativeClass = new NativeString();
            rayClass.hook = hook;
            return rayClass;
        }
        else
        {
            return rayClass.instantiate();
        }
    }

    public RayRef getNewRayRef(RayHook hook)
    {
        return NativeString.getNewRayRef(this, hook);
    }

    public static RayRef getNewRayRef(String current, RayHook hook)
    {
        NativeString ns = new NativeString();
        ns.setStr(current);
        return getNewRayRef(ns, hook);
    }

    public static RayRef getNewRayRef(NativeString current, RayHook hook)
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

    public static NativeString castToStr(RayRef ref)
    {
        return (NativeString) ref.getType().nativeClass;
    }

    public boolean equals(NativeString obj)
    {
        return this.strval.equals(obj.strval);
    }

    public Object clone()
    {
        NativeString v = new NativeString();
        if (strval != null)
        {
            v.strval = new RayString(strval);
            if (!strval.equals(v.strval)) // TODO kill
                System.out.println("alert! " + strval + "-" + v.strval);
        }
        v.status = status;
        return v;
    }

    public int getInt()
    {
        if (status == 0)
            return 0;

        if ((status & STR) > 0)
        {
            return IntUtils.parseInt(strval.toString(), 10);
        }
        return 0;
    }

    public RayString getStr()
    {
        if ((status & STR) > 0)
            return strval;
        return emptyString;
    }

    public RayString getStrOrNull()
    {
        if ((status & STR) > 0)
            return strval;
        return null;
    }
    public boolean isNull()
    {
        return status == 0;
    }
    public boolean isTrue()
    {
        if (status == 0 || getStr().length() == 0)
            return false;
        if (getInt() > 0)
            return true;
        if (getStr().length() > 1 || getStr().charAt(0) != '0') // TODO: "000" ist ein problem...
            return true;
        return false;
    }

    public NativeString setInt(int i)
    {
        status = STR;
        strval = new RayString("" + i);
        return this;
    }

    public NativeString setStr(RayString rs)
    {
        status = STR;
        strval = rs;
        return this;
    }
    public NativeString setStr(String rs)
    {
        status = STR;
        strval = new RayString(rs);
        return this;
    }

    public String toString()
    {
        RayString rs = getStr();
        if (rs != null)
            return rs.toString();
        else
            return null;
    }

    public void assign(NativeString var)
    {
        setStr(var.getStr());
    }

    public int compareTo(Object o)
    {
        return this.toString().compareTo(o.toString());
    }

    public static RayRef execFunc(RayRef ref, String func, RayRefVector params, RayHook hook)
    {
        //        hook.print(current.toString() + ":" + func + " " + params[0].toString());
        if (ref == null)
            RayUtils.RunExp("ref == null");

        hook = ref.getType().hook;
        NativeString current = ref.getNativeString();

        if (func.equals("set"))
        {
            current.assign(params.getNativeString(0));
            //            current = params.getRV(0);
        }
        else if (func.equals("eq") || func.equals("equals"))
        {
            if (current.strval.equals(params.getNativeString(0).strval))
                return trueRef;
            return falseRef;
        }
        else if (func.equals("ne"))
        {
            if (current.strval.equals(params.getNativeString(0).strval))
                return falseRef;
            return trueRef;
        }
        else if (func.equals("not"))
        {
            if (current.isTrue())
                return falseRef;
            return trueRef;
        }
        else if (func.equals("or")) // achtung kein short-cicurting !
        {
            if (current.isTrue())
            {
                current = (NativeString) current.clone();
                return getNewRayRef(current, hook);
            }
            else if (params.getNativeString(0).isTrue())
            {
                current = (NativeString) params.getNativeString(0).clone();
                return getNewRayRef(current, hook);
            }
            current = new NativeString().setInt(0);
            return getNewRayRef(current, hook);

        }
        else if (func.equals("and"))
        {
            if (current.isTrue() && params.getNativeString(0).isTrue())
            {
                current = (NativeString) params.getRayRef(0).clone();
            }
            else
                current = new NativeString().setInt(0);
            return getNewRayRef(current, hook);

        }
        else if (func.equals("lt")) // TODO: ?:;
        {
            if (current.getInt() < params.getNativeString(0).getInt())
                return trueRef;
            return falseRef;
        }
        else if (func.equals("le"))
        {
            if (current.getInt() <= params.getNativeString(0).getInt())
                return trueRef;
            return falseRef;
        }
        else if (func.equals("gt"))
        {
            if (current.getInt() > params.getNativeString(0).getInt())
                return trueRef;
            return falseRef;
        }
        else if (func.equals("ge"))
        {
            if (current.getInt() >= params.getNativeString(0).getInt())
                return trueRef;
            return falseRef;
        }
        else if (func.equals("charAt"))
        {
            current.getStr().charAt(params.getNativeString(0).getInt());
        }
        else if (func.equals("setCharAt")) // TODO: setCharAt
        {
        }
        else if (func.equals("split"))
        {
            NativeStringArray na = new NativeStringArray();
            na.setArray(current.getStr().split(params.getNativeString(0).getStr().toString()));
            RayRef naref = NativeStringArray.getNewRayRef(na, hook);
            return naref;
        }
        else if (func.equals("length") || func.equals("size"))
        {
            NativeString rv = new NativeString();
            rv.setInt(current.getStr().length());
            current = rv;
        }
        else if (func.equals("copy"))
        {
            current = (NativeString) current.clone();
        }
        else if (func.equals("inc"))
        {
            current.setInt(current.getInt() + 1);
        }
        else if (func.equals("dec"))
        {
            current.setInt(current.getInt() - 1);
        }
        else if (func.equals("append"))
        {
            current = new NativeString().setStr(current + params.getNativeString(0).toString());
            return NativeString.getNewRayRef(current, hook);
        }
        else if (func.equals("append!"))
        {
            current.setStr(current + params.getNativeString(0).toString());
        }
        else if (func.equals("plus"))
        {
            current = new NativeString().setInt(current.getInt() + params.getNativeString(0).getInt());
        }
        else if (func.equals("plus!"))
        {
            current.setInt(current.getInt() + params.getNativeString(0).getInt());
        }
        else if (func.equals("minus"))
        {
            current = new NativeString().setInt(current.getInt() - params.getNativeString(0).getInt());
        }
        else if (func.equals("minus!"))
        {
            current.setInt(current.getInt() - params.getNativeString(0).getInt());
        }
        else if (func.equals("times"))
        {
            current = new NativeString().setInt(current.getInt() * params.getNativeString(0).getInt());
        }
        else if (func.equals("times!"))
        {
            current.setInt(current.getInt() * params.getNativeString(0).getInt());
        }
        else if (func.equals("devideby"))
        {
            current = new NativeString().setInt(current.getInt() / params.getNativeString(0).getInt());
        }
        else if (func.equals("devideby!"))
        {
            current.setInt(current.getInt() / params.getNativeString(0).getInt());
        }
        else if (func.equals("sqrt"))
        {
            current = new NativeString().setInt((int) Math.sqrt(current.getInt()));
        }
        else if (func.equals("sqrt!"))
        {
            current.setInt((int) Math.sqrt(current.getInt()));
        }
        else if (func.equals("readFile"))
        {
            try
            {
                current.setStr(new RayString(FileUtils.readCompleteFile(new File(params.getNativeString(0).getStr().toString()), "utf-8").toString().toCharArray()));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else if (func.equals("check"))
        {
            RayClass rc1 = params.getRayRef(0).getType();
            RayClass rc2 = params.getRayRef(1).getType();
            rc1 = rc1.extendsClass;
            rc2 = rc2.extendsClass;
            //            if(rc1 != rc2) System.err.println("argh");
            rc1.dump(hook);
            rc2.dump(hook);
            //            hook.print(              );

        }
        else if (func.equals("print"))
        {
            //  System.out.println(current);
            if (RaySystem.hook == null)
                RayUtils.RunExp("sad");
            RaySystem.hook.print("" + current);
            //   hook
        }
        else if (func.equals("alert"))
        {
            RaySystem.hook.alert(current.toString());
        }
        else
            throw new RuntimeException("Unknown function: " + func);

        if (ref.getNativeString() != current)
            ref = getNewRayRef(current, hook);
        //    rref.getType().nativeClass = current;
        return ref;

    }

    public RayRef call(RayString className, RayString func, RayRef instance, RayRefVector parameterValues, RayHook hook)
    {
        return execFunc(instance, "" + func, parameterValues, hook);
    }

}
