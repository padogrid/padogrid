package org.hazelcast.addon.internal.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

import org.joda.time.DateTime;

/**
 * ClassUtil provides convenience methods for handling classes.
 * 
 * @author dpark
 *
 */
public class ClassUtil {
	/**
	 * Returns true if the specified class is primitive, String, array, enum,
	 * wrapper primitive, i.e., Boolean, Byte, Character, Short, Integer, Long,
	 * Float, Double.
	 * 
	 * @param c The class to verify
	 */
	public static boolean isPrimitiveBase(Class c) {
		return c.isPrimitive() || c.isArray() || c.isEnum() || c == Integer.class || c == Short.class || c == Long.class
				|| c == Byte.class || c == Character.class || c == Float.class || c == Double.class
				|| c == Boolean.class || c == String.class;
	}

	/**
	 * Returns true if the specified object is primitive, String, array, enum,
	 * wrapper primitive, i.e., Boolean, Byte, Character, Short, Integer, Long,
	 * Float, Double.
	 * 
	 * @param obj The object to verify
	 */
	public static boolean isPrimitiveBase(Object obj) {
		if (obj == null) {
			return false;
		}
		return isPrimitiveBase(obj.getClass());
	}

	/**
	 * Returns a new instance of the specified class. If the specified class is
	 * primitive then it returns the default value of that type. For String, it
	 * returns an empty string. For enum, it returns the first enum value if any.
	 * For all others, it instantiates the class by invoking the default
	 * constructor.
	 * 
	 * @param clazz The class to instantiate
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object newInstance(Class clazz) throws InstantiationException, IllegalAccessException {
		if (clazz == String.class) {
			return "";
		} else if (clazz == boolean.class || clazz == Boolean.class) {
			return false;
		} else if (clazz == byte.class || clazz == Byte.class) {
			return (byte) 0;
		} else if (clazz == char.class || clazz == Character.class) {
			return (char) 0;
		} else if (clazz == short.class || clazz == Short.class) {
			return (short) 0;
		} else if (clazz == int.class || clazz == Integer.class) {
			return (int) 0;
		} else if (clazz == long.class || clazz == Long.class) {
			return (long) 0;
		} else if (clazz == float.class || clazz == Float.class) {
			return (float) 0;
		} else if (clazz == double.class || clazz == Double.class) {
			return (double) 0;
		} else {
			Object c[] = clazz.getEnumConstants();
			if (c == null) {
				return clazz.newInstance();
			} else if (c.length > 0) {
				return c[0];
			} else {
				return null;
			}
		}
	}

	public static Object newPrimitive(Class clazz, String value) throws InstantiationException, IllegalAccessException {
		if (clazz == null || value == null) {
			return null;
		}
		if (clazz == String.class) {
			return value;
		} else if (clazz == boolean.class || clazz == Boolean.class) {
			return Boolean.parseBoolean(value);
		} else if (clazz == byte.class || clazz == Byte.class) {
			return Byte.parseByte(value);
		} else if (clazz == char.class || clazz == Character.class) {
			return value.length() > 0 ? value.charAt(0) : null;
		} else if (clazz == short.class || clazz == Short.class) {
			return Short.parseShort(value);
		} else if (clazz == int.class || clazz == Integer.class) {
			return Integer.parseInt(value);
		} else if (clazz == long.class || clazz == Long.class) {
			return Long.parseLong(value);
		} else if (clazz == float.class || clazz == Float.class) {
			return Float.parseFloat(value);
		} else if (clazz == double.class || clazz == Double.class) {
			return Double.parseDouble(value);
		} else {
			return null;
		}
	}

	/**
	 * Returns the class type that matches the specified type name. It returns null
	 * if the matching type is not found or supported. The supported types are
	 * <ul>
	 * <li>primitives and primitive wrappers</li>
	 * <li>java.lang.String</li>
	 * <li>java.util.Date</li>
	 * <li>jorg.joda.time.DateTime</li>
	 * </ul>
	 * 
	 * @param typeName Type name.
	 */
	public static Class getType(String typeName) {
		if (typeName.equals("Boolean")) {
			return Boolean.class;
		} else if (typeName.equalsIgnoreCase("boolean")) {
			return boolean.class;
		} else if (typeName.equals("Byte")) {
			return Byte.class;
		} else if (typeName.equalsIgnoreCase("byte")) {
			return byte.class;
		} else if (typeName.equals("Character")) {
			return Character.class;
		} else if (typeName.equalsIgnoreCase("char")) {
			return char.class;
		} else if (typeName.equals("Integer")) {
			return Integer.class;
		} else if (typeName.equalsIgnoreCase("int")) {
			return int.class;
		} else if (typeName.equals("Short")) {
			return Short.class;
		} else if (typeName.equalsIgnoreCase("short")) {
			return short.class;
		} else if (typeName.equals("Long")) {
			return Long.class;
		} else if (typeName.equalsIgnoreCase("long")) {
			return long.class;
		} else if (typeName.equals("Float")) {
			return Float.class;
		} else if (typeName.equalsIgnoreCase("float")) {
			return float.class;
		} else if (typeName.equals("Double")) {
			return Double.class;
		} else if (typeName.equalsIgnoreCase("double")) {
			return double.class;
		} else if (typeName.equalsIgnoreCase("String")) {
			return String.class;
		} else if (typeName.equalsIgnoreCase("Date")) {
			return Date.class;
		} else if (typeName.equalsIgnoreCase("DateTime")) {
			return DateTime.class;
		} else {
			return null;
		}
	}

	/**
	 * Adds the specified file path to the current thread's class loader.
	 * 
	 * @param filePath Directory or jar file.
	 * @throws MalformedURLException Thrown if the file path is invalid.
	 */
	public static void addClassPath(File filePath) throws MalformedURLException {
		ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

		// Add the conf dir to the classpath
		// Chain the current thread classloader
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { filePath.toURI().toURL() },
				currentThreadClassLoader);

		// Replace the thread classloader - assumes
		// you have permissions to do so
		Thread.currentThread().setContextClassLoader(urlClassLoader);
	}
}
