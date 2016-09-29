package info.jerrinot.subzero;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.Serializer;

/**
 * Hook for registering customer Kryo serializers.
 *
 * It's suitable for scenario where property-based registration of custom serializers is not suitable.
 * It gives you control on how {@link com.esotericsoftware.kryo.Serializer} instances are created.
 *
 * In most cases you do not need to implement this interface yourself: You can use the
 * {@link UserSerializerConfig#register(Class, Serializer)} static method.
 *
 * <code>
 *      import static info.jerrinot.subzero.UserSerializer.register;
 *      [...]
 *
 *      public static class MySerializer extends Serializer<MyObject> {
 *
 *          public MySerializer() {
 *              super(MyObject.class);
 *          }
 *
 *          @Override
 *          public UserSerializer userSerializers() {
 *              return register(MyObject.class, new MyCustomKryoSerializer());
 *          }
 *      }
 * </code>
 *
 */
public interface UserSerializer {

    /**
     * Register single serializer. This is useful when SubZero is configured to serializer just a single type.
     * It allows implementations to provide a fail-fast semantic when requested type is not known.
     *
     * @param kryo
     * @param clazz
     */
    void registerSingleSerializer(Kryo kryo, Class clazz);

    /**
     * Register all serializers. It's called when the exact type is not known up-front. This is the case
     * of Global Subzero serializers and also when a single type is registered declaratively
     * in pre-3.8 Hazelcast versions.
     *
     * @param kryo
     */
    void registerAllSerializers(Kryo kryo);

}
