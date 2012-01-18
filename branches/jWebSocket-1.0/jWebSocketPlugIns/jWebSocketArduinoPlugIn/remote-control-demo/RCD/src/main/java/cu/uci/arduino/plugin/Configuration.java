/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.arduino.plugin;

import processing.app.Preferences;

/**
 *
 * @author xdariel
 */
public class Configuration extends Preferences {

    private static  String pref_path;

    public static void init(String path) {
        
        Configuration.pref_path = path;        
        Preferences.init(Configuration.pref_path);
    }
    
  
    public static String getPath() {
        return pref_path;
    }
 
    public static void setPpath(String pref_path) {
        Preferences.init(pref_path);
    }
   
}
