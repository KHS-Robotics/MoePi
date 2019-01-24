package com.moe365.moepi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utility for reflections, most used for implementing Externalizable.
 * @author mailmindlin (FRC Team 365)
 */
public class ReflectionUtils {
	protected static final Field modifiersField;

	static {
		Field tmp = null;
		try {
			tmp = Field.class.getDeclaredField("modifiers");
			tmp.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException ex) {
			ex.printStackTrace();
		}
		modifiersField = tmp;
	}
	@FunctionalInterface
	protected static interface FieldOperator {
		void apply(Field field) throws IllegalAccessException;
	}

	/**
	 * Set a double field on an object, even if the field is null. If the field
	 * is final, then the updated value might not always work, because of how
	 * final fields are handled by the JIT compiler.
	 * 
	 * @param obj
	 *            the object to set the field on
	 * @param fieldName
	 *            name of the field
	 * @param value
	 *            value to set the field to
	 * @throws NoSuchFieldException
	 *             if the field specified is not there
	 * @throws IllegalArgumentException
	 *             If the field specified is not a double field
	 * @throws IllegalAccessException
	 *             If the SecurityManager blocks us from setting the field
	 */
	public static void setDouble(Object obj, String fieldName, double value)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		applyOnField(obj.getClass(), fieldName, field->field.setDouble(obj, value));
	}
	
	/**
	 * Set an int field on the given object
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void setInt(Object obj, String fieldName, int value)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		applyOnField(obj.getClass(), fieldName, field->field.setInt(obj, value));
	}
	
	/**
	 * Set a float field on the given object
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void setFloat(Object obj, String fieldName, float value)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		applyOnField(obj.getClass(), fieldName, field->field.setFloat(obj, value));
	}
	
	/**
	 * Set a long field on athegiven object
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void setLong(Object obj, String fieldName, long value)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		applyOnField(obj.getClass(), fieldName, field->field.setLong(obj, value));
	}
	
	/**
	 * Apply an operation on a field of a class. Useful for getting/setting protected/final fields.
	 * @param clazz
	 * @param fieldName
	 * @param action
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static void applyOnField(Class<?> clazz, String fieldName, FieldOperator action) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		Field field;
		try {
			field = clazz.getField(fieldName);
		} catch (NoSuchFieldException ex1) {
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ex2) {
				throw ex2;
			}
		}
		final boolean wasAccessible = field.isAccessible();
		final int modifiers = field.getModifiers();

		try {
			field.setAccessible(true);
			modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
			action.apply(field);
		} finally {
			if (field.isAccessible() ^ wasAccessible)
				field.setAccessible(wasAccessible);
			if (field.getModifiers() != modifiers)
				modifiersField.setInt(field, modifiers);
		}
	}
}