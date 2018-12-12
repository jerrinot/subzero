package info.jerrinot.subzero.test;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.serialization.impl.ObjectDataInputStream;
import com.hazelcast.internal.serialization.impl.ObjectDataOutputStream;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class TestUtils {

    private static final InternalSerializationService mockSerializationService;

    static {
        mockSerializationService = mock(InternalSerializationService.class, withSettings().stubOnly());
    }

    public static <T> T serializeAndDeserializeObject(StreamSerializer<T> serializer, T input) throws IOException {
        byte[] blob = serialize(serializer, input);
        return deserialize(serializer, blob);
    }

    public static <T> byte[] serialize(StreamSerializer<T> serializer, T input) throws IOException{
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectDataOutputStream odos = new ObjectDataOutputStream(os, mockSerializationService);
        serializer.write(odos, input);
        return os.toByteArray();
    }

    public static <T> T deserialize(StreamSerializer<T> serializer, byte[] blob) throws IOException {
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
}
