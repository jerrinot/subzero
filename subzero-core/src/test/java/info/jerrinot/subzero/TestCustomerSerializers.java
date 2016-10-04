package info.jerrinot.subzero;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.test.HazelcastTestSupport;
import info.jerrinot.subzero.test.AnotherNonSerializableObject;
import info.jerrinot.subzero.test.NonSerializableObjectRegisteredInDefaultConfigFile;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TestCustomerSerializers extends HazelcastTestSupport {
    private TestHazelcastFactory hazelcastFactory;

    @Before
    public void setUp() {
        hazelcastFactory = new TestHazelcastFactory();
    }

    @Test
    public void testGlobalCustomSerializerRegisteredInDefaultConfigFile() throws Exception {
        String mapName = randomMapName();
        Config config = new Config();
        SubZero.useAsGlobalSerializer(config);
        HazelcastInstance member = hazelcastFactory.newHazelcastInstance(config);
        IMap<Integer, NonSerializableObjectRegisteredInDefaultConfigFile> myMap = member.getMap(mapName);

        myMap.put(0, new NonSerializableObjectRegisteredInDefaultConfigFile());
        NonSerializableObjectRegisteredInDefaultConfigFile fromCache = myMap.get(0);

        assertEquals("deserialized", fromCache.name);
    }

    @Test
    public void testGlobalCustomSerializationConfiguredProgrammatically() {
        String mapName = randomMapName();
        Config config = new Config();

        SubZero.useAsGlobalSerializer(config, MyGlobalUserSerlizationConfig.class);

        HazelcastInstance member = hazelcastFactory.newHazelcastInstance(config);
        IMap<Integer, AnotherNonSerializableObject> myMap = member.getMap(mapName);
        myMap.put(0, new AnotherNonSerializableObject());
        AnotherNonSerializableObject fromCache = myMap.get(0);

        assertEquals("deserialized", fromCache.name);
    }

    @Test
    public void testTypedCustomSerializerRegisteredInDefaultConfigFile() throws Exception {
        String mapName = randomMapName();
        Config config = new Config();
        SubZero.useForClasses(config, NonSerializableObjectRegisteredInDefaultConfigFile.class);
        HazelcastInstance member = hazelcastFactory.newHazelcastInstance(config);
        IMap<Integer, NonSerializableObjectRegisteredInDefaultConfigFile> myMap = member.getMap(mapName);

        myMap.put(0, new NonSerializableObjectRegisteredInDefaultConfigFile());
        NonSerializableObjectRegisteredInDefaultConfigFile fromCache = myMap.get(0);

        assertEquals("deserialized", fromCache.name);
    }

    @Test
    public void testTypedCustomSerializer_configuredBySubclassing() throws Exception {
        String mapName = randomMapName();

        Config config = new Config();
        SerializerConfig serializerConfig = new SerializerConfig();
        serializerConfig.setClass(MySerializer.class);
        serializerConfig.setTypeClass(AnotherNonSerializableObject.class);
        config.getSerializationConfig().getSerializerConfigs().add(serializerConfig);

        HazelcastInstance member = hazelcastFactory.newHazelcastInstance(config);
        IMap<Integer, AnotherNonSerializableObject> myMap = member.getMap(mapName);

        myMap.put(0, new AnotherNonSerializableObject());
        AnotherNonSerializableObject fromCache = myMap.get(0);

        assertEquals("deserialized", fromCache.name);
    }

    public static class MyGlobalUserSerlizationConfig extends AbstractGlobalUserSerializer {
        public MyGlobalUserSerlizationConfig() {
            super(UserSerializerConfig.register(AnotherNonSerializableObject.class, new AnotherNonSerializableObjectKryoSerializer()));
        }
    }

    public static class MySerializer extends AbstractTypeSpecificUserSerializer<AnotherNonSerializableObject> {
        public MySerializer() {
            super(AnotherNonSerializableObject.class, new AnotherNonSerializableObjectKryoSerializer());
        }
    }

    public static class AnotherNonSerializableObjectKryoSerializer extends com.esotericsoftware.kryo.Serializer<AnotherNonSerializableObject> {

        @Override
        public void write(Kryo kryo, Output output, AnotherNonSerializableObject object) {

        }

        @Override
        public AnotherNonSerializableObject read(Kryo kryo, Input input, Class<AnotherNonSerializableObject> type) {
            AnotherNonSerializableObject object = new AnotherNonSerializableObject();
            object.name = "deserialized";
            return object;
        }
    }

}
