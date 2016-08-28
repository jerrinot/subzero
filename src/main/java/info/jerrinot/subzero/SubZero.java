package info.jerrinot.subzero;

import com.hazelcast.config.Config;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;

public final class SubZero {

    private SubZero() {

    }

    public static Config subZeroAsDefaultSerializer(Config config) {
        SerializationConfig serializationConfig = config.getSerializationConfig();
        GlobalSerializerConfig globalSerializerConfig = serializationConfig.getGlobalSerializerConfig();
        if (globalSerializerConfig == null) {
            globalSerializerConfig = new GlobalSerializerConfig();
            serializationConfig.setGlobalSerializerConfig(globalSerializerConfig);
        }
        globalSerializerConfig.setClassName(Serializer.class.getName()).setOverrideJavaSerialization(true);
        return config;
    }

    public static Config subZeroForClasses(Config config, Class<?>...classes) {
        SerializationConfig serializationConfig = config.getSerializationConfig();
        for (Class<?> clazz : classes) {
            SerializerConfig serializerConfig = new SerializerConfig();
            Serializer<?> serializer = new Serializer(clazz);
            serializerConfig.setImplementation(serializer);
            serializerConfig.setTypeClass(clazz);
            serializationConfig.addSerializerConfig(serializerConfig);
        }
        return config;
    }
}
