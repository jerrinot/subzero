package info.jerrinot.subzero.it;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.serialization.HazelcastSerializationException;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.util.function.Consumer;
import info.jerrinot.subzero.Serializer;
import info.jerrinot.subzero.SubZero;
import info.jerrinot.subzero.SubzeroConfigRule;
import info.jerrinot.subzero.internal.ClassLoaderUtils;
import info.jerrinot.subzero.internal.PropertyUserSerializer;
import info.jerrinot.subzero.test.TestUtils;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hazelcast.config.InMemoryFormat.OBJECT;
import static info.jerrinot.subzero.SubZero.useAsGlobalSerializer;
import static info.jerrinot.subzero.SubZero.useForClasses;
import static info.jerrinot.subzero.SubzeroConfigRule.useConfig;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public abstract class BaseSmokeTests extends HazelcastTestSupport {
    private static final int CLUSTER_SIZE = 2;

    private TestHazelcastFactory factory = new TestHazelcastFactory();

    @Parameterized.Parameter(0)
    public boolean useClient;

    @Parameterized.Parameters(name = "useClient:{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {false},
                {true},
        });
    }

    @Rule
    public SubzeroConfigRule configRule = useConfig("subzero-serializers.properties");

    @After
    public void tearDown() {
        factory.shutdownAll();
    }

    @Test(expected = HazelcastSerializationException.class)
    public void testInfrastructure() {
        //make sure the test fails when Subzero is not enabled

        HazelcastInstance[] instances = createInstances(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                //intentionally empty
            }
        });
        executeTest(instances);
    }

    @Test
    public void testGlobalSerializer() {
        HazelcastInstance[] instances = createInstances(new Consumer<Object>() {
            @Override
            public void accept(Object configurationObject) {
                SerializationConfig serializationConfig = extractSerializationConfig(configurationObject);
                configureGlobalConfig(serializationConfig);
            }
        });
        executeTest(instances);
    }

    @Test
    public void testTypedSerializer() {
        HazelcastInstance[] instances = createInstances(new Consumer<Object>() {
            @Override
            public void accept(Object configurationObject) {
                SerializationConfig serializationConfig = extractSerializationConfig(configurationObject);
                configureTypedConfig(serializationConfig);
            }
        });
        executeTest(instances);
    }

    @Test
    public void testTypedSerializer_withInjector() {
        HazelcastInstance[] instances = createInstances(new Consumer<Object>() {
            @Override
            public void accept(Object configurationObject) {
                useForClasses(configurationObject, Person.class);
            }
        });
        executeTest(instances);
    }

    @Test
    public void testGlobalSerializer_withInjector() {
        HazelcastInstance[] instances = createInstances(new Consumer<Object>() {
            @Override
            public void accept(Object configurationObject) {
                useAsGlobalSerializer(configurationObject);
            }
        });

        executeTest(instances);
    }

    @Test
    public void testCompat() throws Exception {
        configRule.reconfigure("compatible-field-default-serializer.properties");

        final String classname = "some.pckage.SyntheticPerson";
        final String constantField = "firstname";
        String expectedFirstname = "somename";
        String mapName = randomMapName();

        final AtomicInteger instanceCounter = new AtomicInteger();
        HazelcastInstance[] instances = createInstances(new Consumer<Object>() {
            @Override
            public void accept(Object configurationObject) {
                int extraFieldCount = instanceCounter.incrementAndGet();
                int totalFieldCount = 1 + extraFieldCount;
                String fields[] = new String[totalFieldCount];
                fields[0] = constantField;
                for (int i = 1; i < fields.length; i++) {
                    fields[i] = "extrafield" + i;
                }
                ByteArrayClassLoader classLoader = TestUtils.createClass(classname, fields);

                if (configurationObject instanceof Config) {
                    Config config = (Config) configurationObject;
                    config.getMapConfig("default").setInMemoryFormat(OBJECT);
                    config.setClassLoader(classLoader);
                } else if (configurationObject instanceof ClientConfig) {
                    ((ClientConfig) configurationObject).setClassLoader(classLoader);
                }
                SubZero.useAsGlobalSerializer(configurationObject);
            }
        });

        HazelcastInstance i0 = instances[0];
        IMap<Integer, Object> map0 = i0.getMap(mapName);
        ClassLoader cl0 = ClassLoaderUtils.getConfiguredClassLoader(i0);
        Object o1 = cl0.loadClass(classname).newInstance();
        o1.getClass().getField(constantField).set(o1, expectedFirstname);
        map0.put(0, o1);

        HazelcastInstance i1 = instances[1];
        IMap<Integer, Object> map1 = i1.getMap(mapName);
        Object o = map1.get(0);

        String actualFirstname = (String) o.getClass().getField(constantField).get(o);
        assertEquals(expectedFirstname, actualFirstname);
    }

    private void executeTest(HazelcastInstance[] instances) {
        IMap<Integer, Person> map = instances[0].getMap("myMap");
        Person joe = new Person("Joe");
        map.put(0, joe);

        assertEquals(joe, map.get(0));
    }

    private static SerializationConfig extractSerializationConfig(Object configurationObject) {
        if (configurationObject instanceof Config) {
            return ((Config) configurationObject).getSerializationConfig();
        } else if (configurationObject instanceof ClientConfig) {
            return ((ClientConfig) configurationObject).getSerializationConfig();
        } else {
            throw new AssertionError("unknown configuration object " + configurationObject);
        }
    }

    private HazelcastInstance[] createInstances(Consumer<Object> configurationConsumer) {
        int totalInstanceCount = useClient ? CLUSTER_SIZE + 1 : CLUSTER_SIZE;
        HazelcastInstance[] instances = new HazelcastInstance[totalInstanceCount];

        for (int i = useClient ? 1 : 0; i < totalInstanceCount; i++) {
            Config config = new Config();
            configurationConsumer.accept(config);
            instances[i] = factory.newHazelcastInstance(config);
        }
        if (useClient) {
            ClientConfig config = new ClientConfig();
            configurationConsumer.accept(config);
            instances[0] = factory.newHazelcastClient(config);
        }
        return instances;
    }

    private void configureTypedConfig(SerializationConfig config) {
        SerializerConfig serializerConfig = new SerializerConfig()
                .setTypeClassName("info.jerrinot.subzero.it.Person")
                .setClassName("info.jerrinot.subzero.Serializer");

        config.addSerializerConfig(serializerConfig);
    }

    private void configureGlobalConfig(SerializationConfig config) {
        GlobalSerializerConfig globalSerializerConfig = new GlobalSerializerConfig()
                .setClassName(Serializer.class.getName())
                .setOverrideJavaSerialization(true);
        config.setGlobalSerializerConfig(globalSerializerConfig);
    }

}
