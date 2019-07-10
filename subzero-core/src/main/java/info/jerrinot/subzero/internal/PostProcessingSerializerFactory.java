package info.jerrinot.subzero.internal;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.factories.SerializerFactory;
import info.jerrinot.subzero.SerializerConfigurer;

public final class PostProcessingSerializerFactory implements SerializerFactory {
    private final SerializerFactory delegate;
    private final SerializerConfigurer configurer;

    public PostProcessingSerializerFactory(SerializerFactory delegate, SerializerConfigurer configurer) {
        this.delegate = delegate;
        this.configurer = configurer;
    }

    @Override
    public Serializer makeSerializer(Kryo kryo, Class<?> type) {
        Serializer serializer = delegate.makeSerializer(kryo, type);
        configurer.configure(type, serializer);
        return serializer;
    }
}
