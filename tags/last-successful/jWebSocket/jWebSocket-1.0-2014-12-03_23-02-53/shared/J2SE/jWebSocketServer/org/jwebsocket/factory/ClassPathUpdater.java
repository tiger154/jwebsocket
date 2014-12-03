//	---------------------------------------------------------------------------
//	jWebSocket - ClassPathUpdater (Community Edition, CE)
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Allows programs to modify the classpath during runtime.
 */
public class ClassPathUpdater {

	/**
	 * Used to find the method signature.
	 */
	private static final Class[] PARAMETERS = new Class[]{URL.class};
	/**
	 * Class containing the private addURL method.
	 */
	private static final Class<?> CLASS_LOADER = URLClassLoader.class;

	/**
	 * Adds a new path to the classloader. If the given string points to a file,
	 * then that file's parent file (i.e. directory) is used as the directory to
	 * add to the classpath. If the given string represents a directory, then
	 * the directory is directly added to the classpath.
	 *
	 * @param aDir The directory to add to the classpath (or a file, which will
	 * relegate to its directory).
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void add(String aDir)
			throws IOException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		add(new File(aDir));
	}

	/**
	 * Adds a new path to the classloader. If the given file object is a file,
	 * then its parent file (i.e., directory) is used as the directory to add to
	 * the classpath. If the given string represents a directory, then the
	 * directory it represents is added.
	 *
	 * @param aFile The directory (or enclosing directory if a file) to add to
	 * the classpath.
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void add(File aFile)
			throws IOException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		aFile = aFile.isDirectory() ? aFile : aFile.getParentFile();
		add(aFile.toURI().toURL());
	}

	/**
	 *
	 * @param aFile
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void addJar(File aFile)
			throws IOException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		add(aFile.toURI().toURL());
	}

	/**
	 * Adds a new path to the classloader. The class must point to a directory,
	 * not a file.
	 *
	 * @param aURL The path to include when searching the classpath.
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 */
	public static void add(URL aURL)
			throws IOException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Method lMethod = CLASS_LOADER.getDeclaredMethod("addURL", PARAMETERS);
		lMethod.setAccessible(true);
		lMethod.invoke(getClassLoader(), new Object[]{aURL});
	}

	/**
	 *
	 * @param aURL
	 * @param aClassLoader
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void add(URL aURL, ClassLoader aClassLoader)
			throws IOException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Method lMethod = aClassLoader.getClass().getDeclaredMethod("addURL", PARAMETERS);
		lMethod.setAccessible(true);
		lMethod.invoke(aClassLoader, new Object[]{aURL});
	}

	private static URLClassLoader getClassLoader() {
		return (URLClassLoader) ClassLoader.getSystemClassLoader();
	}
	/*	
	 static{
	 try{
	 Method lMethod = CLASS_LOADER.getDeclaredMethod("addURL", PARAMETERS);
	 lMethod.setAccessible(true);
	 lMethod.invoke((URLClassLoader) ClassLoader.getSystemClassLoader(), new Object[]{new URL("file:C:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs")});
	 lMethod.invoke((URLClassLoader) Thread.currentThread().getContextClassLoader(), new Object[]{new URL("file:C:/svn/jWebSocketDev/rte/jWebSocket-1.0/libs")});
	 } catch(Exception lEx) {
	 String lMsg = lEx.getMessage();
	 System.out.println(lMsg);
	 }
	 }
	 */
}