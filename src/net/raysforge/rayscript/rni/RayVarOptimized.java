package net.raysforge.rayscript.rni;

import net.raysforge.commons.RayString;
import net.raysforge.rayscript.RayClass;

public class RayVarOptimized
{
    RayString strval;
    int intval;
    double dblval;
    RayClass objval;
    private int status = 0;
    public final static int STR = 1;
    public final static int INT = 2;
    public final static int DBL = 4;
    public final static int OBJ = 8;
    private int referenceCount = 0;

    public Object clone()
    {
        RayVarOptimized v = new RayVarOptimized();
        v.strval = strval;
        v.intval = intval;
        v.dblval = dblval;
        v.objval = objval;
        v.status = status;
        return v;
    }

    public boolean compNrs()
    {
        return intval == dblval;
    }

    //    public Var(RayString rs)
    //    {
    //        status= STR;
    //        strval= rs;
    //    }

    public RayVarOptimized setStr(RayString rs)
    {
        status = STR;
        strval = rs;
        return this;
    }
    public RayVarOptimized setStr(String rs)
    {
        status = STR;
        strval = new RayString(rs);
        return this;
    }
    public RayVarOptimized setDbl(double d)
    {
        status = DBL;
        dblval = d;
        return this;
    }
    public RayVarOptimized setInt(int i)
    {
        status = INT;
        intval = i;
        return this;
    }

    public RayVarOptimized setObj(RayClass o)
    {
        status = STR & OBJ;
        strval = new RayString(o.toString());
        objval = o;
        return this;
    }

    public RayClass getObj()
    {
        if ((status & OBJ) > 0)
        {
            return objval;
        }
        return null;
    }

    public RayString getStr()
    {
        if ((status & STR) > 0)
            return strval;
        if ((status & INT) > 0)
        {
            status |= STR;
            return strval = new RayString("" + intval);
        }
        if ((status & DBL) > 0)
        {
            status |= STR;
            return strval = new RayString("" + dblval);
        }
        return null;
    }

    public String toString()
    {
        RayString rs = getStr();
        if (rs != null)
            return rs.toString();
        else
            return null;
    }

    public int getInt()
    {
        if (status == 0)
          return 0;
        if ((status & INT) > 0)
            return intval;
        if ((status & DBL) > 0)
        {
            intval = (int) dblval;
            status |= INT;
            return intval;
        }
        if ((status & STR) > 0)
        {
            intval = parseInt(strval.toString(), 10);
            status |= INT;
            return intval;
        }
        return 0;
    }

    public double getDbl()
    {
        if ((status & DBL) > 0)
            return dblval;
        if ((status & INT) > 0)
        {
            dblval = (double) intval;
            status |= DBL;
            return dblval;
        }
        if ((status & STR) > 0)
        {
            dblval = Double.parseDouble(strval.toString());
            status |= DBL;
            return dblval;
        }
        return 0.0;

    }

    public static int parseInt(String s) throws NumberFormatException
    {
        return parseInt(s, 10);
    }
    public static int parseInt(String s, int radix) throws NumberFormatException
    {
        if (s == null || s.length() == 0)
        {
            return 0;
        }

        if (radix < Character.MIN_RADIX)
        {
            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
        }

        if (radix > Character.MAX_RADIX)
        {
            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = 0, max = s.length();
        int limit;
        int multmin;
        int digit;

        if (s.charAt(0) == '-')
        {
            negative = true;
            limit = Integer.MIN_VALUE;
            i++;
        }
        else
        {
            limit = -Integer.MAX_VALUE;
        }
        multmin = limit / radix;
        if (i < max)
        {
            digit = Character.digit(s.charAt(i++), radix);
            if (digit < 0)
            {
                return 0;
            }
            else
            {
                result = -digit;
            }
        }
        while (i < max)
        {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = Character.digit(s.charAt(i++), radix);
            if (digit < 0)
            {
                break;
            }
            if (result < multmin)
            {
                break;
            }
            result *= radix;
            if (result < limit + digit)
            {
                break;
            }
            result -= digit;
        }
        if (negative)
        {
            if (i > 1)
            {
                return result;
            }
            else
            { /* Only got "-" */
                return 0;
            }
        }
        else
        {
            return -result;
        }
    }

    public boolean isNull()
    {
        return status == 0;
    }
    public boolean isTrue()
    {
        if (status == 0)
            return false;
        if (getInt() > 0)
            return true;
        if (getStr().length() > 1 || getStr().charAt(0) != '0') // TODO: "000" ist ein problem...
            return true;
        return false;
    }

    RayClass type = null;
    public void setType(RayClass className)
    {
        type = className;
    }
    public RayClass getType()
    {
        return type;
    }

}
