package info.jerrinot.subzero.internal;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassLoaderUtils {
    private static final boolean DEBUG_CLASSLOADING = Boolean.getBoolean("subzero.debug.classloading");
    private static final Method loadClassMethodRef = getClassLoadMethod();

    public static ClassLoader getConfiguredClassLoader(HazelcastInstance hz) {
        try {
            return tryToGetClassLoader(hz);
        } catch (RuntimeException e) {
            if (DEBUG_CLASSLOADING) {
                throw e;
            }
            return null;
        }
    }

    private static ClassLoader tryToGetClassLoader(HazelcastInstance hz) {
        try {
            Config config = hz.getConfig();
            return config.getClassLoader();
        } catch (UnsupportedOperationException e) {
            //ok, this is a client instance -> it does not support getConfig()
            try {
                Method getClientConfigMethod = hz.getClass().getMethod("getClientConfig");
                ClientConfig clientConfig = (ClientConfig) getClientConfigMethod.invoke(hz);
                return clientConfig.getClassLoader();
            } catch (NoSuchMethodException e1) {
                throw new IllegalArgumentException("Unknown instance object " + hz, e1);
            } catch (IllegalAccessException e1) {
                throw new IllegalArgumentException("Unknown instance object " + hz, e1);
            } catch (InvocationTargetException e1) {
                throw new IllegalArgumentException("Unknown instance object " + hz, e1);
            }
        }
    }

    public static Class<?> loadClass(String className, ClassLoader classLoader) throws InvocationTargetException, IllegalAccessException {
        return (Class<?>) loadClassMethodRef.invoke(null, classLoader, className);
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
