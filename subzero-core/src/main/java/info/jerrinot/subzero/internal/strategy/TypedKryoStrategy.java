package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import info.jerrinot.subzero.UserSerializer;
import info.jerrinot.subzero.internal.IdGeneratorUtils;

public class TypedKryoStrategy<T> extends KryoStrategy<T> {

    private final Class<T> clazz;
    private final UserSerializer userSerializer;

    public TypedKryoStrategy(Class<T> clazz, UserSerializer registrations) {
        this.clazz = clazz;
        this.userSerializer = registrations;
    }

    @Override
    public void registerCustomSerializers(Kryo kryo) {
        userSerializer.registerSingleSerializer(kryo, clazz);
    }

    @Override
    void writeObject(Kryo kryo, Output output, T object) {
        kryo.writeObject(output, object);
    }

    @Override
    T readObject(Kryo kryo, Input input) {
        return kryo.readObject(input, clazz);
    }

    @Override
    public int newId() {
        return IdGeneratorUtils.newIdForType(getHazelcastInstance(), clazz);
    }
}
