/*
 * Used to give more performance to the server, whenever a new message comes 
 * from the client will be incremented the incoming or the outgoing information
 * respectively
 */
package org.jwebsocket.plugins.monitoring.util;

/**
 *
 * @author vbarzana
 */
public class PlugInObjectInMemory {

	private String mNamespace;
	private String mPlugInId;
	private Integer mIncoming;
	private Integer mOutgoing;

	public PlugInObjectInMemory(String mPlugInId, String mNamespace) {
		this.mNamespace = mNamespace;
		this.mPlugInId = mPlugInId;
		this.mIncoming = 0;
		this.mOutgoing = 0;
	}

	public void incrementIncoming() {
		this.mIncoming = this.mIncoming + 1;
	}

	public void incrementOutgoing() {
		this.mOutgoing = this.mOutgoing + 1;
	}

	public String getNamespace() {
		return mNamespace;
	}

	public void setNamespace(String mNamespace) {
		this.mNamespace = mNamespace;
	}

	public String getPlugInId() {
		return mPlugInId;
	}

	public void setPlugInId(String mPlugInId) {
		this.mPlugInId = mPlugInId;
	}

	public Integer getIncoming() {
		return mIncoming;
	}

	public void setmIncoming(Integer mIncoming) {
		this.mIncoming = mIncoming;
	}

	public Integer getOutgoing() {
		return mOutgoing;
	}

	public void setOutgoing(Integer mOutgoing) {
		this.mOutgoing = mOutgoing;
	}
}
