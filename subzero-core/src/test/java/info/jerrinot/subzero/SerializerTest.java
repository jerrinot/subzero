package info.jerrinot.subzero;

import com.hazelcast.core.HazelcastInstance;
import info.jerrinot.subzero.test.TestUtils;
import org.junit.Test;

import java.io.IOException;

import static info.jerrinot.subzero.test.TestUtils.newMockHazelcastInstance;
import static org.junit.Assert.assertEquals;

public class SerializerTest {

    @Test(expected = AssertionError.class)
    public void givenTwoTypeSpecificSerializersWithTheSameTypeExists_whenTheSameHazelcastInstanceIsInjected_thenTheyThrowError() {
        Serializer serializer1 = new Serializer(String.class);
        serializer1.setHazelcastInstance(newMockHazelcastInstance());

        Serializer serializer2 = new Serializer(String.class);
        serializer2.setHazelcastInstance(newMockHazelcastInstance());

        HazelcastInstance hz = newMockHazelcastInstance();
        serializer1.setHazelcastInstance(hz);
        serializer2.setHazelcastInstance(hz);
    }

    @Test
    public void givenTwoTypeSpecificSerializersWithTheSameTypeExists_whenDifferentHazelcastInstancesAreInjected_thenTheyHaveTheSameTypeId() {
        Serializer serializer1 = new Serializer(String.class);
        serializer1.setHazelcastInstance(newMockHazelcastInstance());

        Serializer serializer2 = new Serializer(String.class);
        serializer2.setHazelcastInstance(newMockHazelcastInstance());

        HazelcastInstance hz1 = newMockHazelcastInstance();
        HazelcastInstance hz2 = newMockHazelcastInstance();
        serializer1.setHazelcastInstance(hz1);
        serializer2.setHazelcastInstance(hz2);

        int typeId1 = serializer1.getTypeId();
        int typeId2 = serializer2.getTypeId();
        assertEquals(typeId1, typeId2);
    }

    @Test
    public void givenTypeSpecificSerializerExist_whenObjectIsSerializedAndDeserialized_thenItHasTheSameInternalState() throws IOException {
        String input = "foo";
        Serializer<String> serializer = new Serializer(String.class);
        serializer.setHazelcastInstance(newMockHazelcastInstance());

        String output = TestUtils.serializeAndDeserializeObject(serializer, input);

        assertEquals(input, output);
    }

}
