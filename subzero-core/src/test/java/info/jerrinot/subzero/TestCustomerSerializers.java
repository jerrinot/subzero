package info.jerrinot.subzero;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hazelcast.client.config.ClientConfig;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public void testGlobalCustomSerializationConfiguredProgrammaticallyForHzConfig() {
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
    public void testGlobalCustomSerializationConfiguredProgrammaticallyForClientConfig() {
        Config memberConfig = new Config();
        SubZero.useAsGlobalSerializer(memberConfig);
        hazelcastFactory.newHazelcastInstance(memberConfig);

        String mapName = randomMapName();
        ClientConfig config = new ClientConfig();

        SubZero.useAsGlobalSerializer(config, MyGlobalUserSerlizationConfig.class);

        HazelcastInstance member = hazelcastFactory.newHazelcastClient(config);
        IMap<Integer, AnotherNonSerializableObject> myMap = member.getMap(mapName);
        myMap.put(0, new AnotherNonSerializableObject());
        AnotherNonSerializableObject fromCache = myMap.get(0);

        assertEquals("deserialized", fromCache.name);
    }

    @Test
    public void testGlobalCustomDelegateSerializationConfiguredProgrammaticallyForHzConfig() {
        String mapName = randomMapName();
        Config config = new Config();

        SubZero.useAsGlobalSerializer(config, MyGlobalDelegateSerlizationConfig.class);

        HazelcastInstance member = hazelcastFactory.newHazelcastInstance(config);
        IMap<Integer, AnotherNonSerializableObject> myMap = member.getMap(mapName);
        myMap.put(0, new AnotherNonSerializableObject());
        AnotherNonSerializableObject fromCache = myMap.get(0);

        assertEquals("deserialized", fromCache.name);
    }

    @Test
    public void testGlobalCustomDelegateSerializationConfiguredProgrammaticallyForClientConfig() {
        Config memberConfig = new Config();
        SubZero.useAsGlobalSerializer(memberConfig);
        hazelcastFactory.newHazelcastInstance(memberConfig);

        String mapName = randomMapName();
        ClientConfig config = new ClientConfig();

        SubZero.useAsGlobalSerializer(config, MyGlobalDelegateSerlizationConfig.class);

        HazelcastInstance member = hazelcastFactory.newHazelcastClient(config);
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
    public void testGlobalCustomSerializer_SpecialRegistrationRegisteredInDefaultConfigFile() {
        String mapName = randomMapName();
        Config config = new Config();

        SubZero.useAsGlobalSerializer(config);
        HazelcastInstance member = hazelcastFactory.newHazelcastInstance(config);
        IMap<Integer, ClassWithUnmodifieableList> myMap = member.getMap(mapName);
        myMap.put(0, new ClassWithUnmodifieableList("foo"));

        //does not throw an exception
        myMap.get(0);
    }

    @Test
    public void testTypedSerializer_SpecialRegistrationRegisteredInDefaultConfigFile() {
        String mapName = randomMapName();
        Config config = new Config();

        SubZero.useForClasses(config, ClassWithUnmodifieableList.class);
        HazelcastInstance member = hazelcastFactory.newHazelcastInstance(config);
        IMap<Integer, ClassWithUnmodifieableList> myMap = member.getMap(mapName);
        myMap.put(0, new ClassWithUnmodifieableList("foo"));

        //does not throw an exception
        myMap.get(0);
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

    public static class MyGlobalDelegateSerlizationConfig extends AbstractGlobalUserSerializer {
        public MyGlobalDelegateSerlizationConfig() {
            super(UserSerializerConfig.delegate(new KryoConfigurer() {
                @Override
                public void configure(Kryo kryo) {
                    kryo.register(AnotherNonSerializableObject.class, new AnotherNonSerializableObjectKryoSerializer());
                }
            }));
        }
    }

    public static final class ClassWithUnmodifieableList {
        private List<String> names;

        public ClassWithUnmodifieableList(String...names) {
            this.names = Collections.unmodifiableList(Arrays.asList(names));
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
