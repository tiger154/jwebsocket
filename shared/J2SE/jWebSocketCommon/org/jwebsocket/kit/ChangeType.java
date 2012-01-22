
package org.jwebsocket.kit;

/**
 *
 * @author Marcos Antonio González Huerta (markos0886, UCI)
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
