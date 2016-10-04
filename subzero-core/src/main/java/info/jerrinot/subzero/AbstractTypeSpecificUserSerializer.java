package info.jerrinot.subzero;

import info.jerrinot.subzero.internal.PropertyUserSerializer;
import info.jerrinot.subzero.internal.strategy.TypedKryoStrategy;

public abstract class AbstractTypeSpecificUserSerializer<T> extends AbstractSerializer<T> {

    public AbstractTypeSpecificUserSerializer(Class<T> clazz, com.esotericsoftware.kryo.Serializer serializer) {
        super(new TypedKryoStrategy<T>(clazz, UserSerializerConfig.register(clazz, serializer)));
    }

    /**
     * @param clazz class this serializer uses.
     *
     */
    public AbstractTypeSpecificUserSerializer(Class<T> clazz) {
        super(new TypedKryoStrategy<T>(clazz, new PropertyUserSerializer()));
    }
}
