package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hazelcast.core.HazelcastInstance;
import info.jerrinot.subzero.internal.IdGeneratorUtils;

public class TypedKryoStrategy<T> extends KryoStrategy<T> {

    private final Class<T> clazz;

    public TypedKryoStrategy(Class<T> clazz) {
        this.clazz = clazz;
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
    public int newId(HazelcastInstance hazelcastInstance) {
        return IdGeneratorUtils.newIdForType(hazelcastInstance, clazz);
    }
}
