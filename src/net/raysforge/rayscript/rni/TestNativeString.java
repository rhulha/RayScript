/*
 * Created on 08.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.raysforge.rayscript.rni;

/**
 * @author Superstar
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TestNativeString
{
    public static void main(String[] args)
    {
        NativeString a = new NativeString();
        test(a);

        a.setStr("" + 5);
        test(a);

        a.setInt(5);
        test(a);

        a.setInt(0);
        test(a);

        a.setStr("");
        test(a);

        a.setStr("0");
        test(a);

        a.setStr("000");
        test(a);

        a.setStr("test");
        test(a);

        a.setStr("0test");
        test(a);

        a.setStr("53-2test");
        test(a);

        a.setStr("-0test");
        test(a);

        a.setStr("-5test");
        test(a);

    }
    private static void test(NativeString a)
    {
        System.out.println("{");
        System.out.println("value  : " + a);
        System.out.println("isTrue : " + a.isTrue());
        System.out.println("isNull : " + a.isNull());
        System.out.println("getInt : " + a.getInt());
        System.out.println("getStr : " + a.getStr());
        System.out.println("getSoN : " + a.getStrOrNull());
        System.out.println("}");
    }

}
