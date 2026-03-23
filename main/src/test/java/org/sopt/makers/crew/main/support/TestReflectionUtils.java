package org.sopt.makers.crew.main.support;

import java.lang.reflect.Field;

public final class TestReflectionUtils {

	private TestReflectionUtils() {
	}

	public static void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
