package info.jerrinot.subzero.internal;

import com.hazelcast.client.impl.HazelcastClientInstanceImpl;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;

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
            if (hz instanceof HazelcastClientInstanceImpl) {
                HazelcastClientInstanceImpl client = (HazelcastClientInstanceImpl)hz;
                return client.getClientConfig().getClassLoader();
            } else {
                HazelcastClientProxy client = (HazelcastClientProxy)hz;
                return client.getClientConfig().getClassLoader();
            }
        }
    }
}
