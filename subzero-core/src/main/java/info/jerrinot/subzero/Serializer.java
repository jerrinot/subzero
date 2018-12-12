package info.jerrinot.subzero;

import info.jerrinot.subzero.internal.PropertyUserSerializer;
import info.jerrinot.subzero.internal.strategy.GlobalKryoStrategy;
import info.jerrinot.subzero.internal.strategy.TypedKryoStrategy;

public final class Serializer<T> extends AbstractSerializer<T> {

    Serializer() {
        super(new GlobalKryoStrategy<T>(PropertyUserSerializer.INSTANCE));
    }

    /**
     * @param clazz class this serializer uses.
     *
     */
    public Serializer(Class<T> clazz) {
        super(new TypedKryoStrategy<T>(clazz, PropertyUserSerializer.INSTANCE));
    }

}
