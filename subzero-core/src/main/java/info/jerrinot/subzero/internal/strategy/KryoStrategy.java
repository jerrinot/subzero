package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.ClassResolver;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ReferenceResolver;
import com.esotericsoftware.kryo.StreamFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.InputChunked;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.OutputChunked;
import com.esotericsoftware.kryo.util.DefaultStreamFactory;
import com.hazelcast.core.HazelcastInstance;
import info.jerrinot.subzero.internal.ClassLoaderUtils;
import info.jerrinot.subzero.internal.IdGeneratorUtils;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.InputStream;
import java.io.OutputStream;

import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.getInteger;
import static java.lang.String.format;
import static java.lang.System.getProperty;

public abstract class KryoStrategy<T> {

    private static final int BUFFER_SIZE = getInteger("subzero.buffer.size.kb", 16) * 1024;
    private static final boolean IGNORE_HAZELCAST_CLASSLOADER = getBoolean("subzero.classloading.ignore");

    private static final String DEFAULT_REFERENCE_RESOLVER_CLASS = "com.esotericsoftware.kryo.util.MapReferenceResolver";
    private static final String REFERENCE_RESOLVER_CLASS_SYSTEM_PROPERTY = "subzero.referenceresolver.class";

    private HazelcastInstance hazelcastInstance;

    private final ThreadLocal<KryoContext> KRYOS = new ThreadLocal<KryoContext>() {
        protected KryoContext initialValue() {
            Kryo kryo = newKryoInstance();
            OutputChunked output = new OutputChunked(BUFFER_SIZE);
            InputChunked input = new InputChunked(BUFFER_SIZE);
            return new KryoContext(kryo, input, output);
        }
    };

    private Kryo newKryoInstance() {
        Kryo kryo;
        if (IGNORE_HAZELCAST_CLASSLOADER) {
            kryo = new Kryo();
        } else {
            ClassLoader classLoader = ClassLoaderUtils.getConfiguredClassLoader(hazelcastInstance);
            ClassResolver classResolver = new DelegatingClassResolver(classLoader);
            ReferenceResolver referenceResolver = createReferenceResolver();
            StreamFactory defaultStreamFactory = new DefaultStreamFactory();
            kryo = new Kryo(classResolver, referenceResolver, defaultStreamFactory);
        }
        registerCustomSerializers(kryo);
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
    }

    HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public abstract void registerCustomSerializers(Kryo kryo);

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void write(OutputStream out, T object) {
        KryoContext kryoContext = KRYOS.get();
        OutputChunked output = kryoContext.getOutputChunked();
        output.setOutputStream(out);
        writeObject(kryoContext.getKryo(), output, object);
        output.endChunks();
        output.flush();
    }

    abstract void writeObject(Kryo kryo, Output output, T object);

    public T read(InputStream in) {
        KryoContext kryoContext = KRYOS.get();
        InputChunked input = kryoContext.getInputChunked();
        input.setInputStream(in);
        return readObject(kryoContext.getKryo(), input);
    }

    abstract T readObject(Kryo kryo, Input input);

    public void destroy(HazelcastInstance hazelcastInstance) {
        IdGeneratorUtils.instanceDestroyed(hazelcastInstance);
    }

    public abstract int newId();

    private static ReferenceResolver createReferenceResolver() {
        String referenceResolverClass = getProperty(REFERENCE_RESOLVER_CLASS_SYSTEM_PROPERTY, DEFAULT_REFERENCE_RESOLVER_CLASS);
        try {
            Class<?> resolverClass = Class.forName(referenceResolverClass);
            return (ReferenceResolver) resolverClass.getConstructor().newInstance();

        } catch (Exception e) {
            throw new IllegalArgumentException(format("could not create ReferenceResolver with class %s", referenceResolverClass), e);
        }
    }
}
