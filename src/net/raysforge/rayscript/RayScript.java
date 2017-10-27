/* 
 *TODO: RayScript

 optional if ... then ohne klammer
 // if nextToken != '(' readUntil('then');

  unterscheidung: clone == cloneComplete, cloneRef
  alle kinder clonen, nur ref klonen
  
  instanceof
  
  qq!dfsdf!.print();
  
  vererbung bei den natives
  kombi mit class funcs: insertHash bei Sql zB.
  
 StringArray extends Array extends Object. => dh Vector besteht in Zukunft aus RayRefs !!!
 und nicht mehr aus NativeArrays, StringArray muss dann halt immer konvertieren!
 else super.exec(params);
 
 class a
 {
   var b = parseVar();
 }
 
 * continue, !! break !!; next, last.

 * *user arrays []

 * Html v2 !

 * Array a = #sql{select names from list order by names};
 * return
 * "a.append(b)".eval();
 *
 *sinnvolle array funcs: sort, join, map, grep, sum
 *
 *super
 *this testen
 *perls <=>, cmp, index("$", pos);
 *
 *closures: 4.times(){ "Hallo".print() }
 *
 * var a = if(b) {c} else {d};
 * 
 * konstructor (super), destructor, 
 * static functions ?
 * packages
 * exceptions
 * for( var a = 0; a < b; a.inc() )
 * enums !
 * foreach VAR key fehlt noch...

 * */

package net.raysforge.rayscript;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import net.raysforge.commons.FileUtils;
import net.raysforge.commons.SimpleConnectionPool;

public class RayScript extends JFrame implements ActionListener, RayHook
{
    private String filename = "test1.ray";
    JTextArea ta = new JTextArea();
    JTextArea msga = new JTextArea();
    JPanel bp = new JPanel();
    JButton exe = new JButton("Execute");
    JButton dump = new JButton("Dump");

    RaySystem raySys;

    public static void main(String[] args) throws Exception
    {
        new RayScript();
    }

    public RayScript() throws Exception
    {
        super("PowerObjects 1.0");
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        setSize(600, 600);
        setLocation(300, 10);
        dump.addActionListener(this);
        bp.add(dump);
        exe.addActionListener(this);
        bp.add(exe);
        JScrollPane jScrollPane = new JScrollPane(ta);
		JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jScrollPane, new JScrollPane(msga));
        sp.setDividerLocation(460);
        //ta.setPreferredSize(new Dimension(600, 400));
        ta.setFont(new Font("Courier", Font.PLAIN, 14));
        getContentPane().add(bp, BorderLayout.SOUTH);
        getContentPane().add(sp, BorderLayout.CENTER);
        setVisible(true);
        ta.setText(new String(FileUtils.readCompleteFile(new File(RayScript.class.getResource(filename).getFile()))));
        raySys = new RaySystem(this);
    } // # Constructor #

    public void actionPerformed(ActionEvent e)
    {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        msga.setText("");
        try
        {
            raySys.parse(ta.getText().toCharArray(), this);
        }
        catch (EndOfCodeException e1)
        {
            e1.printStackTrace();
            return;
        }
        catch (Exception e33)
        {
            e33.printStackTrace();
            return;
        }

        if (e.getSource() == dump)
            raySys.dump();
        if (e.getSource() == exe)
        {
            raySys.execute();
        }
    }

    public void print(Object s)
    {
        System.out.println(s);
        msga.append(s + "\n");
    }

    public void alert(Object s)
    {
        //      System.out.println( s + " " + pos);
        JOptionPane.showMessageDialog(this, s);
    }

    public RayFile getParseTree()
    {
        return raySys.rayFile;
    }

    private SimpleConnectionPool scp = new SimpleConnectionPool();
    public Connection getConnection(String dburl)
    {
        return scp.getConnection(dburl);
    }

} // # public class #
// # EOF #
