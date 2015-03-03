//	---------------------------------------------------------------------------
//	jWebSocket HashUtils (Community Edition, CE)
//	---------------------------------------------------------------------------
//	Copyright 2010-2014 Innotrade GmbH (jWebSocket.org)
//	Alexander Schulze, Germany (NRW)
//
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//	---------------------------------------------------------------------------
package org.jwebsocket.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Hashing calculation utility class
 *
 * @author kyberneees
 */
public class HashUtils {

	/**
	 * Get the MD5 hash calculation for a given byte array value
	 *
	 * @param aByteArray
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String strToMD5(byte[] aByteArray) throws NoSuchAlgorithmException {
		return strToHash(aByteArray, "MD5");
	}

	/**
	 * Get the SHA-1 hash calculation for a given byte array value
	 *
	 * @param aByteArray
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String strToSHA1(byte[] aByteArray) throws NoSuchAlgorithmException {
		return strToHash(aByteArray, "SHA-1");
	}

	/**
	 * Get the SHA-512 hash calculation for a given byte array value
	 *
	 * @param aByteArray
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String strToSHA512(byte[] aByteArray) throws NoSuchAlgorithmException {
		return strToHash(aByteArray, "SHA-512");
	}

	/**
	 * Get the SHA-384 hash calculation for a given byte array value
	 *
	 * @param aByteArray
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String strToSHA384(byte[] aByteArray) throws NoSuchAlgorithmException {
		return strToHash(aByteArray, "SHA-384");
	}

	/**
	 * Get the SHA-256 hash calculation for a given byte array value
	 *
	 * @param aByteArray
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String strToSHA256(byte[] aByteArray) throws NoSuchAlgorithmException {
		return strToHash(aByteArray, "SHA-256");
	}

	/**
	 * Get the target hash algorithm calculation for a given byte array
	 *
	 * @param aByteArray
	 * @param hashAlgorithm The hashing standard algorithm name
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String strToHash(byte[] aByteArray, String hashAlgorithm) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
		byte[] digest = md.digest(aByteArray);

		return byteArrayToHex(digest);
	}

	/**
	 * Get the hex string representation of a given byte array
	 *
	 * @param hash
	 * @return
	 */
	public static String byteArrayToHex(final byte[] hash) {
		String result;
		Formatter formatter = new Formatter();
		try {
			for (byte b : hash) {
				formatter.format("%02x", b);
			}
			result = formatter.toString();
		} catch (Exception lEx) {
			throw new RuntimeException(lEx);
		}
		return result;
	}
}
