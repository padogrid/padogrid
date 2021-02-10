package org.apache.geode.addon.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

/**
 * ObjectUtil introspects objects and provides methods to dynamically invoke
 * setters and getters.
 * 
 * @author dpark
 *
 */
public class ObjectUtil {
	private SimpleDateFormat dateFormatter;
	private DateTimeFormatter jodaFormatter;
	private NumberFormat numberFormat = NumberFormat.getInstance();
	private StringBuffer buffer = new StringBuffer(100);
	private boolean verbose = false;
	private String verboseTag = "";
	private boolean isColumnNamesCaseSensitive = true;

	@SuppressWarnings("rawtypes")
	private Object createKey(Class<?> keyClass, Object[] pkClassSetters, String[] keyNames, Map keyMap,
			String compositeKeyDelimiter) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ParseException {
		Object key = null;
		if (keyNames.length == 1) {
			key = keyMap.get(keyNames[0]);
		} else {
			if (keyClass == null || keyClass == String.class) {
				String strKey = null;
				for (String keyName : keyNames) {
					Object val = keyMap.get(keyName);
					if (val != null) {
						if (strKey == null) {
							strKey = val.toString();
						} else {
							strKey += compositeKeyDelimiter + val;
						}
					}
				}
				key = strKey;
			} else if (ClassUtil.isPrimitiveBase(keyClass) == false) {
				key = createObject(keyClass, pkClassSetters, keyNames, keyMap);
			}
		}
		return key;
	}

	private Object createKey(Class<?> keyClass, Object[] pkClassSetters, String[] pkColumnNames, Class<?> valueClass,
			Object[] valueClassGetters, Object dataObject, String compositeKeyDelimiter)
			throws ParseException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object key = null;
		String strKey = null;
		for (int i = 0; i < pkColumnNames.length; i++) {
			Method method = getGetterMethod(valueClass, pkColumnNames[i]);
			if (method != null) {
				Object val = method.invoke(dataObject);
				if (val != null) {
					if (strKey == null) {
						strKey = val.toString();
					} else {
						strKey += compositeKeyDelimiter + val;
					}
				}
			}
		}
		key = strKey;
		return key;
	}

	protected Object createObject(Class<?> clazz, Object[] clazzSetters, Object[] values, int startValuesIndex)
			throws InstantiationException, IllegalAccessException, ParseException, IllegalArgumentException,
			InvocationTargetException {
		// Handle primitives
		if (ClassUtil.isPrimitiveBase(clazz)) {
			if (values.length > startValuesIndex) {
				return values[startValuesIndex];
			} else {
				return null;
			}
		}

		// Non-primitives
		Object dataObject = clazz.newInstance();
		return updateObject(dataObject, clazzSetters, values, startValuesIndex);
	}

	private Object updateObject(Object dataObject, Object[] clazzSetters, Object[] values, int startValuesIndex)
			throws InstantiationException, IllegalAccessException, ParseException, IllegalArgumentException,
			InvocationTargetException {
		int valuesIndex = startValuesIndex;
		for (int i = 0; i < clazzSetters.length; i++) {
			Object object = clazzSetters[i];
			if (object == null) {
				continue;
			}
			// If the number of columns in the line is greater
			// than the number of tokens then silently ignore.
			if (valuesIndex >= values.length) {
				break;
			}

			// Invoke setter
			Object value = values[i];
			if (object instanceof Field) {
				Field field = null;
				try {
					field = (Field) object;
					if (value instanceof Number) {
						Number number = (Number)value;
						Class<?> type = field.getType();
						if (type.isInstance(value)) {
							field.set(dataObject, value);
						} else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
							field.set(dataObject, number.shortValue());
						} else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
							field.set(dataObject, number.intValue());
						} else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
							field.set(dataObject, number.longValue());
						} else if (Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type)) {
							field.set(dataObject, number.byteValue());
						} else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
							field.set(dataObject, number.doubleValue());
						} else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
							field.set(dataObject, number.floatValue());
						}
					} else {
						field.set(dataObject, value);
					}
				} catch (IllegalArgumentException ex) {
					String valueClassName;
					if (value == null) {
						valueClassName = "null";
					} else {
						valueClassName = value.getClass().getName();
					}
					throw new IllegalArgumentException("Field: " + field + ", Value: [" + valueClassName + ", " + value + "]", ex);
				}
			} else {
				Method method = null;
				try {
					method = (Method) object;
					if (value instanceof Number) {
						Number number = (Number)value;
						Class<?> type = method.getParameterTypes()[0];
						if (type.isInstance(value)) {
							method.invoke(dataObject, value);
						} else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
							method.invoke(dataObject, number.shortValue());
						} else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
							method.invoke(dataObject, number.intValue());
						} else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
							method.invoke(dataObject, number.longValue());
						} else if (Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type)) {
							method.invoke(dataObject, number.byteValue());
						} else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
							method.invoke(dataObject, number.doubleValue());
						} else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
							method.invoke(dataObject, number.floatValue());
						}
					} else {
						method.invoke(dataObject, value);
					}
				} catch (IllegalArgumentException ex) {
					String valueClassName;
					if (value == null) {
						valueClassName = "null";
					} else {
						valueClassName = value.getClass().getName();
					}
					throw new IllegalArgumentException("Method: " + method + ", Value: [" + valueClassName + ", " + value + "]", ex);
				}
			}

		}
		return dataObject;
	}

	/**
	 * Creates an object with the token values.
	 * 
	 * @param clazz        Class to create the object.
	 * @param clazzSetters Fields and methods of the same order as the token values.
	 *                     If clazz is primitive or String, then clazzSetters is not
	 *                     required.
	 * @param tokens       Tokens that are assigned to the clazzSetters.
	 * @return A new instance of the specified clazz set to the token values.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParseException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	protected Object createObject(Class<?> clazz, Object[] clazzSetters, String[] tokens, int startTokenIndex)
			throws InstantiationException, IllegalAccessException, ParseException, IllegalArgumentException,
			InvocationTargetException {
		// Handle primitives
		if (ClassUtil.isPrimitiveBase(clazz)) {
			if (tokens.length > startTokenIndex) {
				return ClassUtil.newPrimitive(clazz, tokens[startTokenIndex]);
			} else {
				return null;
			}
		}

		// Non-primitives
		Object dataObject = clazz.newInstance();
		return updateObject(dataObject, clazzSetters, tokens, startTokenIndex);
	}

	private Object updateObject(Object dataObject, Object[] clazzSetters, String[] tokens, int startTokenIndex)
			throws InstantiationException, IllegalAccessException, ParseException, IllegalArgumentException,
			InvocationTargetException {
		int tokenIndex = startTokenIndex;
		for (int i = 0; i < clazzSetters.length; i++) {
			Object object = clazzSetters[i];
			if (object == null) {
				continue;
			}
			// If the number of columns in the line is greater
			// than the number of tokens then silently ignore.
			if (tokenIndex >= tokens.length) {
				break;
			}

			// Invoke setter
			String stringValue = tokens[i];
			Object value;
			if (object instanceof Field) {
				Field field = (Field) object;
				value = getValue(stringValue, field);
				field.set(dataObject, value);
			} else {
				Method method = (Method) object;
				value = getValue(stringValue, method);
				method.invoke(dataObject, value);
			}
		}

		return dataObject;
	}

	/**
	 * Creates an object for the specified class.
	 * 
	 * @param clazz        Class to be instantiated
	 * @param clazzSetters Setters of the specified class that may be mixed with
	 *                     public fields and methods
	 * @param keyNames     Key names in KeyMap. The order of this array must match
	 *                     the order of clazzSetter.
	 * @param keyMap       KeyMap object containing values to be extracted and
	 *                     assigned the setters.
	 * @return A new instance of the specified class with values in KeyMap assigned.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParseException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("rawtypes")
	private Object createObject(Class<?> clazz, Object[] clazzSetters, String[] keyNames, Map keyMap)
			throws InstantiationException, IllegalAccessException, ParseException, IllegalArgumentException,
			InvocationTargetException {
		Object dataObject = clazz.newInstance();
		for (int i = 0; i < clazzSetters.length; i++) {
			String keyName = keyNames[i];
			Object object = clazzSetters[i];
			if (object == null) {
				continue;
			}

			// Invoke setter
			if (object instanceof Field) {
				Field field = (Field) object;
				Object value = keyMap.get(keyName);
				field.set(dataObject, value);
			} else {
				Method method = (Method) object;
				Object value = keyMap.get(keyName);
				method.invoke(dataObject, value);
			}
		}

		return dataObject;
	}

	private Object getValue(String strVal, Field field) throws ParseException {
		Class<?> clazz = field.getType();
		return getValue(strVal, clazz);
	}

	private Object getValue(String strVal, Method method) throws ParseException {
		Class<?> clazz = method.getParameterTypes()[0];
		return getValue(strVal, clazz);
	}

	/**
	 * Converts the specified string value to an instance of the specified class. It
	 * supports primitives, String, java.util.Date, org.joda.time.DateTime, and
	 * BigDecimal. If the specified class is not supported then it returns the
	 * specified string.
	 * 
	 * @param strVal String value to convert
	 * @param clazz  Class to convert the string value
	 * @throws ParseException
	 */
	private Object getValue(String strVal, Class<?> clazz) throws ParseException {
		if (strVal == null || strVal.length() == 0) {
			return null;
		}
		if (clazz == Boolean.class || clazz == boolean.class) {
			return new Boolean(strVal);
		} else if (clazz == Byte.class || clazz == byte.class) {
			return new Byte(strVal);
		} else if (clazz == Character.class || clazz == char.class) {
			return new Character(strVal.charAt(0));
		} else if (clazz == Integer.class || clazz == int.class) {
			strVal = strVal.replaceAll(",", "");
			if (strVal.endsWith("-")) {
				strVal = strVal.substring(0, strVal.length() - 1);
				return -new Integer(strVal);
			}
			return new Integer(strVal);
		} else if (clazz == Short.class || clazz == short.class) {
			strVal = strVal.replaceAll(",", "");
			if (strVal.endsWith("-")) {
				strVal = strVal.substring(0, strVal.length() - 1);
				return -new Short(strVal);
			}
			return new Short(strVal);
		} else if (clazz == Long.class || clazz == long.class) {
			strVal = strVal.replaceAll(",", "");
			if (strVal.endsWith("-")) {
				strVal = strVal.substring(0, strVal.length() - 1);
				return -new Long(strVal);
			}
			return new Long(strVal);
		} else if (clazz == Float.class || clazz == float.class) {
			strVal = strVal.replaceAll(",", "");
			if (strVal.endsWith("-")) {
				strVal = strVal.substring(0, strVal.length() - 1);
				return -new Float(strVal);
			}
			return new Float(strVal);
		} else if (clazz == Double.class || clazz == double.class) {
			strVal = strVal.replaceAll(",", "");
			if (strVal.endsWith("-")) {
				strVal = strVal.substring(0, strVal.length() - 1);
				return -new Double(strVal);
			} else {
				return new Double(strVal);
			}
		} else if (clazz == String.class) {
			return new String(strVal);
		} else if (clazz == Date.class) {
			return dateFormatter.parse(strVal);
		} else if (clazz == DateTime.class) {
			return jodaFormatter.parseDateTime(strVal);
		} else if (clazz == BigDecimal.class) {
			return new BigDecimal(strVal);
		} else {
			return strVal;
		}
	}

	/**
	 * Returns public Fields and Methods that begin with the specified method
	 * prefix. Methods override Fields if they have the same name.
	 * 
	 * @param clazz The class to introspect.
	 * @return List of public Fields and Methods in the same order as the specified
	 *         column names. Undefined fields/methods are set to null.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object[] getFieldMethodList(Class<?> clazz, String columnNames[], String methodPrefix,
			int parameterCount) {
		if (clazz == null) {
			return null;
		}
		if (ClassUtil.isPrimitiveBase(clazz)) {
			return null;
		}

		Field[] fields = clazz.getDeclaredFields();
		Method[] methods = clazz.getMethods();

		HashMap map = new HashMap();

		// scan fields
		for (int i = 0; i < fields.length; i++) {
			int modifiers = fields[i].getModifiers();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
				// columns.add(fields[i].getName());
				String propertyName = fields[i].getName();
				if (isColumnNamesCaseSensitive) {
					// Support lowercase and uppercase for the first letter of
					// property name
					map.put(getAlternatePropertyName(propertyName), fields[i]);
					map.put(propertyName, fields[i]);
				} else {
					map.put(propertyName.toLowerCase(), fields[i]);
				}
			}
		}
		// scan methods
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			int modifiers = method.getModifiers();
			String methodName = methods[i].getName();
			if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && methodName.startsWith(methodPrefix)
					&& (method.getParameterTypes().length == parameterCount) && !methodName.equals("getClass")) {
				String propertyName = methodName.substring(3);
				if (propertyName.length() > 0) {
					if (isColumnNamesCaseSensitive) {
						// Support lowercase and uppercase for the first letter
						// of property name
						map.put(getAlternatePropertyName(propertyName), methods[i]);
						map.put(propertyName, methods[i]);
					} else {
						map.put(propertyName.toLowerCase(), methods[i]);
					}
				}
			}
		}

		ArrayList list = new ArrayList();
		if (columnNames != null) {
			for (int i = 0; i < columnNames.length; i++) {
				Object obj;
				if (isColumnNamesCaseSensitive) {
					obj = map.get(columnNames[i]);
				} else {
					obj = map.get(columnNames[i].toLowerCase());
				}
				list.add(obj);
			}
		} else {
			Set<Map.Entry> set = map.entrySet();
			for (Map.Entry entry : set) {
				entry.getKey();
				list.add(entry.getValue());
			}
		}
		return list.toArray();
	}

	/**
	 * Returns the class field that matches the specified column name. It returns
	 * null if the matching field is not found.
	 * 
	 * @param clazz      The class in which the field exists
	 * @param columnName The column name
	 */
	protected Field getField(Class<?> clazz, String columnName) {
		if (clazz == null) {
			return null;
		}
		try {
			Field field = null;
			if (isColumnNamesCaseSensitive) {
				try {
					field = clazz.getField(columnName);
				} catch (Exception ex) {
					// ignore
				}
				if (field == null) {
					field = clazz.getField(getAlternatePropertyName(columnName));
				}
			} else {
				Field fields[] = clazz.getFields();
				for (Field field2 : fields) {
					if (columnName.equalsIgnoreCase(field2.getName())) {
						field = field2;
						break;
					}
				}
			}
			return field;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the getter method that matches the specified column name.It returns
	 * null if the matching method is not found.
	 * 
	 * @param clazz      The class in which the getter method exists.
	 * @param columnName The column name.
	 */
	protected Method getGetterMethod(Class<?> clazz, String columnName) {
		if (clazz == null || columnName == null || columnName.length() == 0) {
			return null;
		}
		try {
			Method method = null;
			if (isColumnNamesCaseSensitive) {
				try {
					method = clazz.getMethod("get" + columnName);
				} catch (Exception ex) {
					// ignore
				}
				if (method == null) {
					method = clazz.getMethod("get" + getAlternatePropertyName(columnName));
				}
			} else {
				Method methods[] = clazz.getMethods();
				String getter = "get" + columnName;
				for (Method method2 : methods) {
					if (getter.equalsIgnoreCase(method2.getName())) {
						method = method2;
						break;
					}
				}
			}
			return method;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the alternate property name. If the first letter is lower case then
	 * it turns the upper case, and vice versa. It returns null if the specified
	 * property is null or empty.
	 * 
	 * @param property Property name
	 */
	private String getAlternatePropertyName(String property) {
		if (property == null || property.length() == 0) {
			return null;
		}
		if (Character.isUpperCase(property.charAt(0))) {
			return Character.toLowerCase(property.charAt(0)) + property.substring(1);
		} else {
			return Character.toUpperCase(property.charAt(0)) + property.substring(1);
		}
	}

	/**
	 * Returns true if the column names are case senstivie. Default: true
	 */
	public boolean isColumnNamesCaseSensitive() {
		return isColumnNamesCaseSensitive;
	}

	/**
	 * Enable or disable column name case sensitivity. Default: true
	 * 
	 * @param isColumnNamesCaseSensitive true to make column names case sensitive,
	 *                                   false to make column names case
	 *                                   insensitive.
	 */
	public void setColumnNamesCaseSensitive(boolean isColumnNamesCaseSensitive) {
		this.isColumnNamesCaseSensitive = isColumnNamesCaseSensitive;
	}
}
