package info.jerrinot.subzero;

import info.jerrinot.subzero.internal.PropertyUserSerializer;
import info.jerrinot.subzero.internal.strategy.TypedKryoStrategy;

public class Serializer<T> extends AbstractSerializer<T> {

    /**
     * @param clazz class this serializer uses.
     *
     */
    public Serializer(Class<T> clazz) {
        super(new TypedKryoStrategy<T>(clazz, new PropertyUserSerializer()));
    }

    public Serializer(Class<T> clazz, com.esotericsoftware.kryo.Serializer serializer) {
        super(new TypedKryoStrategy<T>(clazz, UserSerializerConfig.register(clazz, serializer)));
    }

}
