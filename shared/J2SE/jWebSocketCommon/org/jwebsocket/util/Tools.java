//	---------------------------------------------------------------------------
//	jWebSocket Tools (Community Edition, CE)
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.HttpCookie;
import java.net.URI;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.util.Assert;

/**
 * Provides some convenience methods to support the web socket development.
 *
 * @author Alexander Schulze
 */
public class Tools {

	private static final Map<String, String> JAVA_2_GENERIC_MAP = new FastMap<String, String>();
	private static final Map<String, String> GENERIC_2_JAVA_MAP = new FastMap<String, String>();
	private static Timer mTimer = null;
	private static ExecutorService mThreadPool = null;
	/**
	 *
	 */
	public static final boolean ENC_BASE64 = true;
	/**
	 *
	 */
	public static final boolean ENC_PLAIN = false;

	static {
		JAVA_2_GENERIC_MAP.put("java.lang.String", "string");
		JAVA_2_GENERIC_MAP.put("java.lang.Boolean", "boolean");
		JAVA_2_GENERIC_MAP.put("java.lang.Byte", "integer");
		JAVA_2_GENERIC_MAP.put("java.lang.Short", "integer");
		JAVA_2_GENERIC_MAP.put("java.lang.Integer", "integer");
		JAVA_2_GENERIC_MAP.put("java.lang.Long", "long");
		JAVA_2_GENERIC_MAP.put("java.lang.Float", "float");
		JAVA_2_GENERIC_MAP.put("java.lang.Double", "double");
		JAVA_2_GENERIC_MAP.put("java.math.BigDecimal", "double");

		JAVA_2_GENERIC_MAP.put("java.sql.Timestamp", "datetime");
		JAVA_2_GENERIC_MAP.put("java.sql.Date", "date");
		JAVA_2_GENERIC_MAP.put("java.sql.Time", "time");
		JAVA_2_GENERIC_MAP.put("java.util.Date", "datetime");

		JAVA_2_GENERIC_MAP.put("java.util.Collection", "list");
		JAVA_2_GENERIC_MAP.put("java.util.List", "list");
		JAVA_2_GENERIC_MAP.put("java.util.Set", "list");
		JAVA_2_GENERIC_MAP.put("java.util.Map", "map");

		// these are just the conversion/casting defaults 
		// which optionally can be overwritten 
		GENERIC_2_JAVA_MAP.put("string", "java.lang.String");
		GENERIC_2_JAVA_MAP.put("boolean", "java.lang.Boolean");
		GENERIC_2_JAVA_MAP.put("integer", "java.lang.Integer");
		GENERIC_2_JAVA_MAP.put("long", "java.lang.Long");
		GENERIC_2_JAVA_MAP.put("float", "java.lang.Float");
		GENERIC_2_JAVA_MAP.put("double", "java.lang.Double");
		GENERIC_2_JAVA_MAP.put("list", "java.util.List");
		GENERIC_2_JAVA_MAP.put("map", "java.util.Map");
		GENERIC_2_JAVA_MAP.put("time", "java.util.Date");
		GENERIC_2_JAVA_MAP.put("date", "java.util.Date");
		GENERIC_2_JAVA_MAP.put("datetime", "java.util.Date");
	}
	/**
	 *
	 */
	public final static boolean EXPAND_CASE_SENSITIVE = false;
	/**
	 *
	 */
	public final static boolean EXPAND_CASE_INSENSITIVE = true;

	/**
	 * Returns the MD5 sum of the given string. The output always has 32 digits.
	 *
	 * @param aMsg String the string to calculate the MD5 sum for.
	 * @return MD5 sum of the given string.
	 */
	public static String getMD5(String aMsg) {
		return getMD5(aMsg.getBytes());
	}

	/**
	 * Gets the global CPU usage percent.
	 *
	 * @return
	 * @throws java.lang.Exception
	 */
	public static double getCpuUsage() throws Exception {
		Double lJavaVersion = Double.parseDouble(System.getProperty("java.vm.specification.version"));
		if (lJavaVersion >= 1.6) {
			OperatingSystemMXBean lOSBean = ManagementFactory.getPlatformMXBean(
					OperatingSystemMXBean.class);

			return lOSBean.getSystemLoadAverage() * 10;
		} else {
			Sigar mSigar = new Sigar();
			CpuPerc lCPU = mSigar.getCpuPerc();
			double lIdle = lCPU.getIdle();

			return 100 - (lIdle * 100);
		}
	}

	/**
	 * Returns the MD5 sum of the given byte array. The output always has 32 digits.
	 *
	 * @param aByteArray Byte array to calculate the MD5 sum for.
	 * @return MD5 sum of the given string.
	 */
	public static String getMD5(byte[] aByteArray) {
		try {
			return HashUtils.strToMD5(aByteArray);
		} catch (Exception lEx) {
			// log.error("getMD5: " + ex.getMessage());
			System.out.println("getMD5: " + lEx.getMessage());
		}
		return null;
	}

	/**
	 * Returns the SHA1 sum of the given string. The output always has 32 digits.
	 *
	 * @param aMsg String the string to calculate the MD5 sum for.
	 * @return MD5 sum of the given string.
	 */
	public static String getSHA1(String aMsg) {
		try {
			return HashUtils.strToSHA1(aMsg.getBytes());
		} catch (Exception lEx) {
			// log.error("getMD5: " + ex.getMessage());
			System.out.println("getSHA1: " + lEx.getMessage());
		}
		return null;
	}

	/**
	 * Returns the hex value of the given int as a string. If {@code aLen} is greater than zero the
	 * output is cut or filled to the given length otherwise the exact number of digits is returned.
	 *
	 * @param aInt Integer to be converted into a hex-string.
	 * @param aLen Number of hex digits (optionally filled or cut if needed)
	 * @return Hex-string of the given integer.
	 */
	public static String intToHex(int aInt, int aLen) {
		String lRes = Integer.toHexString(aInt);
		if (aLen > 0 && lRes.length() > aLen) {
			lRes = lRes.substring(0, aLen);
		} else {
			while (lRes.length() < aLen) {
				lRes = "0" + lRes.substring(0, aLen);
			}
		}
		return lRes;
	}

	/**
	 * Returns the hex value of the given int as a string. If {@code aLen} is greater than zero the
	 * output is cut or filled to the given length otherwise the exact number of digits is returned.
	 *
	 * @param aInt Integer to be converted into a string.
	 * @param aLen Number of digits (optionally filled or cut if needed)
	 * @return String of the given integer.
	 */
	public static String intToString(int aInt, int aLen) {
		String lRes = Integer.toString(aInt);
		if (aLen > 0 && lRes.length() > aLen) {
			lRes = lRes.substring(0, aLen);
		} else {
			while (lRes.length() < aLen) {
				lRes = "0" + lRes;
			}
		}
		return lRes;
	}

	/**
	 * Converts a string into an integer value and automatically sets it to a given default value if
	 * the string could not be parsed.
	 *
	 * @param aString string to be converted into an integer.
	 * @param aDefault default value assigned to the result in case of an exception.
	 * @return integer value of string or given default value in case of exception.
	 */
	public static int stringToInt(String aString, int aDefault) {
		int lRes;
		try {
			lRes = Integer.parseInt(aString);
		} catch (NumberFormatException lEx) {
			lRes = aDefault;
		}
		return lRes;
	}

	/**
	 * Converts a string into a long value and automatically sets it to a given default value if the
	 * string could not be parsed.
	 *
	 * @param aString string to be converted into a long.
	 * @param aDefault default value assigned to the result in case of an exception.
	 * @return long value of string or given default value in case of exception.
	 */
	public static long stringToLong(String aString, long aDefault) {
		long lRes;
		try {
			lRes = Long.parseLong(aString);
		} catch (NumberFormatException lEx) {
			lRes = aDefault;
		}
		return lRes;
	}

	/**
	 *
	 * @param aISO8601Date
	 * @return
	 */
	public static Date ISO8601ToDate(String aISO8601Date) {
		SimpleDateFormat lSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		try {
			// TimeZone lTimeZone = TimeZone.getTimeZone("GMT");
			// lSDF.setTimeZone(lTimeZone);
			return lSDF.parse(aISO8601Date);
		} catch (ParseException lEx) {
			return null;
		}
	}

	/**
	 *
	 * @param aDate
	 * @return
	 */
	public static String DateToISO8601(Date aDate) {
		// we are using UTC times only here, ignoring the timezone of the server location
		// so don't add a Z to the format string here! 'Z' means character Z = UTC
		SimpleDateFormat lSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		return lSDF.format(aDate);
	}

	/**
	 *
	 * @param aDate
	 * @return
	 */
	public static String DateToISO8601WithMillis(Date aDate) {
		// we are using UTC times only here, ignoring the timezone of the server location
		// so don't add a Z to the format string here! 'Z' means character Z = UTC
		SimpleDateFormat lSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		return lSDF.format(aDate);
	}

	/**
	 * Tries to convert a generic type to the correspondant java type
	 *
	 * @param aGenericType
	 * @return
	 */
	public static String getJavaClassnameFromGenericType(String aGenericType) {
		return GENERIC_2_JAVA_MAP.get(aGenericType);
	}

	/**
	 * Return TRUE if the file is located inside of the given base path, FALSE otherwise
	 *
	 * @param aFile
	 * @param aBasePath
	 * @return
	 */
	public static boolean isParentPath(File aFile, String aBasePath) {
		try {
			String lCanonicalPath = FilenameUtils
					.separatorsToSystem(aFile.getCanonicalPath()) + File.separator;
			String lBasePath = FilenameUtils.separatorsToSystem(aBasePath);
			if (SystemUtils.IS_OS_WINDOWS) {
				if (lCanonicalPath.toLowerCase().startsWith(lBasePath.toLowerCase())) {
					return true;
				}
			} else {
				if (!lCanonicalPath.startsWith(lBasePath)) {
					return false;
				}
			}
		} catch (IOException lEx) {
			return false;
		}
		return true;
	}

	/**
	 * Tries to convert a given object into the given java data type
	 *
	 * @param aValue
	 * @param aFromType
	 * @param aToType
	 * @return
	 */
	public static Object castGenericToJava(Object aValue, String aFromType, String aToType) {
		if (aValue == null) {
			return null;
		}
		if (aFromType != null // && aToType != null
				// && aValue != null // can never be null!
				) {
			aFromType = aFromType.toLowerCase();
			if (aToType != null) {
				aToType = aToType.toLowerCase();
			}

			// convert from datetime (java.sql.Date)
			if ("datetime".equals(aFromType)) {
				if (aValue instanceof String) {
					Date lDate = ISO8601ToDate((String) aValue);
					if (lDate != null) {
						if ("timestamp".equals(aToType)) {
							return new Timestamp(lDate.getTime());
						} else {
							return lDate;
						}
					}
				}
			} else if ("string".equals(aFromType)) {
				if (aValue instanceof String) {
					return (String) aValue;
				}
			} else if ("integer".equals(aFromType)) {
				if (aValue instanceof Integer) {
					return (Integer) aValue;
				}
			} else if ("float".equals(aFromType)) {
				if (aValue instanceof Float) {
					return (Float) aValue;
				}
			} else if ("double".equals(aFromType)) {
				if (aValue instanceof Double) {
					return (Double) aValue;
				}
			} else if ("boolean".equals(aFromType)) {
				if (aValue instanceof Boolean) {
					return (Boolean) aValue;
				}
			}
		}
		return null;
	}

	/**
	 * Tries to convert a given object into the given java data type
	 *
	 * @param aClassname
	 * @return
	 */
	public static String getGenericTypeStringFromJavaClassname(String aClassname) {
		return JAVA_2_GENERIC_MAP.get(aClassname);
	}

	/**
	 *
	 * @param aString
	 * @param aVars
	 * @param aIgnoreCase
	 * @return
	 */
	public static String expandVars(String aString, Map<String, String> aVars,
			boolean aIgnoreCase) {
		String lPattern = "\\$\\{([A-Za-z0-9_]+)\\}";
		int lFlags = aIgnoreCase ? Pattern.CASE_INSENSITIVE : 0;
		Pattern lRegExpr = Pattern.compile(lPattern, lFlags);
		Matcher lMatcher = lRegExpr.matcher(aString);
		while (lMatcher.find()) {
			String lFoundVal = lMatcher.group(1);
			// if (aIgnoreCase) {
			// 	lFoundVal = lFoundVal.toUpperCase();
			// }
			String lEnvVal = aVars.get(lFoundVal);
			if (lEnvVal == null) {
				lEnvVal = "";
			} else {
				lEnvVal = lEnvVal.replace("\\", "\\\\");
			}
			Pattern lSubExpr = Pattern.compile(Pattern.quote(lMatcher.group(0)));
			aString = lSubExpr.matcher(aString).replaceAll(lEnvVal);
		}
		return aString;
	}

	/**
	 * Replaces all pattern ${name} in a string by the values of the corresponding environment
	 * variable.
	 *
	 * @param aString
	 * @return
	 */
	public static String expandEnvVars(String aString) {
		Map<String, String> lVarsMap = System.getenv();
		return expandVars(aString, lVarsMap, EXPAND_CASE_INSENSITIVE);
	}

	/**
	 * Replaces all pattern ${name} in a string by the values of the corresponding system property.
	 *
	 * @param aString
	 * @return
	 */
	public static String expandProps(String aString) {
		Map<String, String> lVarsMap = new FastMap<String, String>();
		Properties lProps = System.getProperties();
		for (Entry lEntry : lProps.entrySet()) {
			Object lKey = lEntry.getKey();
			Object lValue = lEntry.getValue();
			if (lKey instanceof String && lValue instanceof String) {
				lVarsMap.put((String) lKey, (String) lValue);
			}
		}
		return expandVars(aString, lVarsMap, EXPAND_CASE_INSENSITIVE);
	}

	/**
	 * Replaces all pattern ${name} in a string by the values of the corresponding environment
	 * variable or system property. The setting of a system property overrides the setting of the
	 * environment variable.
	 *
	 * @param aString
	 * @return
	 */
	public static String expandEnvVarsAndProps(String aString) {
		Map<String, String> lVarsMap = new FastMap<String, String>();
		Properties lProps = System.getProperties();
		for (Entry lEntry : lProps.entrySet()) {
			Object lKey = lEntry.getKey();
			Object lValue = lEntry.getValue();
			if (null != lKey && null != lValue
					&& lKey instanceof String
					&& lValue instanceof String) {
				lVarsMap.put((String) lKey, (String) lValue);
			}
		}
		lVarsMap.putAll(System.getenv());

		return expandVars(aString, lVarsMap, EXPAND_CASE_INSENSITIVE);
	}

	/**
	 * Compare 2 'Major.Minor.Fix' codified versions. Where Major, Minor, Fix values require to be
	 * non-negative integer values.
	 *
	 * @param aVersion1
	 * @param aVersion2
	 * @return Return 1 if 'version1', 0 if equals and -1 if lower.
	 */
	public static Integer compareVersions(String aVersion1, String aVersion2) {
		Assert.isTrue(null != aVersion1 && !aVersion1.isEmpty(), "The argument 'version1' cannot be null or empty!");
		Assert.isTrue(null != aVersion2 && !aVersion2.isEmpty(), "The argument 'version2' cannot be null or empty!");

		aVersion1 = aVersion1.split(" ")[0];
		aVersion2 = aVersion2.split(" ")[0];
		String[] lVNumbers1 = StringUtils.split(aVersion1, ".");
		String[] lVNumbers2 = StringUtils.split(aVersion2, ".");

		Assert.isTrue(lVNumbers1.length >= 3, "The argument 'version1' has"
				+ " invalid jWebSocket version codification! "
				+ "Expecting: 'Major.Minor.Fix'");
		Assert.isTrue(lVNumbers2.length >= 3, "The argument 'version2' has "
				+ "invalid jWebSocket version codification! "
				+ "Expecting: 'Major.Minor.Fix'");

		String lErrorMsg = "The argument 'version1' has invalid value. "
				+ "Expecting non-negative integer values!";
		Integer lMayor1 = Integer.parseInt(lVNumbers1[0]);
		Assert.isTrue(lMayor1 >= 0, lErrorMsg);
		Integer lMinor1 = Integer.parseInt(lVNumbers1[1]);
		Assert.isTrue(lMinor1 >= 0, lErrorMsg);
		Integer lFix1 = Integer.parseInt(lVNumbers1[2]);
		Assert.isTrue(lFix1 >= 0, lErrorMsg);

		lErrorMsg = "The argument 'version2' has invalid value. "
				+ "Expecting non-negative integer values!";
		Integer lMayor2 = Integer.parseInt(lVNumbers2[0]);
		Assert.isTrue(lMayor2 >= 0, lErrorMsg);
		Integer lMinor2 = Integer.parseInt(lVNumbers2[1]);
		Assert.isTrue(lMinor2 >= 0, lErrorMsg);
		Integer lFix2 = Integer.parseInt(lVNumbers2[2]);
		Assert.isTrue(lFix2 >= 0, lErrorMsg);

		int lCMayor = lMayor1.compareTo(lMayor2);
		int lCMinor = lMinor1.compareTo(lMinor2);
		int lCFix = lFix1.compareTo(lFix2);

		if (lCMayor != 0) {
			return lCMayor;
		}
		if (lCMinor != 0) {
			return lCMinor;
		}

		return lCFix;
	}

	/**
	 *
	 * @param aClassName
	 * @param aMethodName
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public static Object invoke(String aClassName, String aMethodName,
			Object... aArgs) throws Exception {
		Class lClass = Class.forName(aClassName);
		/*
		 * if (lClass == null) { throw new Exception("Class '" + aClassName + "'
		 * not found."); }
		 */
		Object lRes;

		Class[] lArgClasses = null;
		if (aArgs != null) {
			lArgClasses = new Class[aArgs.length];
			for (int lIdx = 0; lIdx < lArgClasses.length; lIdx++) {
				lArgClasses[lIdx] = aArgs[lIdx].getClass();
			}
		}
		Method lMthd = lClass.getMethod(aMethodName, lArgClasses);
		/*
		 * if (lMthd == null) { throw new Exception("Method '" + aMethodName +
		 * "' not found."); }
		 */
		lRes = lMthd.invoke(null, aArgs);

		return lRes;
	}

	/**
	 *
	 * @param aClass
	 * @param aMethodName
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public static Object invoke(Class aClass, String aMethodName,
			Object... aArgs) throws Exception {
		if (aClass == null) {
			throw new Exception("No class passed for call.");
		}
		Object lRes;

		Class[] lArgClasses = null;
		if (aArgs != null) {
			lArgClasses = new Class[aArgs.length];
			for (int lIdx = 0; lIdx < lArgClasses.length; lIdx++) {
				Class lClass = aArgs[lIdx].getClass();
				lArgClasses[lIdx] = lClass;
			}
		}
		Method lMthd = aClass.getMethod(aMethodName, lArgClasses);
		if (lMthd == null) {
			throw new Exception("Method '" + aMethodName + "' not found.");
		}
		lRes = lMthd.invoke(null, aArgs);

		return lRes;
	}

	/**
	 *
	 * @param aClass
	 * @param aMethodName
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public static Object invokeUnique(Class aClass, String aMethodName,
			Object... aArgs) throws Exception {
		if (aClass == null) {
			throw new Exception("No class passed for call.");
		}
		if (aMethodName == null) {
			throw new Exception("No method name passed for call.");
		}
		Object lRes;

		Class[] lArgClasses;
		if (aArgs != null) {
			lArgClasses = new Class[aArgs.length];
			for (int lIdx = 0; lIdx < lArgClasses.length; lIdx++) {
				Class lClass = aArgs[lIdx].getClass();
				lArgClasses[lIdx] = lClass;
			}
		}

		Method lMthd = null;
		Method[] lMethods = aClass.getMethods();
		for (Method lMethod : lMethods) {
			if (aMethodName.equals(lMethod.getName())) {
				lMthd = lMethod;
				break;
			}
		}
		if (lMthd == null) {
			throw new Exception("Method '" + aMethodName + "' not found.");
		}
		lRes = lMthd.invoke(null, aArgs);

		return lRes;
	}

	/**
	 *
	 * @param aClass
	 * @param aMethodName
	 * @param aArgs
	 * @param aClasses
	 * @return
	 * @throws Exception
	 */
	public static Object invokeUnique(Class aClass, String aMethodName,
			Object[] aArgs, Class[] aClasses) throws Exception {
		if (aClass == null) {
			throw new Exception("No class passed for call.");
		}
		if (aArgs != null && aClasses != null && aArgs.length != aClasses.length) {
			throw new Exception("Number of aclasses must match the number of arguments.");
		}
		if (aMethodName == null) {
			throw new Exception("No method name passed for call.");
		}
		Object lRes;

		Class[] lArgClasses;
		if (aArgs != null) {
			lArgClasses = new Class[aArgs.length];
			for (int lIdx = 0; lIdx < lArgClasses.length; lIdx++) {
				Class lClass = aClasses[lIdx].getClass();
				lArgClasses[lIdx] = lClass;
			}
		}

		Method lMthd = null;
		Method[] lMethods = aClass.getMethods();
		for (Method lMethod : lMethods) {
			if (aMethodName.equals(lMethod.getName())) {
				lMthd = lMethod;
				break;
			}
		}
		if (lMthd == null) {
			throw new Exception("Method '" + aMethodName + "' not found.");
		}
		lRes = lMthd.invoke(null, aArgs);

		return lRes;
	}

	/**
	 *
	 * @param aInstance
	 * @param aMethodName
	 * @param aClasses
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public static Object invoke(Object aInstance, String aMethodName, Class[] aClasses,
			Object... aArgs) throws Exception {
		if (aInstance == null) {
			throw new Exception("No instance passed for call.");
		}
		Class lClass = aInstance.getClass();
		Object lRes;

		Method lMthd = lClass.getMethod(aMethodName, aClasses);
		if (aArgs == null) {
			aArgs = new Object[0];
		}
		lRes = lMthd.invoke(aInstance, aArgs);

		return lRes;
	}

	/**
	 *
	 * @param aInstance
	 * @param aMethodName
	 * @param aArgs
	 * @return
	 * @throws Exception
	 */
	public static Object invoke(Object aInstance, String aMethodName,
			Object... aArgs) throws Exception {
		Class[] lArgClasses = null;
		if (aArgs != null) {
			lArgClasses = new Class[aArgs.length];
			for (int lIdx = 0; lIdx < lArgClasses.length; lIdx++) {
				lArgClasses[lIdx] = aArgs[lIdx].getClass();
			}
		}

		return invoke(aInstance, aMethodName, lArgClasses, aArgs);
	}
	private static final char[] BASE64_CHAR_MAP = new char[64];

	static {
		int lIdx = 0;
		for (char lC = 'A'; lC <= 'Z'; lC++) {
			BASE64_CHAR_MAP[lIdx++] = lC;
		}
		for (char lC = 'a'; lC <= 'z'; lC++) {
			BASE64_CHAR_MAP[lIdx++] = lC;
		}
		for (char lC = '0'; lC <= '9'; lC++) {
			BASE64_CHAR_MAP[lIdx++] = lC;
		}
		BASE64_CHAR_MAP[lIdx++] = '+';
		BASE64_CHAR_MAP[lIdx++] = '/';
	}

	/**
	 *
	 * @param aBA
	 * @return
	 */
	public static String base64Encode(byte[] aBA) {
		int lLen = aBA.length;
		int oDataLen = (lLen * 4 + 2) / 3;// output length without padding
		int oLen = ((lLen + 2) / 3) * 4;// output length including padding
		char[] out = new char[oLen];
		int ip = 0;
		int op = 0;
		int i0, i1, i2;
		int o0, o1, o2, o3;
		while (ip < lLen) {
			i0 = aBA[ip++] & 0xff;
			i1 = ip < lLen ? aBA[ip++] & 0xff : 0;
			i2 = ip < lLen ? aBA[ip++] & 0xff : 0;
			o0 = i0 >>> 2;
			o1 = ((i0 & 3) << 4) | (i1 >>> 4);
			o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			o3 = i2 & 0x3F;
			out[op++] = BASE64_CHAR_MAP[o0];
			out[op++] = BASE64_CHAR_MAP[o1];
			out[op] = op < oDataLen ? BASE64_CHAR_MAP[o2] : '=';
			op++;
			out[op] = op < oDataLen ? BASE64_CHAR_MAP[o3] : '=';
			op++;
		}
		return new String(out);
	}

	/**
	 * Guess whether given file is binary. Just checks for anything under 0x09.
	 *
	 * @param aFile
	 * @return boolean if the file is binary or not
	 * @throws java.io.FileNotFoundException
	 */
	public static boolean isBinaryFile(File aFile) throws FileNotFoundException, IOException {
		FileInputStream in = new FileInputStream(aFile);
		int size = in.available();
		if (size > 1024) {
			size = 1024;
		}
		byte[] data = new byte[size];
		in.read(data);
		in.close();

		int lAsci = 0;
		int lOther = 0;

		for (int i = 0; i < data.length; i++) {
			byte b = data[i];
			if (b < 0x09) {
				return true;
			}

			if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D) {
				lAsci++;
			} else if (b >= 0x20 && b <= 0x7E) {
				lAsci++;
			} else {
				lOther++;
			}
		}

		if (lOther == 0) {
			return false;
		}

		return (lAsci + lOther) * 100 / lOther > 95;
	}

	/**
	 *
	 * @param aBase64String
	 * @return
	 */
	public static byte[] base64Decode(String aBase64String) {
		return Base64.decodeBase64(aBase64String);
	}

	/**
	 *
	 * @param aArray
	 * @return
	 */
	public static List<String> parseStringArrayToList(String[] aArray) {
		FastList<String> lRes = new FastList<String>();
		int lEnd = aArray.length;
		for (int lIdx = 0; lIdx < lEnd; lIdx++) {
			lRes.add(aArray[lIdx]);
		}
		return lRes;
	}

	/**
	 *
	 */
	public static void startUtilityThreadPool() {
		if (System.getProperties().contains("JWEBSOCKET_HOME")) {
			AccessController.checkPermission(stringToPermission("permission java.util.PropertyPermission \""
					+ "org.jwebsocket.tools.threadpool\", \"write\""));
		}

		if (null == mThreadPool) {
			mThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "jWebSocket Utility ThreadPool");
				}
			});
		}
	}

	/**
	 *
	 */
	public static void stopUtilityThreadPool() {
		if (System.getProperties().contains("JWEBSOCKET_HOME")) {
			AccessController.checkPermission(stringToPermission("permission java.util.PropertyPermission \""
					+ "org.jwebsocket.tools.threadpool\", \"write\""));
		}

		if (null != mThreadPool && !mThreadPool.isShutdown()) {
			mThreadPool.shutdownNow();
		}
		mThreadPool = null;
	}

	/**
	 *
	 * @return
	 */
	public static ExecutorService getThreadPool() {
		if (System.getProperties().contains("JWEBSOCKET_HOME")) {
			AccessController.checkPermission(stringToPermission("permission java.util.PropertyPermission \""
					+ "org.jwebsocket.tools.threadpool\", \"write\""));
		}

		if (null == mThreadPool) {
			startUtilityThreadPool();
		}

		return mThreadPool;
	}

	/**
	 * Starts the jWebSocket utility timer. The timer automatically purge expired tasks every 5
	 * minute.
	 */
	public static void startUtilityTimer() {
		if (System.getProperties().contains("JWEBSOCKET_HOME")) {
			AccessController.checkPermission(stringToPermission("permission java.util.PropertyPermission \""
					+ "org.jwebsocket.tools.timer\", \"write\""));
		}

		if (null == mTimer) {
			mTimer = new Timer("jWebSocket Utility Timer");
			final Timer lTimer = mTimer;
			mTimer.scheduleAtFixedRate(new JWSTimerTask() {
				@Override
				public void runTask() {
					lTimer.purge();
				}
			}, 0, 300000);
		}
	}

	/**
	 * Stops the jWebSocket utility time.
	 */
	public static void stopUtilityTimer() {
		if (System.getProperties().contains("JWEBSOCKET_HOME")) {
			AccessController.checkPermission(stringToPermission("permission java.util.PropertyPermission \""
					+ "org.jwebsocket.tools.timer\", \"write\""));
		}

		if (null != mTimer) {
			mTimer.cancel();
			mTimer.purge();
		}

		mTimer = null;
	}

	/**
	 *
	 * @return A jWebSocket shared utility timer
	 */
	public static Timer getTimer() {
		if (System.getProperties().contains("JWEBSOCKET_HOME")) {
			AccessController.checkPermission(stringToPermission("permission java.util.PropertyPermission \""
					+ "org.jwebsocket.tools.timer\", \"write\""));
		}

		if (null == mTimer) {
			startUtilityTimer();
		}
		return mTimer;
	}

	/**
	 * From
	 * http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
	 *
	 *
	 * @param aString
	 * @return
	 */
	public static byte[] hexStringToByteArray(String aString) {
		int lLength = aString.length();
		byte[] lData = new byte[lLength / 2];
		for (int lIndex = 0; lIndex < lLength; lIndex += 2) {
			lData[lIndex / 2] = (byte) ((Character.digit(aString.charAt(lIndex), 16) << 4)
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
		final StringBuilder lHexBuilder = new StringBuilder(2 * aByteArray.length);
		for (final byte lByte : aByteArray) {
			lHexBuilder.append(HEXES.charAt((lByte & 0xF0) >> 4)).append(HEXES.charAt((lByte & 0x0F)));
		}
		return lHexBuilder.toString();
	}

	/**
	 * Indicates if a cookie is valid for a given URI
	 *
	 * @param aURI
	 * @param aCookie
	 * @return TRUE if the cookie is valid, FALSE otherwise
	 */
	public static boolean isCookieValid(URI aURI, HttpCookie aCookie) {
		return !aCookie.hasExpired()
				&& (null == aCookie.getDomain() || HttpCookie.domainMatches(aCookie.getDomain(), aURI.getHost()))
				&& (null == aCookie.getPath() || (null != aURI.getPath() && aURI.getPath().startsWith(aCookie.getPath())))
				&& (aCookie.getSecure() == (aURI.getScheme().equals("wss")));
	}

	/**
	 * Checks if a path has trailing separator, if not it appends the correct one according to the
	 * operating system.
	 *
	 * @param aPath the path to be checked for the trailing separator.
	 * @return the path ensuring the trailing separator or null if no path was given.
	 */
	public static String appendTrailingSeparator(String aPath) {
		if (null != aPath) {
			if (!aPath.endsWith("\\") && !aPath.endsWith("/")) {
				aPath += System.getProperty("file.separator");
			}
		}
		return aPath;
	}

	/**
	 * Deflate a byte array with Zip compression
	 *
	 * @param aUncompressedData The uncompressed data
	 * @return The compressed data
	 * @throws Exception
	 */
	public static byte[] deflate(byte[] aUncompressedData) throws Exception {
		Deflater lDeflater = new Deflater();
		lDeflater.setInput(aUncompressedData);
		lDeflater.finish();
		byte[] lOut = new byte[1024 * 1000 * 5];
		int lWritten = lDeflater.deflate(lOut);
		byte[] lResult = new byte[lWritten];

		System.arraycopy(lOut, 0, lResult, 0, lWritten);

		return lResult;
	}

	/**
	 * Inflate a byte array with Zip compression
	 *
	 * @param aCompressedData
	 * @return
	 * @throws Exception
	 */
	public static byte[] inflate(byte[] aCompressedData) throws Exception {
		Inflater lInflater = new Inflater();
		lInflater.setInput(aCompressedData);
		byte[] lOut = new byte[1024 * 1000 * 5];
		int lWritten = lInflater.inflate(lOut);
		byte[] lResult = new byte[lWritten];

		System.arraycopy(lOut, 0, lResult, 0, lWritten);

		return lResult;
	}

	/**
	 * Compress a byte array using zip compression
	 *
	 * @param aBA
	 * @param aBase64Encode if TRUE, the result is Base64 encoded
	 * @return
	 * @throws Exception
	 */
	public static byte[] zip(byte[] aBA, Boolean aBase64Encode) throws Exception {
		ByteArrayOutputStream lBAOS = new ByteArrayOutputStream();
		ArchiveOutputStream lAOS = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, lBAOS);
		ZipArchiveEntry lZipEntry = new ZipArchiveEntry("temp.zip");
		lZipEntry.setSize(aBA.length);
		lAOS.putArchiveEntry(lZipEntry);
		lAOS.write(aBA);
		lAOS.closeArchiveEntry();
		lAOS.flush();
		lAOS.close();

		if (aBase64Encode) {
			aBA = Base64.encodeBase64(lBAOS.toByteArray());
		} else {
			aBA = lBAOS.toByteArray();
		}

		return aBA;
	}

	/**
	 * Uncompress a byte array using zip compression
	 *
	 * @param aBA
	 * @param aBase64Decode If TRUE, the byte array is Base64 decoded before uncompress
	 * @return
	 * @throws Exception
	 */
	public static byte[] unzip(byte[] aBA, Boolean aBase64Decode) throws Exception {
		if (aBase64Decode) {
			aBA = Base64.decodeBase64(aBA);
		}
		ByteArrayInputStream lBAIS = new ByteArrayInputStream(aBA);
		ZipArchiveInputStream lAIOS = new ZipArchiveInputStream(lBAIS);
		// ATTENTION: do not comment next line!!!
		lAIOS.getNextZipEntry();

		ByteArrayOutputStream lBAOS = new ByteArrayOutputStream();
		IOUtils.copy(lAIOS, lBAOS);
		lAIOS.close();

		return lBAOS.toByteArray();
	}

	/**
	 * Zip files
	 *
	 * @param aFilesPath The list of files to Zip
	 * @param aOutputZipFile
	 * @param lFolder
	 * @throws IOException
	 */
	private static void zip(String[] aFilesPath, ZipOutputStream aOutputZipFile, String lFolder) throws IOException {
		byte[] buffer = new byte[4096];
		int bytesRead;
		// output file
		for (String lFilePath : aFilesPath) {
			File lFile = new File(lFilePath);

			if (!lFile.isDirectory()) {
				FileInputStream in = new FileInputStream(lFile);

				ZipEntry entry = new ZipEntry((lFolder.isEmpty()) ? lFile.getName() : lFolder + File.separator + lFile.getName());
				aOutputZipFile.putNextEntry(entry);

				while ((bytesRead = in.read(buffer)) != -1) {
					aOutputZipFile.write(buffer, 0, bytesRead);
				}
				in.close();

			} else {
				String[] lSubFiles = lFile.list();
				for (int lIndex = 0; lIndex < lSubFiles.length; lIndex++) {
					lSubFiles[lIndex] = lFile.getPath() + File.separator + lSubFiles[lIndex];
				}
				zip(lSubFiles, aOutputZipFile, (lFolder.isEmpty()) ? lFile.getName() : lFolder + File.separator + lFile.getName());
			}
		}
	}

	/**
	 * Zip files
	 *
	 * @param aFilesPath The list of files to Zip
	 * @param aOutputZipFile
	 * @throws IOException
	 */
	public static void zip(String[] aFilesPath, String aOutputZipFile) throws IOException {
		ZipOutputStream lOut = new ZipOutputStream(new FileOutputStream(aOutputZipFile));
		zip(aFilesPath, lOut, "");

		lOut.close();
	}

	/**
	 * Unzip a ZIP file into an output directory
	 *
	 * @see http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
	 *
	 * @param aZipFile input zip file
	 * @param aOutputDirectory
	 * @throws IOException
	 */
	public static void unzip(File aZipFile, File aOutputDirectory) throws IOException {

		byte[] lBuffer = new byte[1024];

		//create output directory is not exists
		if (!aOutputDirectory.exists()) {
			aOutputDirectory.mkdir();
		}

		//get the zip file content
		ZipInputStream lZIS = new ZipInputStream(new FileInputStream(aZipFile));
		//get the zipped file list entry
		ZipEntry lZE = lZIS.getNextEntry();

		while (lZE != null) {
			String lFileName = lZE.getName();
			File lNewFile = new File(aOutputDirectory.getCanonicalPath() + File.separator + lFileName);

			if (lZE.isDirectory()) {
				lNewFile.mkdir();
			} else {
				FileOutputStream lFOS = new FileOutputStream(lNewFile);
				int lLength;
				while ((lLength = lZIS.read(lBuffer)) > 0) {
					lFOS.write(lBuffer, 0, lLength);
				}

				lFOS.close();
			}
			lZE = lZIS.getNextEntry();
		}

		lZIS.closeEntry();
		lZIS.close();
	}

	/**
	 * Parse a string valid representation of a Java security permission. Example:
	 * <code>permission java.util.PropertyPermission "java.util.logging.config.class", "read"</code>
	 *
	 * @param aPermission
	 * @return
	 */
	public static Permission stringToPermission(String aPermission) {
		try {
			String[] lTempArray = aPermission.replace("\"", "").split(" ", 3);
			String lClassName = lTempArray[1];
			String lName = null, lActions = null;

			if (lTempArray.length > 2) {
				lTempArray = lTempArray[2].split(",", 2);

				lName = lTempArray[0];
				if (lTempArray.length > 1) {
					lActions = lTempArray[1];
				}
			}

			Class lClazz = Class.forName(lClassName);
			Constructor<Permission> lConstructor = lClazz.getConstructor(String.class, String.class);
			if (null != lConstructor) {
				return lConstructor.newInstance(new Object[]{lName, lActions});
			} else {
				return (Permission) lClazz.newInstance();
			}
		} catch (Exception lEx) {
		}

		return null;
	}

	/**
	 * Executes a privileged action in sandbox.
	 *
	 * @param aPermissions The security permissions.
	 * @param aAction The action to execute/
	 * @return
	 */
	public static Object doPrivileged(PermissionCollection aPermissions, PrivilegedAction aAction) {
		ProtectionDomain lProtectionDomain = new ProtectionDomain(
				new CodeSource(null, (Certificate[]) null), aPermissions);
		AccessControlContext lSecureContext = new AccessControlContext(new ProtectionDomain[]{lProtectionDomain});

		return AccessController.doPrivileged(aAction, lSecureContext);
	}

	/**
	 * Performs a wildcard matching for a given string value.
	 *
	 * @param aText the string to be matched.
	 *
	 * @param aPattern the wildcard pattern. This can contain the wildcard character '*' (asterisk).
	 *
	 * @see http://www.adarshr.com/papers/wildcard
	 * @return <tt>true</tt> if a match is found, <tt>false</tt>
	 * otherwise.
	 */
	public static boolean wildCardMatch(String aText, String aPattern) {
		if ("*".equals(aPattern) || aText.equals(aPattern)) {
			return true;
		}

		// Create the cards by splitting using a RegEx. If more speed 
		// is desired, a simpler character based splitting can be done.
		String[] lCards = aPattern.split("\\*");

		// Iterate over the cards.
		for (String lCard : lCards) {
			int idx = aText.indexOf(lCard);

			// Card not detected in the text.
			if (idx == -1) {
				return false;
			}

			// Move ahead, towards the right of the text.
			aText = aText.substring(idx + lCard.length());
		}

		return true;
	}

	/**
	 * Performs a wildcard matching for a given string value. If the value matches one of the
	 * patterns, the method returns TRUE.
	 *
	 * @param aText the text to be tested for matches.
	 *
	 * @param aPatterns the wildcard patterns. This can contain the wildcard character '*'
	 * (asterisk).
	 * @return <tt>true</tt> if a match is found, <tt>false</tt>
	 * otherwise.
	 */
	public static boolean wildCardMatch(String aText, String[] aPatterns) {
		for (String lPattern : aPatterns) {
			if (wildCardMatch(aText, lPattern)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Creates a LoadBalancer plug-in compliant response token from incoming token.
	 *
	 * @param aInToken The incoming token
	 * @return
	 */
	public static Token createLoadBalancerResponse(Token aInToken) {
		Token aResponse = TokenFactory.createToken();

		Integer lTokenId = null;
		String lType = null;
		String lNS = null;
		String lSourceConnector = null;
		if (aInToken != null) {
			lTokenId = aInToken.getInteger("utid", -1);
			lType = aInToken.getString("type");
			lNS = "org.jwebsocket.plugins.loadbalancer";
			lSourceConnector = aInToken.getString("sourceId");
		}
		aResponse.setType("response");

		// if code and msg are already part of outgoing token do not overwrite!
		aResponse.setInteger("code", aResponse.getInteger("code", 0));
		aResponse.setString("msg", aResponse.getString("msg", "ok"));

		if (lTokenId != null) {
			aResponse.setInteger("utid", lTokenId);
		}
		if (lNS != null) {
			aResponse.setString("ns", lNS);
		}
		if (lType != null) {
			aResponse.setString("reqType", lType);
		}
		if (lSourceConnector != null) {
			aResponse.setString("sourceId", lSourceConnector);
		}

		return aResponse;
	}

	/**
	 * Invokes a future task using a multithreading model.
	 *
	 * @param aRunnable
	 * @param aTimeout
	 */
	public static void invokeLater(final Runnable aRunnable, int aTimeout) {
		Tools.getTimer().schedule(new JWSTimerTask() {

			@Override
			protected void runTask() {
				Tools.getThreadPool().submit(aRunnable);
			}
		}, aTimeout);
	}
}
