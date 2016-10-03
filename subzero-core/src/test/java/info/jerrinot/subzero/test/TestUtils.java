package info.jerrinot.subzero.test;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.serialization.impl.ObjectDataInputStream;
import com.hazelcast.internal.serialization.impl.ObjectDataOutputStream;
import info.jerrinot.subzero.AbstractSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class TestUtils {

    private static final InternalSerializationService mockSerializationService;

    static {
        mockSerializationService = mock(InternalSerializationService.class, withSettings().stubOnly());
    }

    public static <T> T serializeAndDeserializeObject(AbstractSerializer<T> serializer, T input) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectDataOutputStream odos = new ObjectDataOutputStream(os, mockSerializationService);
        serializer.write(odos, input);
        ObjectDataInputStream odis = new ObjectDataInputStream(new ByteArrayInputStream(os.toByteArray()),
                mockSerializationService);
        return serializer.read(odis);
    }

    public static HazelcastInstance newMockHazelcastInstance() {
        HazelcastInstance hz = mock(HazelcastInstance.class);
        when(hz.getConfig()).thenReturn(new Config());
        return hz;
    }
}
