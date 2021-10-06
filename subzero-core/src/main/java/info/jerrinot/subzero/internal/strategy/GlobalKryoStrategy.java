package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import info.jerrinot.subzero.UserSerializer;
import info.jerrinot.subzero.internal.IdGeneratorUtils;

public final class GlobalKryoStrategy<T> extends KryoStrategy<T> {
    private final UserSerializer userSerializer;

    public GlobalKryoStrategy(UserSerializer registrations) {
        this.userSerializer = registrations;
    }

    @Override
    public void registerCustomSerializers(Kryo kryo) {
        userSerializer.registerAllSerializers(kryo);
    }

    @Override
    void writeObject(Kryo kryo, Output output, T object) {
        kryo.writeClassAndObject(output, object);
    }

    @Override
    T readObject(Kryo kryo, Input input) {
        return (T) kryo.readClassAndObject(input);
    }

    @Override
    public int newId() {
        return IdGeneratorUtils.globalId(getHazelcastInstance());
    }
}
