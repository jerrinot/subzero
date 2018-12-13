package info.jerrinot.subzero.internal;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassLoaderUtils {
    private static final boolean DEBUG_CLASSLOADING = Boolean.getBoolean("subzero.debug.classloading");

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
}
