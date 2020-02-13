package info.jerrinot.subzero.it;

import com.hazelcast.core.HazelcastInstance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Abstract away different IMap package between Hazelcast 3.x and Hazelcast 4.x
 *
 * @param <K>
 * @param <V>
 */
public final class MapProxy<K, V> {

    private final Object imapObject;

    private MapProxy(Object imapObject) {
        this.imapObject = imapObject;
    }

    private static final Method GET_MAP_PROXY_METHOD;
    private static final Method SET_KEY_METHOD;
    private static final Method GET_KEY_METHOD;

    static {
        try {
            GET_MAP_PROXY_METHOD = HazelcastInstance.class.getMethod("getMap", String.class);
            Class<?> imapClass = GET_MAP_PROXY_METHOD.getReturnType();
            SET_KEY_METHOD = imapClass.getMethod("set", Object.class, Object.class);
            GET_KEY_METHOD = imapClass.getMethod("get", Object.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Hazelcast instance does not have getMap()");
        }
    }

    /**
     * Create new IMap proxy
     *
     * @param instance hazelcast instance to use to get the proxy
     * @param mapName name of the map
     * @param <K> map Key
     * @param <V> map Value
     * @return new Map proxy to access IMap
     */
    public static <K, V> MapProxy<K, V> newProxy(HazelcastInstance instance, String mapName) {
        Object imap = invokeReflectively(GET_MAP_PROXY_METHOD, instance, mapName);
        return new MapProxy<K, V>(imap);
    }

    public void set(K k, V v) {
        invokeReflectively(SET_KEY_METHOD, imapObject, k, v);
    }

    public V get(K k) {
        return (V) invokeReflectively(GET_KEY_METHOD, imapObject, k);
    }

    private static Object invokeReflectively(Method method, Object object, Object... args) {
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw  new IllegalStateException("Error while invoking method reflectively", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else throw  new IllegalStateException("Error while invoking method reflectively", cause);
        }
    }
}
