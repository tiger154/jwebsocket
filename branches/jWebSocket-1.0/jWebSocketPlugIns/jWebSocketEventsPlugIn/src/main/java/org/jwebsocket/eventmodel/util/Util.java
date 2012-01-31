//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javolution.util.FastList;
import org.json.JSONObject;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IServerSecureComponent;
import org.jwebsocket.eventmodel.exception.InvalidExecutionTime;
import org.jwebsocket.eventmodel.exception.NotAuthorizedException;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;
import org.springframework.util.StringUtils;

/**
 *
 * @author kyberneees
 */
public class Util {

	/**
	 * Generate a string unique token identifier (UTID)
	 * 
	 * @param aToken 
	 * @return The string unique token identifier
	 */
	public static String generateSharedUTID(Token aToken) throws Exception {
		JSONObject lJSON = JSONProcessor.tokenToJSON(aToken);
		lJSON.remove("utid");
		lJSON.remove("usid");

		char[] lChars = lJSON.toString().toCharArray();
		FastList<Character> lCharsList = new FastList<Character>();
		for (char lChar : lChars) {
			lCharsList.add((Character) lChar);
		}
		Collections.sort(lCharsList);

		return Tools.getMD5(lCharsList.toString().replace(", ", ","));
	}

	/**
	 * Takes a specific IP address and a range using the IP/Netmask (e.g. 192.168.1.0/24 or 202.24.0.0/14).
	 *
	 * @param aIpAddress the address to check
	 * @param aIpAddressRange the range of addresses that most contain the IP address
	 * @return true if the IP address is in the range of addresses.
	 */
	public static boolean isIpAddressInRange(String aIpAddress, String aIpAddressRange) {
		int lNMaskBits = 0;

		if (aIpAddress.indexOf('/') > 0) {
			String[] lAddressAndMask = StringUtils.split(aIpAddress, "/");
			aIpAddress = lAddressAndMask[0];
			lNMaskBits = Integer.parseInt(lAddressAndMask[1]);
		}

		InetAddress lRequiredAddress = parseAddress(aIpAddressRange);
		InetAddress lRemoteAddress = parseAddress(aIpAddress);

		if (!lRequiredAddress.getClass().equals(lRemoteAddress.getClass())) {
			throw new IllegalArgumentException("IP Address in expression must be the same type as "
					+ "version returned by request!");
		}

		if (lNMaskBits == 0) {
			return lRemoteAddress.equals(lRequiredAddress);
		}

		byte[] lRemAddr = lRemoteAddress.getAddress();
		byte[] lReqAddr = lRequiredAddress.getAddress();

		int lOddBits = lNMaskBits % 8;
		int lNMaskBytes = lNMaskBits / 8 + (lOddBits == 0 ? 0 : 1);
		byte[] lMask = new byte[lNMaskBytes];

		Arrays.fill(lMask, 0, lOddBits == 0 ? lMask.length : lMask.length - 1, (byte) 0xFF);

		if (lOddBits != 0) {
			int lFinalByte = (1 << lOddBits) - 1;
			lFinalByte <<= 8 - lOddBits;
			lMask[lMask.length - 1] = (byte) lFinalByte;
		}

		for (int lIndex = 0; lIndex < lMask.length; lIndex++) {
			if ((lRemAddr[lIndex] & lMask[lIndex]) != (lReqAddr[lIndex] & lMask[lIndex])) {
				return false;
			}
		}

		return true;
	}

	private static InetAddress parseAddress(String aAddress) {
		try {
			return InetAddress.getByName(aAddress);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Failed to parse the address '" + aAddress + "'!", e);
		}
	}

	public static List<String> parseStringArrayToList(String[] aArray) {
		FastList<String> lList = new FastList<String>();
		int lEnd = aArray.length;
		for (int lIndex = 0; lIndex < lEnd; lIndex++) {
			lList.add(aArray[lIndex]);
		}

		return lList;
	}

	/**
	 * Shutdown a ThreadPool and await for termination
	 *
	 * @param aExecutor the ThreadPool
	 * @param aAllowedTime the time to await for termination
	 * @throws Exception
	 */
	public static void shutdownThreadPoolAndAwaitTermination(ExecutorService aExecutor, int aAllowedTime) throws Exception {
		aExecutor.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!aExecutor.awaitTermination(aAllowedTime, TimeUnit.SECONDS)) {
				aExecutor.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!aExecutor.awaitTermination(aAllowedTime, TimeUnit.SECONDS)) {
					throw new Exception("Pool did not terminate!");
				}
				throw new InvalidExecutionTime("Threads uses more time than allowed for execution!");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			aExecutor.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
			throw ie;
		}
	}

	/**
	 * Check the security restrictions on a ISecureComponent. 
	 * <p>
	 * If the check fail, an exception is Thrown
	 *
	 * @throws Exception
	 */
	public static void checkSecurityRestrictions(IServerSecureComponent aSecureObject,
			WebSocketConnector aConnector, boolean aIsAuthenticated, String aUsername, List<String> aAuthorities) throws Exception {
		//Leaving if the security checks are not enabled
		if (!aSecureObject.isSecurityEnabled()) {
			return;
		}

		//NOT '!' operator flag
		boolean lExclusion = false;

		//Temporal variables
		Iterator<String> lIterator;
		String lValue;
		boolean lAuthorityAuthorized = false, lIpAuthorized = false,
				lUserAuthorized = false, lUserMatch = false, lStop = false;

		//Processing ip addresses restrictions
		if (aSecureObject.getIpAddresses().size() > 0) {
			lIterator = aSecureObject.getIpAddresses().iterator();
			while (lIterator.hasNext()) {
				lValue = lIterator.next();

				if (!lValue.equals("all")) {
					lExclusion = (lValue.startsWith("!")) ? true : false;
					lValue = (lExclusion) ? lValue.substring(1) : lValue;

					if (Util.isIpAddressInRange(aConnector.getRemoteHost().getHostAddress(), lValue)) {
						lIpAuthorized = (lExclusion) ? false : true;
						break;
					}
				} else {
					lIpAuthorized = true;
					break;
				}
			}
			if (!lIpAuthorized) {
				throw new NotAuthorizedException("Your IP address '"
						+ aConnector.getRemoteHost().getHostAddress()
						+ "' is not authorized to execute the operation!");
			}
		}

		//Processing users
		if (aSecureObject.getUsers().size() > 0) {
			if (aIsAuthenticated) {
				lIterator = aSecureObject.getUsers().iterator();
				while (lIterator.hasNext()) {
					lValue = lIterator.next();	//Required USER

					if (!lValue.equals("all")) {
						lExclusion = (lValue.startsWith("!")) ? true : false;
						lValue = (lExclusion) ? lValue.substring(1) : lValue;

						if (lValue.equals(aUsername)) {
							lUserMatch = true;
							if (!lExclusion) {
								lUserAuthorized = true;
								break;
							}
						}
					} else {
						lUserMatch = true;
						lUserAuthorized = true;
						break;
					}
				}
			}
			//Not authorized!
			if (!lUserAuthorized && lUserMatch || aSecureObject.getRoles().isEmpty()) {
				throw new NotAuthorizedException("Invalid credentials to execute the operation!");
			}
		}

		//Processing roles restrictions
		if (aSecureObject.getRoles().size() > 0) {
			if (aIsAuthenticated) {
				Iterator<String> lUserAuthorities = aAuthorities.iterator();
				String lAuthority;
				while (lUserAuthorities.hasNext()) {
					//Required ROLES iteration
					lIterator = aSecureObject.getRoles().iterator();
					while (lIterator.hasNext() && !lStop) {
						lAuthority = lUserAuthorities.next();	//User ROLE
						lValue = lIterator.next();	//Required ROLE

						if (!lValue.equals("all")) {
							lExclusion = (lValue.startsWith("!")) ? true : false;
							lValue = (lExclusion) ? lValue.substring(1) : lValue;

							if (lValue.equals(lAuthority)) {
								if (!lExclusion) {
									lAuthorityAuthorized = true; //Authorized!
								}
								lStop = true;
							}
						} else {
							lAuthorityAuthorized = true;
							lStop = true;
						}
					}
					if (lStop) {
						break;
					}
				}
			}
			//Not authorized!
			if (!lAuthorityAuthorized) {
				throw new NotAuthorizedException("Invalid credentials to execute the operation!");
			}
		}
	}
}
