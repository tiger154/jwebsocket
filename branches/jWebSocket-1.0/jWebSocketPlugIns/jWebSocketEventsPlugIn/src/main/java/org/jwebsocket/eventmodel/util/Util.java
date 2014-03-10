//	---------------------------------------------------------------------------
//	jWebSocket - Util (Community Edition, CE)
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
import org.jwebsocket.api.IEmbeddedAuthentication;
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
 * @author Rolando Santamaria Maso
 */
public class Util {

	/**
	 * Generate a string unique token identifier (UTID)
	 *
	 * @param aToken
	 * @return The string unique token identifier
	 * @throws Exception
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
	 * Takes a specific IP address and a range using the IP/Netmask (e.g.
	 * 192.168.1.0/24 or 202.24.0.0/14).
	 *
	 * @param aIpAddress the address to check
	 * @param aIpAddressRange the range of addresses that most contain the IP
	 * address
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

	/**
	 *
	 * @param aArray
	 * @return
	 */
	public static List<String> parseStringArrayToList(String[] aArray) {
		FastList<String> lList = new FastList<String>();
		for (String lItem : aArray) {
			lList.add(lItem);
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
	 * Check the security restrictions on a ISecureComponent. <p> If the check
	 * fail, an exception is Thrown
	 *
	 * @param aSecureObject
	 * @param aAuthentication
	 * @throws Exception
	 */
	public static void checkSecurityRestrictions(IServerSecureComponent aSecureObject,
			IEmbeddedAuthentication aAuthentication) throws Exception {
		//Leaving if the security checks are not required
		if (!aSecureObject.isSecurityEnabled()) {
			return;
		}

		//NOT '!' operator flag
		boolean lExclusion;

		//Temporal variables
		Iterator<String> lIterator;
		String lValue;
		boolean lAuthorityAuthorized = false,
				lIpAuthorized = false,
				lUserAuthorized = false,
				lUserMatch = false;

		// caching local
		boolean lIsClientAuthenticated = aAuthentication.isAuthenticated();

		//Processing ip addresses restrictions
		if (aSecureObject.getIpAddresses().size() > 0) {
			lIterator = aSecureObject.getIpAddresses().iterator();
			while (lIterator.hasNext()) {
				lValue = lIterator.next();

				if (!lValue.equals("all")) {
					lExclusion = (lValue.startsWith("!")) ? true : false;
					lValue = (lExclusion) ? lValue.substring(1) : lValue;

					if (Util.isIpAddressInRange(aAuthentication.getRemoteHost().getHostAddress(), lValue)) {
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
						+ aAuthentication.getRemoteHost().getHostAddress()
						+ "' is not authorized to execute the operation!");
			}
		}

		//Processing users
		if (aSecureObject.getUsers().size() > 0) {
			if (lIsClientAuthenticated) {
				lIterator = aSecureObject.getUsers().iterator();
				while (lIterator.hasNext()) {
					lValue = lIterator.next();	//Required USER

					if (!lValue.equals("all")) {
						lExclusion = (lValue.startsWith("!")) ? true : false;
						lValue = (lExclusion) ? lValue.substring(1) : lValue;

						if (lValue.equals(aAuthentication.getUsername())) {
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
			if (lIsClientAuthenticated) {
				lIterator = aSecureObject.getRoles().iterator();
				while (lIterator.hasNext()) {
					lValue = lIterator.next();	//Required ROLE

					if (!lValue.equals("all")) {
						lExclusion = (lValue.startsWith("!")) ? true : false;
						lValue = (lExclusion) ? lValue.substring(1) : lValue;

						if (aAuthentication.hasAuthority(lValue)) {
							if (!lExclusion) {
								lAuthorityAuthorized = true; //Authorized!
							}
							break;
						}
					} else {
						lAuthorityAuthorized = true;
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

	/**
	 * Check the security restrictions on a ISecureComponent. <p> If the check
	 * fail, an exception is Thrown
	 *
	 * @param aSecureObject
	 * @param aConnector
	 * @param aIsAuthenticated
	 * @param aUsername
	 * @param aAuthorities
	 * @throws Exception
	 */
	public static void checkSecurityRestrictions(IServerSecureComponent aSecureObject,
			WebSocketConnector aConnector, boolean aIsAuthenticated, String aUsername, List<String> aAuthorities) throws Exception {
		//Leaving if the security checks are not required
		if (!aSecureObject.isSecurityEnabled()) {
			return;
		}

		//NOT '!' operator flag
		boolean lExclusion;

		//Temporal variables
		Iterator<String> lIterator;
		String lValue;
		boolean lAuthorityAuthorized = false,
				lIpAuthorized = false,
				lUserAuthorized = false,
				lUserMatch = false,
				lStop = false;

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
