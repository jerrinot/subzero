package info.jerrinot.subzero.internal;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.SerializerFactory;
import info.jerrinot.subzero.SerializerConfigurer;

public final class PostProcessingSerializerFactory implements SerializerFactory {
    private final SerializerFactory delegate;
    private final SerializerConfigurer configurer;

    public PostProcessingSerializerFactory(SerializerFactory delegate, SerializerConfigurer configurer) {
        this.delegate = delegate;
        this.configurer = configurer;
    }

    @Override
    public Serializer newSerializer(Kryo kryo, Class type) {
        Serializer serializer = delegate.newSerializer(kryo, type);
        configurer.configure(type, serializer);
        return serializer;
    }

    @Override
    public boolean isSupported(Class type) {
        return delegate.isSupported(type);
    }
}
