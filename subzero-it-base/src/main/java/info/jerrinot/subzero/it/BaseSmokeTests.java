package info.jerrinot.subzero.it;

import com.hazelcast.config.Config;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import info.jerrinot.subzero.Serializer;
import org.junit.Before;
import org.junit.Test;

import static info.jerrinot.subzero.SubZero.useAsGlobalSerializer;
import static info.jerrinot.subzero.SubZero.useForClasses;
import static org.junit.Assert.assertEquals;

public abstract class BaseSmokeTests extends HazelcastTestSupport {
    private static final int CLUSTER_SIZE = 2;

    private TestHazelcastInstanceFactory factory;

    @Before
    public void setUp() {
        factory = createHazelcastInstanceFactory(CLUSTER_SIZE);
    }

    @Test
    public void testGlobalSerializer() {
        HazelcastInstance[] instances = factory.newInstances(createGlobalConfig());
        IMap<Integer, Person> map = instances[0].getMap("myMap");

        Person joe = new Person("Joe");
        map.put(0, joe);

        assertEquals(joe, map.get(0));
    }

    @Test
    public void testTypedSerializer() {
        HazelcastInstance[] instances = factory.newInstances(createTypedConfig());
        IMap<Integer, Person> map = instances[0].getMap("myMap");

        Person joe = new Person("Joe");
        map.put(0, joe);

        assertEquals(joe, map.get(0));
    }

    @Test
    public void testTypedSerializer_withInjector() {
        Config config = useForClasses(new Config(), Person.class);
        HazelcastInstance[] instances = factory.newInstances(config);
        IMap<Integer, Person> map = instances[0].getMap("myMap");

        Person joe = new Person("Joe");
        map.put(0, joe);

        assertEquals(joe, map.get(0));
    }

    @Test
    public void testGlobalSerializer_withInjector() {
        Config config = useAsGlobalSerializer(new Config());
        HazelcastInstance[] instances = factory.newInstances(config);
        IMap<Integer, Person> map = instances[0].getMap("myMap");

        Person joe = new Person("Joe");
        map.put(0, joe);

        assertEquals(joe, map.get(0));
    }

    private Config createTypedConfig() {
        Config config = new Config();

        SerializerConfig serializerConfig = new SerializerConfig()
                .setTypeClassName("info.jerrinot.subzero.it.Person")
                .setClassName("info.jerrinot.subzero.Serializer");

        SerializationConfig serializationConfig = config.getSerializationConfig();
        serializationConfig.addSerializerConfig(serializerConfig);

        return config;
    }

    private Config createGlobalConfig() {
        Config config = new Config();

        GlobalSerializerConfig globalSerializerConfig = new GlobalSerializerConfig()
                .setClassName(Serializer.class.getName())
                .setOverrideJavaSerialization(true);
        config.getSerializationConfig().setGlobalSerializerConfig(globalSerializerConfig);
        return config;
    }

}
