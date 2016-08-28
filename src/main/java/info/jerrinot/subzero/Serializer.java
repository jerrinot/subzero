package info.jerrinot.subzero;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import info.jerrinot.subzero.internal.strategy.GlobalKryoStrategy;
import info.jerrinot.subzero.internal.strategy.KryoStrategy;
import info.jerrinot.subzero.internal.strategy.TypedKryoStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Serializer<T> implements StreamSerializer<T>, HazelcastInstanceAware {
    private int id;
    private HazelcastInstance hazelcastInstance;
    private KryoStrategy<T> strategy;

    public Serializer() {
        Class<T> classType = getClassType();
        this.strategy = classType == null ? new GlobalKryoStrategy<T>() : new TypedKryoStrategy<T>(classType);
    }

    public Serializer(Class<T> clazz) {
        this.strategy = new TypedKryoStrategy<T>(clazz);
    }

    public void write(ObjectDataOutput out, T object) throws IOException {
        strategy.write((OutputStream) out, object);
    }

    public T read(ObjectDataInput in) throws IOException {
        return strategy.read((InputStream) in);
    }

    public int getTypeId() {
        return id;
    }

    public void destroy() {
        strategy.destroy(hazelcastInstance);
    }

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.id = getClassTypeIdInternal(hazelcastInstance);
    }


    protected int getClassTypeIdInternal(HazelcastInstance hazelcastInstance) {
        return strategy.newId(hazelcastInstance);
    }

    protected Class<T> getClassType() {
        return null;
    }
}
