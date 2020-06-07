package org.coherence.addon.kafka.debezium;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import com.netcrest.pado.internal.util.ObjectUtil;

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

	public ObjectConverter(String keyClassName, String[] keyFieldNames, String valueClassName, String[] valueFieldNames)
			throws ClassNotFoundException {
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
