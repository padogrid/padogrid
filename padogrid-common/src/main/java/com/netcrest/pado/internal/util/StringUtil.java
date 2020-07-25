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

/**
 * StringUtil provides convenience mthods for manipulating the specified string
 * value.
 * 
 * @author dpark
 *
 */
public class StringUtil {
	/**
	 * Returns the short name of the fully-qualified class name.
	 * 
	 * @param className The fully-qualified class name.
	 */
	public static String getShortClassName(String className) {
		if (className == null) {
			return null;
		}
		String shortName = null;
		int index = className.lastIndexOf('.');
		if (index == -1 || index >= className.length() - 1) {
			shortName = className;
		} else {
			shortName = className.substring(index + 1);
		}
		return shortName;
	}

	/**
	 * Returns the short class name of the specified object.
	 * 
	 * @param obj The object from which its short name is to be derived.
	 */
	public static String getShortClassName(Object obj) {
		if (obj == null) {
			return null;
		}
		return getShortClassName(obj.getClass().getName());
	}

	/**
	 * Trims the matching character found in the left end of the string.
	 * 
	 * @param str The string to trim.
	 * @param c   The character remove.
	 */
	public static String trimLeft(String str, char c) {
		if (str == null) {
			return str;
		}
		int len = str.length();
		int index = 0;
		while (index < len && str.charAt(index++) == c)
			;
		index--;
		if (index < 0) {
			return "";
		} else if (index < len) {
			return str.substring(index);
		} else {
			return str;
		}
	}

	/**
	 * Trims the matching character found in right end of the string.
	 * 
	 * @param str The string to trim.
	 * @param c   The character remove.
	 */
	public static String trimRight(String str, char c) {
		if (str == null) {
			return str;
		}
		int len = str.length();
		int index = len - 1;
		while (index >= 0 && str.charAt(index--) == c)
			;
		index++;
		if (index > len - 1) {
			return str;
		} else if (index >= 0) {
			return str.substring(0, index + 1);
		} else {
			return "";
		}
	}

	/**
	 * Trims whitespace characters. Same as String.trim() except it handles null
	 * value.
	 */
	public static String trim(String str) {
		if (str == null) {
			return null;
		}
		return str.trim();
	}

	/**
	 * Trims all of the matching character in the string.
	 * 
	 * @param str The string to trim.
	 * @param c   The character remove.
	 */
	public static String trim(String str, char c) {
		return trimRight(trimLeft(str, c), c);
	}

	/**
	 * Replaces the all of the matching oldValue in the string with the newValue.
	 * 
	 * @param str      The string to replace matching substring.
	 * @param oldValue The old value to match and replace.
	 * @param newValue The new value to replace the old value with.
	 */
	public static String replace(String str, String oldValue, String newValue) {
		if (str == null || oldValue == null || newValue == null) {
			return str;
		}

		int index = str.indexOf(oldValue);
		if (index != -1) {
			int oldValueLen = oldValue.length();
			int newValueLen = newValue.length();
			String head;
			String tail = str;
			StringBuffer buffer = new StringBuffer(str.length() + newValueLen);
			do {
				head = tail.substring(0, index);
				buffer.append(head);
				buffer.append(newValue);
				tail = tail.substring(index + oldValueLen);
				index = tail.indexOf(oldValue);
			} while (index != -1);
			buffer.append(tail);

			str = buffer.toString();
		}

		return str;
	}

	public static String getLeftPaddedString(String str, int maxSize, char pad) {
		if (str == null) {
			return str;
		}
		int diff = maxSize - str.length();
		StringBuffer buffer = new StringBuffer(maxSize);
		for (int i = 0; i < diff; i++) {
			buffer.append(pad);
		}
		buffer.append(str);
		return buffer.toString();
	}

	public static String getRightPaddedString(String str, int maxSize, char pad) {
		if (str == null) {
			return str;
		}
		int diff = maxSize - str.length();
		StringBuffer buffer = new StringBuffer(maxSize);
		buffer.append(str);
		for (int i = 0; i < diff; i++) {
			buffer.append(pad);
		}
		return buffer.toString();
	}

	/**
	 * Removes extra white spaces, ie., spaces, tabs.
	 * 
	 * @param str
	 * @return
	 */
	public static String removeExtraSpaces(String str) {
		if (str == null) {
			return str;
		}
		return str.replaceAll("\\s+", " ");
	}
}
