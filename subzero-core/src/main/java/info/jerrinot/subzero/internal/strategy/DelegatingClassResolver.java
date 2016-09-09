package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.util.DefaultClassResolver;
import com.hazelcast.nio.ClassLoaderUtil;

/**
 * Delegates class loading to Hazelcast -> SubZero will use the same strategy
 * as Hazelcast.
 *
 */
final class DelegatingClassResolver extends DefaultClassResolver {

    private final ClassLoader classLoader;

    public DelegatingClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    protected Class<?> getTypeByName(String className) {
        try {
            return ClassLoaderUtil.loadClass(classLoader, className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
