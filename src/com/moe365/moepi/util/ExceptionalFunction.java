package com.moe365.moepi.util;

@FunctionalInterface
public interface ExceptionalFunction<T, E extends Exception, R> {
	R apply(T arg0) throws E;
}
