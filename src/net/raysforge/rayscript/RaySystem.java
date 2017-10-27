/*
 * Created on 23.05.2003
 */
package net.raysforge.rayscript;

import java.util.HashMap;

import net.raysforge.rayscript.rni.RayNative;


public class RaySystem
{
    public HashMap rayFiles = new HashMap();
    
    public RayFile rayFile;
    
    public static RayHook hook;
    
    public RaySystem( RayHook hook) throws Exception
    {
        RaySystem.hook = hook;
        rayFile = new RayFile( hook);
        RayNative.addNatives(rayFile, hook); // TODO: ergebnis speichern, damit schneller.
    }
    
    public void parse(char source[], RayHook hook) throws Exception
    {
        RayFile.parse(rayFile, source, hook, false);
    }

    public void execute()
    {
        rayFile.execute();
    }

    public void dump()
    {
        rayFile.dump();
    }

    public void parseCharArray( String name, char ar[]) throws Exception
    {
        rayFiles.put(name, RayFile.parse(ar, hook));
    }
    
    public void execute( String name)
    {
        RayFile rf = (RayFile) rayFiles.get(name);
        rf.execute();
    }   
}
