package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.util.DefaultClassResolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Delegates class loading to Hazelcast -> SubZero will use the same strategy
 * as Hazelcast.
 *
 */
final class DelegatingClassResolver extends DefaultClassResolver {

    private final ClassLoader classLoader;
    private static final Method loadClassMethodRef = getClassLoadMethod();

    public DelegatingClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    protected Class<?> getTypeByName(String className) {
        try {
            return (Class<?>) loadClassMethodRef.invoke(null, classLoader, className);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ClassNotFoundException) {
                return null;
            }
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getClassLoadMethod() {
        Class<?> classLoaderUtilClass = getClassLoaderUtilClass();
        try {
            return classLoaderUtilClass.getDeclaredMethod("loadClass", ClassLoader.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Class<?> getClassLoaderUtilClass() {
        String[] possibleNames = {
                "com.hazelcast.nio.ClassLoaderUtil",
                "com.hazelcast.internal.nio.ClassLoaderUtil"
        };

        for (String possibleName : possibleNames) {
            Class<?> loadedClass = loadClass(possibleName);
            if (loadedClass != null) return loadedClass;
        }
        throw new IllegalStateException("unable to load ClassLoaderUtil");
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoClassDefFoundError e) {
            return null;
        }
    }
}
