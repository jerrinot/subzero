package info.jerrinot.subzero;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

import java.util.LinkedHashMap;
import java.util.Map;

public final class UserSerializerConfig {
    public static UserSerializationBuilder register(Class clazz, Serializer serializer) {
        return new UserSerializationBuilder(clazz, serializer);
    }

    public static UserSerializationBuilder delegate(KryoConfigurer kryoConfigurer) {
        return new UserSerializationBuilder(kryoConfigurer);
    }

    public final static class UserSerializationBuilder implements UserSerializer {
        private Map<Class, com.esotericsoftware.kryo.Serializer> maps = new LinkedHashMap<Class, Serializer>();
        private KryoConfigurer kryoConfigurer = null;

        private UserSerializationBuilder(Class clazz, com.esotericsoftware.kryo.Serializer serializer) {
            maps.put(clazz, serializer);
        }

        private UserSerializationBuilder(KryoConfigurer kryoConfigurer) {
            this.kryoConfigurer = kryoConfigurer;
        }

        public UserSerializationBuilder register(Class clazz, Serializer serializer) {
            com.esotericsoftware.kryo.Serializer previousSerializer = maps.put(clazz, serializer);
            if (previousSerializer != null) {
                throw new IllegalArgumentException("There is already " + previousSerializer
                        + " configured for class " + clazz);
            }
            return this;
        }

        @Override
        public void registerSingleSerializer(Kryo kryo, Class clazz) {
            com.esotericsoftware.kryo.Serializer serializer = maps.get(clazz);
            if (serializer == null) {
                throw new IllegalArgumentException("There is no custom Kryo Serializer configured for " + clazz);
            }
            kryo.register(clazz, serializer);
        }

        @Override
        public void registerAllSerializers(Kryo kryo) {
            for (Map.Entry<Class, com.esotericsoftware.kryo.Serializer> entry : maps.entrySet()) {
                Class clazz = entry.getKey();
                com.esotericsoftware.kryo.Serializer serializer = entry.getValue();
                kryo.register(clazz, serializer);
            }
            if (kryoConfigurer != null)
                kryoConfigurer.configure(kryo);
        }
    }
}
