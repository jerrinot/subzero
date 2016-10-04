package info.jerrinot.subzero;

import info.jerrinot.subzero.internal.strategy.GlobalKryoStrategy;

public abstract class AbstractGlobalUserSerializer<T> extends AbstractSerializer<T> {

    public AbstractGlobalUserSerializer(UserSerializer userSerializer) {
        super(new GlobalKryoStrategy(userSerializer));
    }
}
