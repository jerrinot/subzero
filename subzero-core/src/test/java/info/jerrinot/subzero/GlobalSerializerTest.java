package info.jerrinot.subzero;

import com.hazelcast.core.HazelcastInstance;
import info.jerrinot.subzero.test.TestUtils;
import org.junit.Test;

import java.io.IOException;

import static info.jerrinot.subzero.test.TestUtils.newMockHazelcastInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class GlobalSerializerTest {

    @Test
    public void givenSingleSerializerExist_whenHazelcastInstanceIsInjected_thenTypeIdIsGreaterThenZero() {
        AbstractSerializer serializer = new GlobalSerializer();
        serializer.setHazelcastInstance(newMockHazelcastInstance());

        HazelcastInstance hz = newMockHazelcastInstance();
        serializer.setHazelcastInstance(hz);

        int typeId = serializer.getTypeId();
        assertTrue(typeId > 0);
    }

    @Test
    public void givenTwoSerializerExist_whenTheSameHazelcastInstanceIsInjected_thenTheyHaveDifferentTypeId() {
        AbstractSerializer serializer1 = new GlobalSerializer();
        serializer1.setHazelcastInstance(newMockHazelcastInstance());
        AbstractSerializer serializer2 = new GlobalSerializer();
        serializer2.setHazelcastInstance(newMockHazelcastInstance());

        HazelcastInstance hz = newMockHazelcastInstance();
        serializer1.setHazelcastInstance(hz);
        serializer2.setHazelcastInstance(hz);

        int typeId1 = serializer1.getTypeId();
        int typeId2 = serializer2.getTypeId();
        assertNotEquals(typeId1, typeId2);
    }

    @Test
    public void givenTwoSerializerExist_whenDifferentHazelcastInstanceAreInjected_thenTheyHaveTheSameTypeId() {
        AbstractSerializer serializer1 = new GlobalSerializer();
        serializer1.setHazelcastInstance(newMockHazelcastInstance());
        AbstractSerializer serializer2 = new GlobalSerializer();
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
    public void givenSpecificNeutralSerializerExist_whenObjectIsSerializedAndDeserialized_thenItHasTheSameInternalState() throws IOException {
        String input = "foo";
        AbstractSerializer<String> serializer = new GlobalSerializer();
        serializer.setHazelcastInstance(newMockHazelcastInstance());

        String output = TestUtils.serializeAndDeserializeObject(serializer, input);

        assertEquals(input, output);
    }
}
