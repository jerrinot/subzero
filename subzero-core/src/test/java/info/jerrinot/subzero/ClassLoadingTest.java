package info.jerrinot.subzero;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;

import static com.hazelcast.config.InMemoryFormat.OBJECT;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ClassLoadingTest extends HazelcastTestSupport {

    private TestHazelcastFactory hazelcastFactory;

    @Before
    public void setUp() {
        hazelcastFactory = new TestHazelcastFactory();
    }

    @Test
    public void givenMemberHasClassLoaderConfigured_whenObjectIsStored_thenClassLoaderWillBeUsed() throws Exception {
        String mapName = randomMapName();
        Config config = new Config();
        SubZero.useAsGlobalSerializer(config);
        ClassLoader spyingClassLoader = createSpyingClassLoader();
        config.setClassLoader(spyingClassLoader);
        config.addMapConfig(new MapConfig(mapName).setInMemoryFormat(OBJECT));
        HazelcastInstance member = hazelcastFactory.newHazelcastInstance(config);
        IMap<Integer, Object> myMap = member.getMap(mapName);

        myMap.put(0, new MyClass());

        verify(spyingClassLoader).loadClass("info.jerrinot.subzero.ClassLoadingTest$MyClass");
    }

    @Test
    public void givenClientHasClassLoaderConfigured_whenObjectIsFetched_thenClassLoaderWillBeUsed() throws Exception {
        Config memberConfig = new Config();
        SubZero.useAsGlobalSerializer(memberConfig);
        hazelcastFactory.newHazelcastInstance(memberConfig);

        ClientConfig clientConfig = new ClientConfig();
        ClassLoader clientClassLoader = createSpyingClassLoader();
        clientConfig.setClassLoader(clientClassLoader);
        SubZero.useAsGlobalSerializer(clientConfig);
        HazelcastInstance client = hazelcastFactory.newHazelcastClient(clientConfig);
        IMap<Integer, Object> myMap = client.getMap(randomMapName());
        myMap.put(0, new MyClass());

        myMap.get(0);

        verify(clientClassLoader).loadClass("info.jerrinot.subzero.ClassLoadingTest$MyClass");
    }


    private ClassLoader createSpyingClassLoader() {
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
        return spy(urlClassLoader);
    }


    public static class MyClass implements Serializable {

    }

}
