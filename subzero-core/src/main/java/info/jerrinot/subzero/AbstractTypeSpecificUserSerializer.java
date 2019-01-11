package info.jerrinot.subzero;

import info.jerrinot.subzero.internal.PropertyUserSerializer;
import info.jerrinot.subzero.internal.strategy.TypedKryoStrategy;


/**
 * This class is for registering Kryo serialization for a type where you need custom serialization and/or you want
 * control over the serializable class type ID.
 *
 * The class is intended to be extended. It optionally takes a custom serializer
 * {@link com.esotericsoftware.kryo.Serializer} that will be registered with the internal Kryo serializer instance for
 * the provided class. By not providing a custom serializer, the behaviour of the class is to check for a custom
 * serializer registration in the subzero-serializers.properties properties file.
 *
 * <code>
 *      public static class MySerializer extends AbstractTypeSpecificUserSerializer<MyObject> {
 *
 *          public MySerializer() {
 *              super(MyObject.class, new MyObjectKryoSerializer());
 *          }
 *
 *      }
 * </code>
 *
 * It is also possible to define a static class type ID through overriding {@link #getTypeId()} which would otherwise
 * return an auto-generated ID unique to the type.
 *
 * @param <T> type to register Kyro serialization against
 */
public abstract class AbstractTypeSpecificUserSerializer<T> extends AbstractSerializer<T> {

    public AbstractTypeSpecificUserSerializer(Class<T> clazz, com.esotericsoftware.kryo.Serializer serializer) {
        super(new TypedKryoStrategy<T>(clazz, UserSerializerConfig.register(clazz, serializer)));
    }

    /**
     * @param clazz class this serializer uses.
     *
     */
    public AbstractTypeSpecificUserSerializer(Class<T> clazz) {
        super(new TypedKryoStrategy<T>(clazz, PropertyUserSerializer.INSTANCE));
    }
}
