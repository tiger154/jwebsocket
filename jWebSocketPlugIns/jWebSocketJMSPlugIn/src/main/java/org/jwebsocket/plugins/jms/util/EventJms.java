package org.jwebsocket.plugins.jms.util;

public enum EventJms {
	HANDLE_TEXT("handleJmsText"), HANDLE_TEXT_MESSAGE("handleJmsTextMessage"), HANDLE_MAP("handleJmsMap"), HANDLE_MAP_MESSAGE(
			"handleJmsMapMessage");
	private String mValue;

	private EventJms(String value) {
		this.mValue = value;
	}

	public String getValue() {
		return mValue;
	}

	public boolean equals(String aAction) {
		return mValue.equals(aAction);
	}

	@Override
	public String toString() {
		return mValue;
	}
}
