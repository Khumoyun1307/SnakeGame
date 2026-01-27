package com.snakegame.testutil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Small reflection helpers for tests.
 *
 * <p>Provides best-effort access to private fields and methods without adding additional
 * dependencies.</p>
 */
public final class Reflect {
    private Reflect() {}

    /**
     * Reads a static field value from a class.
     *
     * @param clazz target class
     * @param name field name
     * @return field value
     */
    public static Object getStaticField(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read static field " + clazz.getName() + "." + name, e);
        }
    }

    /**
     * Reads an instance field value from an object.
     *
     * @param target target instance
     * @param name field name
     * @return field value
     */
    public static Object getField(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read field " + target.getClass().getName() + "." + name, e);
        }
    }

    /**
     * Writes an instance field value on an object.
     *
     * @param target target instance
     * @param name field name
     * @param value value to set
     */
    public static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + target.getClass().getName() + "." + name, e);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Invokes a declared method on an object.
     *
     * @param target target instance
     * @param methodName method name
     * @param paramTypes parameter types for lookup
     * @param args arguments to pass
     * @return method return value
     * @param <T> expected return type
     */
    public static <T> T call(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method m = target.getClass().getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return (T) m.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call " + target.getClass().getName() + "#" + methodName, e);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Invokes a declared static method on a class.
     *
     * @param clazz target class
     * @param methodName method name
     * @param paramTypes parameter types for lookup
     * @param args arguments to pass
     * @return method return value
     * @param <T> expected return type
     */
    public static <T> T callStatic(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method m = clazz.getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return (T) m.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call " + clazz.getName() + "#" + methodName, e);
        }
    }
}
