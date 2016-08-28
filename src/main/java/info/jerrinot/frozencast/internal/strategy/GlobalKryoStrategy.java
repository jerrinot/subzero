package info.jerrinot.frozencast.internal.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hazelcast.core.HazelcastInstance;
import info.jerrinot.frozencast.internal.IdGeneratorUtils;

public final class GlobalKryoStrategy<T> extends KryoStrategy<T> {


    @Override
    void writeObject(Kryo kryo, Output output, T object) {
        kryo.writeClassAndObject(output, object);
    }

    @Override
    T readObject(Kryo kryo, Input input) {
        return (T) kryo.readClassAndObject(input);
    }

    @Override
    public int newId(HazelcastInstance hazelcastInstance) {
        return IdGeneratorUtils.newId(hazelcastInstance);
    }
}
