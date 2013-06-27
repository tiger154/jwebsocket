/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.sharedobjects;

import java.util.List;
import javolution.util.FastList;

/**
 *
 * @author aschulze
 */
public class SharedCanvas extends BaseSharedObject {

	private List mCmds = new FastList();

	/**
	 *
	 * @param aCmd
	 */
	public void addCmd(String aCmd) {
		 mCmds.add(aCmd);
	}

	/**
	 *
	 * @param aIndex
	 */
	public void removeCmd(int aIndex) {
		 mCmds.remove(aIndex);
	}
}
