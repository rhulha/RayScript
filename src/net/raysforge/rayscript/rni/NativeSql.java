/*
 * Created on 08.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.raysforge.rayscript.rni;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.raysforge.commons.FileUtils;
import net.raysforge.commons.RayString;
import net.raysforge.rayscript.RayClass;
import net.raysforge.rayscript.RayFile;
import net.raysforge.rayscript.RayHook;
import net.raysforge.rayscript.RayRef;
import net.raysforge.rayscript.RayRefVector;
import net.raysforge.rayscript.RayScript;
import net.raysforge.rayscript.RaySystem;
import net.raysforge.rayscript.RayUtils;

public class NativeSql extends NativeObject implements NativeClass
{
    Connection con;
    PreparedStatement sth;
    ResultSet rs;

    public NativeClass getNativeClass() throws Exception
    {
        return new NativeSql();
    }

    public RayClass getNativeRayClass(RayHook hook) throws Exception
    {
        return getNativeRayClass_(hook);
    }

    public static RayClass getNativeRayClass_(RayHook hook) throws Exception
    {
        RayClass rayClass = null;
        char source[] = FileUtils.readCompleteFile(new File(RayScript.class.getResource("objtree/Sql.ray").getFile()), "utf-8").toString().toCharArray();
        RayFile rf = RayFile.parse(source, hook, false);
        rayClass = (RayClass) rf.classes.get(new RayString("Sql"));
        rayClass.nativeClass = new NativeSql();
        return rayClass;
    }

    public static RayRef getNewRayRef(NativeSql current, RayHook hook)
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
            e.printStackTrace();
        }
        return rayRef;
    }

    // *******************

    public void addDriver(String dbDriver)
    {
        try
        {
            Class.forName(dbDriver);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void getConn(String dburl) // TODO: connection pool !
    {
        try
        {
            if (RaySystem.hook != null)
                con = RaySystem.hook.getConnection(dburl);
            else
                con = DriverManager.getConnection(dburl);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void prepare(String query)
    {
        try
        {
            sth = con.prepareStatement(query);
        }
        catch (SQLException e)
        {

            e.printStackTrace();
        }
    }

    public void execute(RayRefVector params)
    {
        try
        {
            for (int i = 0; i < params.size(); i++)
            {
                sth.setString(i + 1, params.getNativeString(i).toString());
            }
            rs = sth.executeQuery();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static int countRows(ResultSet rs) throws SQLException
    {
        int a = rs.getRow();
        rs.last();
        int b = rs.getRow();
        //        if( b > 0)          rs.absolute(a);
        rs.beforeFirst();
        return b;
    }

    public NativeStringArray getTables() throws SQLException
    {
        String[] st = { "TABLE" }; // # ordinary tables #
        DatabaseMetaData dbmd = con.getMetaData();
        ResultSet dbmdrs = dbmd.getTables(null, null, "%", st);
        ResultSetMetaData rsmd = dbmdrs.getMetaData();
        dbmdrs.last();
        int rows = dbmdrs.getRow(); // rows is number of tables in db.
        dbmdrs.beforeFirst(); // undo changes to rs position.

        NativeStringArray na = new NativeStringArray();
        NativeString[] col = new NativeString[rows];

        int i = 0;
        while (dbmdrs.next())
        {
            col[i++] = new NativeString().setStr(dbmdrs.getString("TABLE_NAME"));
        }
        dbmdrs.close();
        na.setArray(col);
        return na;
    }

    public NativeStringArray getColumns(String table) throws SQLException
    {
        DatabaseMetaData dbmd = con.getMetaData();
        ResultSet dbmdrs = dbmd.getColumns(null, null, table, "%");
        ResultSetMetaData rsmd = dbmdrs.getMetaData();
        dbmdrs.last();
        int rows = dbmdrs.getRow(); // rows is number of tables in db.
        dbmdrs.beforeFirst(); // undo changes to rs position.

        NativeStringArray na = new NativeStringArray();
        NativeString[] col = new NativeString[rows];

        int i = 0;
        while (dbmdrs.next())
        {
            col[i++] = new NativeString().setStr(dbmdrs.getString("COLUMN_NAME"));
        }
        dbmdrs.close();
        na.setArray(col);
        return na;
    }

    public NativeStringArray fetchRow()
    {
        NativeStringArray na = new NativeStringArray();
        try
        {
            int c = rs.getMetaData().getColumnCount();
            NativeString[] col = new NativeString[c];
            if (rs.next())
            {
                for (int i = 0; i < c; i++)
                {
                    col[i] = new NativeString();
                    col[i].setStr(rs.getString(i + 1));
                }
                na.setArray(col);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return na;
    }

    public void close()
    {
        try
        {
            if (rs != null)
                rs.close();
            if (sth != null)
                sth.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void closeConn()
    {
        try
        {
            con.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static RayRef execFunc(RayRef ref, String func, RayRefVector params, RayHook hook)
    {
        //        hook.print(current.toString() + ":" + func + " " + params[0].toString());
        if (ref == null)
            RayUtils.RunExp("ref == null");
        NativeSql current = (NativeSql) ref.getType().nativeClass;

        try
        {
            if (func.equals("addDriver"))
            {
                current.addDriver(params.getNativeString(0).toString());
            }
            else if (func.equals("getConn"))
            {
                current.getConn(params.getNativeString(0).toString());
            }
            else if (func.equals("getTables"))
            {
                NativeStringArray na = current.getTables();
                return NativeStringArray.getNewRayRef(na, hook);
            }
            else if (func.equals("getColumns"))
            {
                NativeStringArray na = current.getColumns(params.getNativeString(0).toString());
                return NativeStringArray.getNewRayRef(na, hook);
            }
            else if (func.equals("prepare"))
            {
                current.prepare(params.getNativeString(0).toString());
            }
            else if (func.equals("execute"))
            {
                current.execute(params);
            }
            else if (func.equals("rowCount"))
            {
                return NativeString.getNewRayRef("" + NativeSql.countRows(current.rs), hook);
            }
            else if (func.equals("fetchRow"))
            {
                NativeStringArray na = current.fetchRow();
                return NativeStringArray.getNewRayRef(na, hook);
            }
            else if (func.equals("close"))
            {
                current.close();
            }
            else if (func.equals("closeConn"))
            {
                current.closeConn();
            }
            else
            {
                return null;
                //            System.err.println("sql, unknown native function " + func);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return ref;
    }

    public RayRef call(RayString className, RayString func, RayRef instance, RayRefVector parameterValues, RayHook hook)
    {
        if (className.equals("Sql"))
        {
            RayRef r = execFunc(instance, "" + func, parameterValues, hook);
            return r != null ? r : super.call(className, func, instance, parameterValues, hook);
        }
        throw new RuntimeException();
    }
}
