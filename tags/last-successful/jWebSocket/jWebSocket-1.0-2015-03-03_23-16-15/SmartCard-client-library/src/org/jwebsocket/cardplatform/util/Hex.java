package org.jwebsocket.cardplatform.util;


public class Hex {

     /**
      * From http://stackoverflow.com/questions/140131/
      * convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
      *
      *
      * @param A hex string value
      * @return
      */
	public static byte[] hexStringToByteArray(String aString) {
		int lLength = aString.length();
		byte[] lData = new byte[lLength / 2];
		for (int lIndex = 0; lIndex < lLength; lIndex += 2) {
		  lData[lIndex / 2] =
                     (byte) ((Character.digit(aString.charAt(lIndex), 16) << 4)
			     + Character.digit(aString.charAt(lIndex + 1), 16));
		}
		return lData;
	}
	static final String HEXES = "0123456789ABCDEF";

	/**
	 * From http://www.rgagnon.com/javadetails/java-0596.html
	 * 
	 * @param aByteArray
	 * @return 
	 */
	public static String hexByteArrayToString(byte[] aByteArray) {
		if (aByteArray == null) {
			return null;
		}
		final StringBuilder lHexBuilder =
                        new StringBuilder(2 * aByteArray.length);
		for (final byte lByte : aByteArray) {
		     lHexBuilder.append(HEXES.charAt(
                     (lByte & 0xF0) >> 4)).append(HEXES.charAt((lByte & 0x0F)));
		}
		return lHexBuilder.toString();
	}
}
