package info.jerrinot.subzero;

import info.jerrinot.subzero.internal.PropertyUserSerializer;
import info.jerrinot.subzero.internal.strategy.GlobalKryoStrategy;

public class GlobalSerializer<T> extends AbstractSerializer<T> {

    public GlobalSerializer() {
        this(new PropertyUserSerializer());
    }

    public GlobalSerializer(UserSerializer propertyUserSerializer) {
        super(new GlobalKryoStrategy(propertyUserSerializer));
    }
}
