/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.internal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * ClassFinder provides convenience methods for finding classes in a given jar
 * path, a package name, and/or classs loader.
 * 
 * @author dpark
 *
 */
public class ClassFinder {

	/**
	 * Returns all classes that are in the specified package. Use this method to
	 * find inflated classes, i.e., classes that are kept in a directory. Use
	 * getClasses(String jarPath, String packageName) if the classes are in a jar
	 * file.
	 * 
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException Thrown if unable to load a class
	 * @throws IOException            Thrown if error occurs while reading the jar
	 *                                file
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Class[] getClasses(String packageName, boolean isRecursive) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		// assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration resources = classLoader.getResources(path);
		List dirs = new ArrayList();
		while (resources.hasMoreElements()) {
			URL resource = (URL) resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList classes = new ArrayList();
		for (Iterator iterator = dirs.iterator(); iterator.hasNext();) {
			File directory = (File) iterator.next();
			classes.addAll(findClasses(directory, packageName, isRecursive));
		}
		return (Class[]) classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Returns all classes found in the specified directory.
	 * 
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base
	 *                    directory
	 * @return The classes
	 * @throws ClassNotFoundException Thrown if unable to load a class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List findClasses(File directory, String packageName, boolean isRecursive) throws ClassNotFoundException {
		List classes = new ArrayList();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory() && isRecursive) {
				classes.addAll(findClasses(file, packageName + "." + file.getName(), isRecursive));
			} else if (file.getName().endsWith(".class")) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	/**
	 * Returns all classes that are in the specified jar and package name.
	 * 
	 * @param jarPath     The absolute or relative jar path.
	 * @param packageName The package name.
	 * @return Returns all classes that are in the specified jar and package name.
	 * @throws ClassNotFoundException Thrown if unable to load a class
	 * @throws IOException            Thrown if error occurs while reading the jar
	 *                                file
	 */
	@SuppressWarnings("rawtypes")
	public static Class[] getClasses(String jarPath, String packageName, boolean isRecursive) throws ClassNotFoundException, IOException {
		String[] classNames = getClassNames(jarPath, packageName, isRecursive);
		Class classes[] = new Class[classNames.length];
		for (int i = 0; i < classNames.length; i++) {
			String className = (String) classNames[i];
			classes[i] = Class.forName(className);
		}
		return classes;
	}

	/**
	 * Returns all names of classes that are defined in the specified jar and
	 * package name.
	 * 
	 * @param jarPath     The absolute or relative jar path.
	 * @param packageName The package name.
	 * @return Returns all names of classes that are defined in the specified jar
	 *         and package name.
	 * @throws IOException Thrown if error occurs while reading the jar file
	 */
	@SuppressWarnings({ "resource", "unchecked", "rawtypes" })
	public static String[] getClassNames(String jarPath, String packageName, boolean isRecursive) throws IOException {
		if (jarPath == null) {
			return new String[0];
		}

		File file;
		if (jarPath.startsWith("/") || jarPath.indexOf(':') >= 0) {
			// absolute path
			file = new File(jarPath);
		} else {
			// relative path
			String workingDir = System.getProperty("user.dir");
			file = new File(workingDir + "/" + jarPath);
		}

		ArrayList arrayList = new ArrayList();
		packageName = packageName.replaceAll("\\.", "/") + "/";
		JarInputStream jarFile = new JarInputStream(new FileInputStream(file));
		JarEntry jarEntry;
		while (true) {
			jarEntry = jarFile.getNextJarEntry();
			if (jarEntry == null) {
				break;
			}
			String name = jarEntry.getName();
			if (name.startsWith(packageName) && (name.endsWith(".class"))) {
				if (isRecursive == false && name.replaceFirst(packageName, "").contains("/")) {
					continue;
				}
				int endIndex = name.length() - 6;
				name = name.replaceAll("/", "\\.");
				name = name.substring(0, endIndex);
				arrayList.add(name);
			}
		}

		return (String[]) arrayList.toArray(new String[0]);
	}

	/**
	 * Returns all classes that are in the specified jar.
	 * 
	 * @param jarPath The absolute or relative jar path. It returns null if jarPath
	 *                is null.
	 * @return Returns all classes that are in the specified jar
	 * @throws ClassNotFoundException Thrown if unable to load a class
	 * @throws IOException            Thrown if error occurs while reading the jar
	 *                                file
	 */
	@SuppressWarnings("rawtypes")
	public static Class[] getAllClasses(String jarPath) throws ClassNotFoundException, IOException {
		if (jarPath == null) {
			return null;
		}
		String[] classNames = getAllClassNames(jarPath);
		Class classes[] = new Class[classNames.length];
		for (int i = 0; i < classNames.length; i++) {
			String className = (String) classNames[i];
			classes[i] = Class.forName(className);
		}
		return classes;
	}

	/**
	 * Returns all classes that are in the specified jar using the specified class
	 * loader.
	 * 
	 * @param jarPath     The absolute or relative jar path.
	 * @param classLoader The class loader to load the classes found in the jar.
	 * @return Returns all classes that are in the specified jar. It returns null if
	 *         jarPath or classLoader is null.
	 * @throws ClassNotFoundException Thrown if unable to load a class
	 * @throws IOException            Thrown if error occurs while reading the jar
	 *                                file
	 */
	@SuppressWarnings("rawtypes")
	public static Class[] getAllClasses(String jarPath, ClassLoader classLoader)
			throws ClassNotFoundException, IOException {
		if (jarPath == null || classLoader == null) {
			return null;
		}
		String[] classNames = getAllClassNames(jarPath);
		Class classes[] = new Class[classNames.length];
		for (int i = 0; i < classNames.length; i++) {
			String className = (String) classNames[i];
			classes[i] = classLoader.loadClass(className);
		}
		return classes;
	}

	/**
	 * Returns all names of classes that are defined in the specified jar.
	 * 
	 * @param jarPath The absolute or relative jar path.
	 * @return Returns all names of classes that are defined in the specified jar.
	 * @throws IOException Thrown if error occurs while reading the jar file
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "resource" })
	public static String[] getAllClassNames(String jarPath) throws IOException {
		if (jarPath == null) {
			return new String[0];
		}

		jarPath = jarPath.trim();
		if (jarPath.length() == 0) {
			return new String[0];
		}

		File file;
		if (jarPath.startsWith("/") || jarPath.indexOf(':') >= 0) {
			// absolute path
			file = new File(jarPath);
		} else {
			// relative path
			String workingDir = System.getProperty("user.dir");
			file = new File(workingDir + "/" + jarPath);
		}

		ArrayList arrayList = new ArrayList();
		JarInputStream jarFile = new JarInputStream(new FileInputStream(file));
		JarEntry jarEntry;
		while (true) {
			jarEntry = jarFile.getNextJarEntry();
			if (jarEntry == null) {
				break;
			}
			String name = jarEntry.getName();
			if (name.endsWith(".class")) {
				int endIndex = name.length() - 6;
				name = name.replaceAll("/", "\\.");
				name = name.substring(0, endIndex);
				arrayList.add(name);
			}
		}

		return (String[]) arrayList.toArray(new String[0]);
	}
}
