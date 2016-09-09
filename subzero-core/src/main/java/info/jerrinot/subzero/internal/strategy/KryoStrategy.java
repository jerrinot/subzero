package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.InputChunked;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.OutputChunked;
import com.esotericsoftware.kryo.util.DefaultStreamFactory;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import com.hazelcast.core.HazelcastInstance;
import info.jerrinot.subzero.internal.ClassLoaderUtils;
import info.jerrinot.subzero.internal.IdGeneratorUtils;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.lang.Integer.getInteger;

public abstract class KryoStrategy<T> {

    private static final int BUFFER_SIZE = getInteger("subzero.buffer.size.kb", 16) * 1024;
    private HazelcastInstance hazelcastInstance;

    private final ThreadLocal<KryoContext> KRYOS = new ThreadLocal<KryoContext>() {
        protected KryoContext initialValue() {
            ClassLoader classLoader = ClassLoaderUtils.getConfiguredClassLoader(hazelcastInstance);
            DelegatingClassResolver classResolver = new DelegatingClassResolver(classLoader);
            MapReferenceResolver mapReferenceResolver = new MapReferenceResolver();
            DefaultStreamFactory defaultStreamFactory = new DefaultStreamFactory();
            Kryo kryo = new Kryo(classResolver, mapReferenceResolver, defaultStreamFactory);
            kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            OutputChunked output = new OutputChunked(BUFFER_SIZE);
            InputChunked input = new InputChunked(BUFFER_SIZE);
            return new KryoContext(kryo, input, output);
        }
    };

    HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void write(OutputStream out, T object) throws IOException {
        KryoContext kryoContext = KRYOS.get();
        OutputChunked output = kryoContext.getOutputChunked();
        output.setOutputStream(out);
        writeObject(kryoContext.getKryo(), output, object);
        output.endChunks();
        output.flush();
    }

    abstract void writeObject(Kryo kryo, Output output, T object);

    public T read(InputStream in) throws IOException {
        KryoContext kryoContext = KRYOS.get();
        InputChunked input = kryoContext.getInputChunked();
        input.setInputStream(in);
        T object = readObject(kryoContext.getKryo(), input);
        return object;
    }

    abstract T readObject(Kryo kryo, Input input);

    public void destroy(HazelcastInstance hazelcastInstance) {
        IdGeneratorUtils.instanceDestroyed(hazelcastInstance);
    }

    public abstract int newId();
}
