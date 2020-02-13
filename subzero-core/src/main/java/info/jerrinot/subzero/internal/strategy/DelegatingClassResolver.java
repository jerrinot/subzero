package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.util.DefaultClassResolver;
import info.jerrinot.subzero.internal.ClassLoaderUtils;

import java.lang.reflect.InvocationTargetException;

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
            return ClassLoaderUtils.loadClass(className, classLoader);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ClassNotFoundException) {
                return null;
            }
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

}
