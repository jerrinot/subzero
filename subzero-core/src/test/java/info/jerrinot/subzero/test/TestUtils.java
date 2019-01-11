package info.jerrinot.subzero.test;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.serialization.impl.ObjectDataInputStream;
import com.hazelcast.internal.serialization.impl.ObjectDataOutputStream;
import com.hazelcast.nio.serialization.StreamSerializer;
import info.jerrinot.subzero.SerializerTest;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TestUtils {

    public static <T> T serializeAndDeserializeObject(StreamSerializer<T> serializer, T input) throws IOException {
        byte[] blob = serialize(serializer, input);
        return deserialize(serializer, blob);
    }

    public static <T> byte[] serialize(StreamSerializer<T> serializer, T input) throws IOException{
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InternalSerializationService mockSerializationService = mock(InternalSerializationService.class, withSettings().stubOnly());
        ObjectDataOutputStream odos = new ObjectDataOutputStream(os, mockSerializationService);
        serializer.write(odos, input);
        return os.toByteArray();
    }

    public static <T> T deserialize(StreamSerializer<T> serializer, byte[] blob) throws IOException {
        InternalSerializationService mockSerializationService = mock(InternalSerializationService.class, withSettings().stubOnly());
        ObjectDataInputStream odis = new ObjectDataInputStream(new ByteArrayInputStream(blob), mockSerializationService);
        return serializer.read(odis);
    }

    public static HazelcastInstance newMockHazelcastInstance() {
        return newMockHazelcastInstance(null);
    }

    public static HazelcastInstance newMockHazelcastInstance(ClassLoader classLoader) {
        HazelcastInstance hz = mock(HazelcastInstance.class);
        Config config = new Config();
        config.setClassLoader(classLoader);
        when(hz.getConfig()).thenReturn(config);
        return hz;
    }

    public static ByteArrayClassLoader createClass(String classname, String...fields) {
        DynamicType.Builder<Object> byteBuddy = new ByteBuddy(ClassFileVersion.JAVA_V6)
                .subclass(Object.class)
                .name(classname)
                .modifiers(Visibility.PUBLIC);
        for (String field : fields) {
            byteBuddy = byteBuddy.defineField(field, String.class, Visibility.PUBLIC);
        }
        byte[] bytes = byteBuddy.make().getBytes();
        Map<String, byte[]> typeDefinitions = new HashMap<String, byte[]>();
        typeDefinitions.put(classname, bytes);
        return new ByteArrayClassLoader(SerializerTest.class.getClassLoader(), typeDefinitions);
    }
}
