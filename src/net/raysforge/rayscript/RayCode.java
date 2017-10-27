package net.raysforge.rayscript;

import java.util.HashMap;

import net.raysforge.commons.IntUtils;
import net.raysforge.commons.RayString;
import net.raysforge.rayscript.rni.NativeString;

public class RayCode extends RaySource
{
    //    public RayString codeStr= new RayString();
    HashMap vars;
    RayHook hook;
    public RayFunction parent = null;
    HashMap parameters = null;
    RayRef thisrv = new RayRef();

    public RayCode(RayHook hook)
    {
        this.hook = hook;
        //        vars = new HashMap();  // see execute
    }

    protected Object clone()
    {
        RayCode rcode = new RayCode(hook);
        rcode.parent = parent;
        rcode.pos = 0;
        rcode.src = src; // TODO: copy ?
        return rcode;
    }

    public RayRef execute(HashMap parameters)
    {
        return execute(parameters, null);
    }

    public RayRef execute(HashMap parameters, HashMap localVars)
    {
        vars = localVars;
        if (vars == null)
        {
            vars = new HashMap();
        }
        this.parameters = parameters;

        RayRef rv = null, temp;
        int i = 0;
        pos = 0;
        try
        {
            while (pos < src.length)
            {
                if ((temp = parseVar()) == null)
                    break;
                if (!temp.isNull())
                    rv = temp;
                if (i++ > 900) // TODO: kill
                    throw new RuntimeException("infinite loop");
            }
        }
        catch (EndOfCodeException e)
        {
            //            System.out.println("---");
        }
        if (rv == null)
            System.out.println("-> rv == null");
        if (rv != null && rv.isNull())
            System.out.println("-> rv.isNull()");
        return rv;
    }

    public RayRef parseVar() throws EndOfCodeException
    {
        RayRef current = NativeString.getNewRayRef("", hook); // TODO: null statt "" ?, war: new RayRef();
        RayString token = RayUtils.getSourceToken(this);
        boolean createVar = false;

        //        if (token == null)            return null;
        if (token.equals("var"))
        {
            createVar = true;
            token = RayUtils.getSourceToken(this);
        }
        //        if (token == null)            return null;
        if (token.equals(""))
        {
            return NativeString.getNewRayRef("", hook);
        }
        else if (token.equals(")"))
        {
            pos--;
        }
        else if (token.equals(";"))
        {
            return new RayRef(); // um eine funktion etwas zur�ckgeben zu lassen.
        }
        else if (token.equals("new"))
        {
            current = handleNew( current);
        }
        else if (token.equals("if"))
        {
            handleIf();
        }
        else if (token.equals("while"))
        {
            handleWhile();
        }
        else if (token.equals("foreach"))
        {
            handleForeach();
        }
        else if (token.equals("exit"))
        {
            throw new EndOfCodeException();
        }
        else
        {
            current = handleVariable(current, token, createVar);
        }
        //        if (!current.isNull()) System.out.println(token + ":" + current);
        return current;
    }

    private RayRef handleVariable(RayRef current, RayString token, boolean createVar) throws EndOfCodeException
    {
        if (token.charAt(0) == '-')
        {
            token = RayUtils.getSourceToken(this);
            NativeString tns = new NativeString().setInt(IntUtils.parseInt("-" + token));
            RayClass trc = NativeString.getNewRayRef("" + tns, hook).getType();
            current.setType(trc);
        }
        else if (Character.isDigit(token.charAt(0)))
        {
            NativeString tns = new NativeString().setInt(IntUtils.parseInt("" + token));
            RayClass trc = NativeString.getNewRayRef("" + tns, hook).getType();
            current.setType(trc);
        }
        else if (token.charAt(0) == '"' || token.charAt(0) == '\'')
        {
            if (token.charAt(0) == '"')
                token = interpolate(token);
            char ca[] = token.getCharArray();

            NativeString tns = new NativeString().setStr(new String(ca, 1, ca.length - 2));
            RayClass trc = NativeString.getNewRayRef("" + tns, hook).getType();
            current.setType(trc);
        }
        else if (token.startsWithLetter())
        {
            if (token.equals("this"))
            {
                current = thisrv;
                current.setType(this.parent.parent);
            }
            else
            {
                current = getVar(current, token, createVar);
            }
        }
        else
        {
            throw new RuntimeException("error zeichen an pos: " + pos + " nicht erwartet: '" + token + "'");
        }
        RayUtils.eatSpaces(this);
        if (pos >= src.length);
        else if (src[pos] == '=')
        {
            // Infos was hier passiert siehe: RayVar.txt
            pos++;
             current.assign(parseVar());
            //            current.assign( (RayVar) parseVar().clone());
//            current = parseVar();
//            vars.put(token, current); // dem token die neue Ref zuweisen ( sonst hat er noch die alte )
        }
        else
        {
            while (pos < src.length && src[pos] == '.')
            {
                pos++;
                current = parseCalls(current);
                if (current == null)
                    break;
            }
        }
        return current;
    }

    private RayRef getVar(RayRef current, RayString token, boolean createVar)
    {
        if (vars.containsKey(token))
            return (RayRef) vars.get(token);
        if (createVar)
        {
            vars.put(token, current);
            return current;
        }
        if (parameters != null && parameters.containsKey(token))
            return (RayRef) parameters.get(token);
        RayClass rc = null;
        if (parent != null)
            rc = parent.parent;
        while (rc != null)
        {
            //            System.err.println(rc.vars.hashCode());
            if (rc.vars.containsKey(token))
                return (RayRef) rc.vars.get(token);
            if (rc.staticVars.containsKey(token))
                return (RayRef) rc.staticVars.get(token);
            rc = rc.extendsClass;
        }
        hook.print("error: undefined variable found:" + token);
        throw new RuntimeException("error: undefined variable found:" + token);
    }

    public RayRef parseCalls(RayRef current) throws EndOfCodeException
    {
        RayRefVector params = new RayRefVector();
        RayString funcName = RayUtils.getSourceToken(this);
        expect('(');
        RayString token;
        if (!RayUtils.getSourceToken(this, true).equals(")")) // damit params.size auch mal 0 ist.
            do
            {
                params.add(parseVar());
            }
            while ((token = RayUtils.getSourceToken(this)).equals(","));
        else
            token = RayUtils.getSourceToken(this);

        if (token.charAt(0) != ')')
            throw new RuntimeException("no closing brace for function call: " + token);
        if (current.type == null) // Variable ist vom Typ Basis-Klasse
        {
            throw new RuntimeException("current.type == null");
            //    return NativeString.execFunc(current, "" + funcName, params, hook);
        }
        else // Variable ist vom Typ User-Defined-Class
            return current.type.classFuncCall(funcName, params, current);
    }

    private RayRef handleNew(RayRef current) throws EndOfCodeException
    {
        RayString token;
        RayRefVector params = new RayRefVector();
        RayString className = RayUtils.getSourceToken(this); // classIdentifier
        if (!hook.getParseTree().classes.containsKey(className))
            throw new RuntimeException("unknown Class: " + className);
        expect('(');
        do
        {
            params.add(parseVar());
        }
        while ((token = RayUtils.getSourceToken(this)).equals(","));
        if (token.charAt(0) != ')') // TODO: use expect()
            throw new RuntimeException("no closing brace for Constructor call: " + token);
        RayClass rc = (RayClass) hook.getParseTree().classes.get(className);
        try
        {
            //current.setType(rc);
            current.setType(rc.instantiate());
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (current.type.functions.containsKey(new RayString("init"))) // TODO: else search extends tree ?
            current.type.classFuncCall(new RayString("init"), params, current); // TODO: in instantiate verschieben !
        return current;
    }

    private void handleIf() throws EndOfCodeException
    {
        char c;
        expect('(');
        RayRef val = parseVar();
        expect(')');
        expect('{'); // TODO: parse until ';'

        RayCode rc1 = RayCode.parse(this, hook, this.parent);

        boolean skipRest = false;
        if (val.isTrue())
        {
            skipRest = true;
            rc1.execute(parameters, vars);
        }

        handleElse(skipRest);
    }

    private void handleElse(boolean skipRest) throws EndOfCodeException
    {
        if (skipRest)
        {
            while (RayUtils.getSourceToken(this, true).equals("else"))
            {
                //    RayUtils.getSourceToken(this, false);
                while (src[pos] != '{')
                    pos++;
                pos++;
                RayUtils.getInnerText(this, '{', '}'); // nur zum �berspringen
            }
        }
        else
        {
            if (RayUtils.getSourceToken(this, true).equals("else"))
            {
                RayUtils.getSourceToken(this, false); // else

                if (RayUtils.getSourceToken(this, true).equals("{"))
                {
                    RayUtils.getSourceToken(this, false); // {
                    RayCode.parse(this, hook, this.parent).execute(parameters, vars);
                }
            }
        }
    }

    private void handleWhile() throws EndOfCodeException
    {
        char c;
        expect('(');

        int condition_pos = pos;
        RayString code = RayUtils.getInnerText(this, '(', ')');
        //        pos++;
        expect('{'); // TODO: parse until ';'

        RayCode rc1 = RayCode.parse(this, hook, this.parent);

        int loop_end_pos = pos;
        int i = 0;
        pos = condition_pos;

        boolean skipRest = false;
        while (parseVar().isTrue())
        {
            skipRest = true;
            if (i++ > 900) // TODO: kill
                throw new RuntimeException("schleife evtl. unendlich");
            rc1.execute(parameters, vars);
            rc1.pos = 0;
            pos = condition_pos;
        }
        pos = loop_end_pos;
        handleElse(skipRest);
    }

    private void handleForeach() throws EndOfCodeException
    {
        RayString keystr = RayUtils.getSourceToken(this);
        RayRef key = NativeString.getNewRayRef("", hook); // new RayRef();
        key = getVar(key, keystr, true);
        expect('(');
        RayRef array = parseVar();
        expect(')');
        expect('{'); // TODO: parse until ';'

        RayCode rc1 = RayCode.parse(this, hook, this.parent);

        int i = 0;
        boolean skipRest = false;

        while (i < array.getNativeArray().array.length)
        {
            skipRest = true;
            key.type.nativeClass = array.getNativeArray().array[i];
            //           key.getNativeString().assign(array.getNativeArray().array[i]);
            if (i++ > 900) // TODO: kill
                throw new RuntimeException("schleife evtl. unendlich");
            rc1.execute(parameters, vars);
            rc1.pos = 0;
        }
        handleElse(skipRest);
    }

    private RayString interpolate(RayString token)
    {
        // Diese Funktion macht aus: a = 5; "Hallo $a".print(); => Hallo 5
        // Sie ersetzt in Strings $var durch den Inhalt, wie bei Perl.
        // Bei ${code} wird ergebnis des codes eingef�gt ! Cool gell :-)
        // TODO: \n geht noch nicht !!!
        // @array -> array.setJoinChar(",");  // besser nicht: ${array.join(",")} is viel besser !
        // TODO: optimize: check out if there is any $ there if not, return original string !
        char str[] = token.getCharArray();
        RayString result = new RayString();
        int varStart, varEnd, textStart = 0, textEnd;
        int pos = 0;
        while (pos < token.l())
        {
            textStart = pos;
            pos = token.index("$", pos);
            if (pos == -1)
                break;
            if (str[pos + 1] == '{') // TODO: {
            {
                varStart = pos + 2;
                textEnd = pos - 1;
                pos++;
                while (pos < token.l())
                {
                    if (str[pos] == '}')
                        break;
                    pos++;
                }
                varEnd = pos;
                result.appendInPlace(new RayString(str, textStart, textEnd + 1));

                RayCode rc1 = new RayCode(hook);
                rc1.src = new RayString(str, varStart, varEnd).getCharArray();
                //                System.out.print("x-");
                //                System.out.println(rc1.src);
                RayRef retval = rc1.execute(parameters, vars);
                result.appendInPlace(retval.getNativeString().getStr());
                pos++;
            }
            else
            {
                varStart = pos + 1;
                textEnd = pos - 1;
                pos++;
                while (pos < token.l())
                {
                    if (!Character.isLetterOrDigit(str[pos]))
                        break;
                    pos++;
                }
                varEnd = pos;
                result.appendInPlace(new RayString(str, textStart, textEnd + 1));
                RayString rsVar = new RayString(str, varStart, varEnd);
                RayRef ref = getVar(null, rsVar, false);

                result.appendInPlace(ref.getNativeString().getStr());
            }
        }
        result.appendInPlace(new RayString(str, textStart, token.length()));
        return result;
    }

    public void expect(char e) throws EndOfCodeException
    {
        char c;
        if ((c = RayUtils.getSourceToken(this).charAt(0)) != e)
            throw new RuntimeException("erwartetes Zeichen: '" + e + "'" + " nicht gefunden; statt dessen: " + c);
    }

    public void expect_old(char e) throws EndOfCodeException
    {
        RayUtils.eatSpacesAndReturns(this);
        //              hook.print(""+ src[pos]);
        if (src[pos] != '{')
            throw new RuntimeException("keine geschweifte Klammer auf nach while '" + src[pos] + "'");
        pos++;
    }

    public void expect(String s) throws EndOfCodeException
    {
        RayString token;
        if (!(token = RayUtils.getSourceToken(this)).equals(s))
            throw new RuntimeException("erwarteter String: '" + s + "'" + " nicht gefunden; statt dessen: " + token);
    }

    public void dump(RayHook rhook)
    {
        RayHook rh;
        if (rhook != null)
            rh = rhook;
        else if (hook != null)
            rh = hook;
        else
            throw new RuntimeException("hook net set in RayCode.dump");
        //        rh.print("// vars hashCode: " + vars.hashCode());
        //        rh.print("// parent hashCode: " + parent.hashCode());
        rh.print(new String(src));
    }
    protected static RayCode parse(RaySource rs, RayHook hook, RayFunction parent) throws EndOfCodeException
    {
        RayString code = RayUtils.getInnerText(rs, '{', '}');
        RayCode rc = new RayCode(hook);
        rc.src = code.getCharArray();
        rc.parent = parent;
        return rc;
    }
}
