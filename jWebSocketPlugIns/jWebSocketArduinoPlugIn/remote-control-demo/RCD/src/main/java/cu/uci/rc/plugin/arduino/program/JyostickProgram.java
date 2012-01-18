/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.rc.plugin.arduino.program;

/**
 *
 * @author xdariel
 */
public class JyostickProgram {
    
    public static Integer[] parseJyostickPosition(String data)
    {
     Integer[] positions = null;
       if( data.length() == 5)
       {
         positions =  new Integer[2];
         positions[0] = Integer.parseInt(  data.split("-")[0]);
         positions[1] = Integer.parseInt(  data.split("-")[1]);
         
       }
    return positions;
    
    }
    
}
