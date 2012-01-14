/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.kit;

/**
 *
 * @author markos0886
 */
public enum ChangeType {
	
	ADD(1),
	UPDATE(2),
	REMOVE(3),
	ENABLED(4),
	DISABLED(5);
	
	private int mChangeType;
	
	ChangeType(int aChangeType) {
		mChangeType = aChangeType;
	}
	
	public int getTypeChange() {
		return mChangeType;
	}
}
