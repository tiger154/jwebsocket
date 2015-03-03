//	---------------------------------------------------------------------------
//	jWebSocket - LocalLoader (Community Edition, CE)
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
package org.jwebsocket.factory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.jwebsocket.logging.Logging;
import org.xeustechnologies.jcl.ClasspathResources;

/**
 * ClassLoader that reloads locally the classes from the jars. Plugins and
 * Filters all configured via jWebSocket.xml file is loaded using this class.
 *
 * @author Marcos Antonio Gonzalez Huerta
 */
public class LocalLoader extends ClassLoader {

	private static Logger mLog = Logging.getLogger();
	/**
	 *
	 */
	protected final ClasspathResources mClasspathResources;
	private char mClassNameReplacementChar;
	private URLClassLoader mParent;
	private ArrayList<String> mLoadedJars = new ArrayList<String>();

	/**
	 *
	 * @param aParent
	 */
	public LocalLoader(URLClassLoader aParent) {
		mClasspathResources = new ClasspathResources();
		this.mParent = aParent;
	}

	/**
	 *
	 * @return
	 */
	public char getClassNameReplacementChar() {
		return mClassNameReplacementChar;
	}

	/**
	 *
	 * @param aClassNameReplacementChar
	 */
	public void setClassNameReplacementChar(char aClassNameReplacementChar) {
		this.mClassNameReplacementChar = aClassNameReplacementChar;
	}

	/**
	 * Loads a Jar
	 *
	 * @param aJarFile
	 * @return TRUE if the jar has beeen loaded, FALSE otherwise
	 */
	public boolean loadJar(String aJarFile) {
		if (mLoadedJars.contains(aJarFile)) {
			return false;
		}
		mClasspathResources.loadJar(aJarFile);
		mLoadedJars.add(aJarFile);
		return true;
	}

	@Override
	public Class loadClass(String aClassName, boolean aResolveIt) throws ClassNotFoundException {
		Class lResult = null;
		byte[] lClassBytes;

		lClassBytes = loadClassBytes(aClassName);
		if (lClassBytes == null) {
			return mParent.loadClass(aClassName);
		}

		lResult = defineClass(aClassName, lClassBytes, 0, lClassBytes.length);

		if (lResult == null) {
			return null;
		}

		/*
		 * Preserve package name.
		 */
		if (lResult.getPackage() == null) {
			String lPackageName = aClassName.substring(0, aClassName.lastIndexOf('.'));
			definePackage(lPackageName, null, null, null, null, null, null, null);
		}

		if (aResolveIt) {
			resolveClass(lResult);
		}

		if (mLog.isTraceEnabled()) {
			mLog.trace("Return new local loaded class " + aClassName);
		}
		return lResult;
	}

	/**
	 *
	 * @param aName
	 * @return
	 */
	public InputStream loadResource(String aName) {
		byte[] lArr = mClasspathResources.getResource(aName);
		if (lArr != null) {
			if (mLog.isTraceEnabled()) {
				mLog.trace("Returning newly loaded resource " + aName);
			}

			return new ByteArrayInputStream(lArr);
		}

		return null;
	}

	/**
	 * Reads the class bytes from different local and remote resources using
	 * ClasspathResources
	 *
	 * @param aClassName
	 * @return byte[]
	 */
	protected byte[] loadClassBytes(String aClassName) {
		aClassName = formatClassName(aClassName);

		return mClasspathResources.getResource(aClassName);
	}

	/**
	 * @param aClassName
	 * @return String
	 */
	protected String formatClassName(String aClassName) {
		aClassName = aClassName.replace('/', '~');

		if (mClassNameReplacementChar == '\u0000') {
			// '/' is used to map the package to the path
			aClassName = aClassName.replace('.', '/') + ".class";
		} else {
			// Replace '.' with custom char, such as '_'
			aClassName = aClassName.replace('.', mClassNameReplacementChar) + ".class";
		}

		aClassName = aClassName.replace('~', '/');
		return aClassName;
	}
}
