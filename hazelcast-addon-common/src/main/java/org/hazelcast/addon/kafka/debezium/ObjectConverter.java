package org.hazelcast.addon.kafka.debezium;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.ParseException;

import org.hazelcast.addon.internal.util.ObjectUtil;

/**
 * ObjectConverter converts a list of field values to an instance of the
 * specified data class.
 * 
 * @author dpark
 *
 */
public class ObjectConverter extends ObjectUtil {

	private Class<?> keyClass;
	private Object[] keyClassSetters;
	private String[] keyFieldNames;
	private Class<?> valueClass;
	private Object[] valueClassSetters;
	private String[] valueFieldNames;
	private Class<?>[] valueFieldTypes;

	public ObjectConverter(String keyClassName, String[] keyFieldNames, String valueClassName, String[] valueFieldNames) throws ClassNotFoundException {
		keyClass = Class.forName(keyClassName);
		valueClass = Class.forName(valueClassName);
		init(keyClass, keyFieldNames, valueClass, valueFieldNames);
	}

	public ObjectConverter(Class<?> keyClass, String[] keyColumnNames, Class<?> valueClass, String[] valueColumnNames) {
		init(keyClass, keyColumnNames, valueClass, valueColumnNames);
	}
	
	private void init(Class<?> keyClass, String[] keyFieldNames, Class<?> valueClass, String[] valueFieldNames) {
		this.keyClass = keyClass;
		this.keyFieldNames = keyFieldNames;
		this.valueClass = valueClass;
		this.valueFieldNames = valueFieldNames;

		keyClassSetters = getFieldMethodList(valueClass, keyFieldNames, "set", 1);
		valueClassSetters = getFieldMethodList(valueClass, valueFieldNames, "set", 1);
		valueFieldTypes = new Class<?>[valueClassSetters.length];
		for (int i = 0; i < valueClassSetters.length; i++) {
			Method method = (Method)valueClassSetters[i];
			if (method != null) {
				Parameter param = method.getParameters()[0];
				valueFieldTypes[i] = param.getType();
			}
		}
	}
	
	public Class<?>[] getValueFielTypes()
	{
		return valueFieldTypes;
	}

	public Object createKeyObject(Object[] keyFieldValues) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ParseException {
		return createObject(keyClass, keyClassSetters, keyFieldValues, 0);
	}

	public Object createValueObject(Object[] valueFieldValues) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ParseException {
		return createObject(valueClass, valueClassSetters, valueFieldValues, 0);
	}
}
