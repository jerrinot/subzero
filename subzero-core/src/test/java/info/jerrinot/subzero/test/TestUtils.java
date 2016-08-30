package info.jerrinot.subzero.test;

import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.serialization.impl.ObjectDataInputStream;
import com.hazelcast.internal.serialization.impl.ObjectDataOutputStream;
import info.jerrinot.subzero.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

public class TestUtils {

    private static final InternalSerializationService mockSerializationService;

    static {
        mockSerializationService = mock(InternalSerializationService.class, withSettings().stubOnly());
    }

    public static <T> T serializeAndDeserializeObject(Serializer<T> serializer, T input) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectDataOutputStream odos = new ObjectDataOutputStream(os, mockSerializationService);
        serializer.write(odos, input);
        ObjectDataInputStream odis = new ObjectDataInputStream(new ByteArrayInputStream(os.toByteArray()),
                mockSerializationService);
        return serializer.read(odis);
    }
}
