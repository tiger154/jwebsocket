/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.rc.plugin.arduino.program;

/**
 *
 * @author xdariel
 */
public class LedsProgram {
    
    
    public static Boolean[] parseLedState(char state) {

        Boolean[] status = null;
        switch (state) {
            case 'a':
                status = new Boolean[4];
                status[0] = true;
                status[1] = true;
                status[2] = true;
                status[3] = true;
                break;
            case 'b':
                status = new Boolean[4];
                status[0] = true;
                status[1] = true;
                status[2] = true;
                status[3] = false;
                break;
            case 'c':
                status = new Boolean[4];
                status[0] = true;
                status[1] = true;
                status[2] = false;
                status[3] = true;
                break;
            case 'd':
                status = new Boolean[4];
                status[0] = true;
                status[1] = true;
                status[2] = false;
                status[3] = false;
                break;
            case 'e':
                status = new Boolean[4];
                status[0] = true;
                status[1] = false;
                status[2] = true;
                status[3] = true;
                break;
            case 'f':
                status = new Boolean[4];
                status[0] = true;
                status[1] = false;
                status[2] = true;
                status[3] = false;
                break;
            case 'g':
                status = new Boolean[4];
                status[0] = true;
                status[1] = false;
                status[2] = false;
                status[3] = true;
                break;
            case 'h':
                status = new Boolean[4];
                status[0] = true;
                status[1] = false;
                status[2] = false;
                status[3] = false;
                break;
            case 'i':
                status = new Boolean[4];
                status[0] = false;
                status[1] = true;
                status[2] = true;
                status[3] = true;
                break;
            case 'j':
                status = new Boolean[4];
                status[0] = false;
                status[1] = true;
                status[2] = true;
                status[3] = false;
                break;
            case 'k':
                status = new Boolean[4];
                status[0] = false;
                status[1] = true;
                status[2] = false;
                status[3] = true;
                break;
            case 'l':
                status = new Boolean[4];
                status[0] = false;
                status[1] = true;
                status[2] = false;
                status[3] = false;
                break;
            case 'm':
                status = new Boolean[4];
                status[0] = false;
                status[1] = false;
                status[2] = true;
                status[3] = true;
                break;
            case 'n':
                status = new Boolean[4];
                status[0] = false;
                status[1] = false;
                status[2] = true;
                status[3] = false;
                break;
            case 'o':
                status = new Boolean[4];
                status[0] = false;
                status[1] = false;
                status[2] = false;
                status[3] = true;
                break;
            case 'p':
                status = new Boolean[4];
                status[0] = false;
                status[1] = false;
                status[2] = false;
                status[3] = false;
                break;
        }

        return status;


    }
}
