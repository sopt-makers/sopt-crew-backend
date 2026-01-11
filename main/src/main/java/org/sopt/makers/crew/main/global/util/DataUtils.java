package org.sopt.makers.crew.main.global.util;

import java.util.function.Function;

public class DataUtils {

	public static <T, R> R safeData(T obj, Function<T, R> mapper) {
		return obj != null ? mapper.apply(obj) : null;
	}
}
