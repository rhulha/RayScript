/**
 * @author Paranoid Ray
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
package net.raysforge.rayscript;

import java.sql.Connection;

public interface RayHook {
   public void print(Object s);
   public void alert(Object s);
   public RayFile getParseTree();
   public Connection getConnection( String url);
}
