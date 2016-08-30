package info.jerrinot.subzero;

import com.hazelcast.config.Config;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;

/**
 * Convenient class for dead-simple SubZero injection into Hazelcast configuration.
 *
 * This is the simplest way to use SubZero when you use Hazelcast programmatic configuration.
 *
 *
 */
public final class SubZero {

    private SubZero() {

    }

    /**
     * Use SubZero as a global serializer.
     *
     * This method configures Hazelcast to delegate a class serialization to SubZero when the class
     * has no explicit strategy configured.
     *
     * @param config Hazelcast configuration to inject SubZero into
     * @return Hazelcast configuration.
     */
    public static Config useAsGlobalSerializer(Config config) {
        SerializationConfig serializationConfig = config.getSerializationConfig();
        GlobalSerializerConfig globalSerializerConfig = serializationConfig.getGlobalSerializerConfig();
        if (globalSerializerConfig == null) {
            globalSerializerConfig = new GlobalSerializerConfig();
            serializationConfig.setGlobalSerializerConfig(globalSerializerConfig);
        }
        globalSerializerConfig.setClassName(Serializer.class.getName()).setOverrideJavaSerialization(true);
        return config;
    }

    /**
     * Use SubZero as a serializer for selected classes only.
     *
     * @param config Hazelcast configuration to inject SubZero into
     * @param classes classes Hazelcast should serialize via SubZero
     * @return Hazelcast configuration
     */
    public static Config useForClasses(Config config, Class<?>...classes) {
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
