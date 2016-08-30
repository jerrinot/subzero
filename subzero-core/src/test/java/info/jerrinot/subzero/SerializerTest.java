package info.jerrinot.subzero;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.serialization.InternalSerializationService;
import info.jerrinot.subzero.test.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SerializerTest {

    private InternalSerializationService mockSerializationService;

    @Before
    public void setUp() {
        mockSerializationService = mock(InternalSerializationService.class);
    }

    @Test
    public void givenSingleSerializerExist_whenHazelcastInstanceIsInjected_thenTypeIdIsGreaterThenZero() {
        Serializer serializer = new Serializer();

        HazelcastInstance hz = mock(HazelcastInstance.class);
        serializer.setHazelcastInstance(hz);

        int typeId = serializer.getTypeId();
        assertTrue(typeId > 0);
    }

    @Test
    public void givenTwoSerializerExist_whenTheSameHazelcastInstanceIsInjected_thenTheyHaveDifferentTypeId() {
        Serializer serializer1 = new Serializer();
        Serializer serializer2 = new Serializer();

        HazelcastInstance hz = mock(HazelcastInstance.class);
        serializer1.setHazelcastInstance(hz);
        serializer2.setHazelcastInstance(hz);

        int typeId1 = serializer1.getTypeId();
        int typeId2 = serializer2.getTypeId();
        assertNotEquals(typeId1, typeId2);
    }

    @Test
    public void givenTwoSerializerExist_whenDifferentHazelcastInstanceAreInjected_thenTheyHaveTheSameTypeId() {
        Serializer serializer1 = new Serializer();
        Serializer serializer2 = new Serializer();

        HazelcastInstance hz1 = mock(HazelcastInstance.class);
        HazelcastInstance hz2 = mock(HazelcastInstance.class);
        serializer1.setHazelcastInstance(hz1);
        serializer2.setHazelcastInstance(hz2);

        int typeId1 = serializer1.getTypeId();
        int typeId2 = serializer2.getTypeId();
        assertEquals(typeId1, typeId2);
    }

    @Test(expected = AssertionError.class)
    public void givenTwoTypeSpecificSerializersWithTheSameTypeExists_whenTheSameHazelcastInstanceIsInjected_thenTheyThrowError() {
        Serializer serializer1 = new Serializer(String.class);
        Serializer serializer2 = new Serializer(String.class);

        HazelcastInstance hz = mock(HazelcastInstance.class);
        serializer1.setHazelcastInstance(hz);
        serializer2.setHazelcastInstance(hz);
    }

    @Test
    public void givenTwoTypeSpecificSerializersWithTheSameTypeExists_whenDifferentHazelcastInstancesAreInjected_thenTheyHaveTheSameTypeId() {
        Serializer serializer1 = new Serializer(String.class);
        Serializer serializer2 = new Serializer(String.class);

        HazelcastInstance hz1 = mock(HazelcastInstance.class);
        HazelcastInstance hz2 = mock(HazelcastInstance.class);
        serializer1.setHazelcastInstance(hz1);
        serializer2.setHazelcastInstance(hz2);

        int typeId1 = serializer1.getTypeId();
        int typeId2 = serializer2.getTypeId();
        assertEquals(typeId1, typeId2);
    }

    @Test
    public void givenSpecificNeutralSerializerExist_whenObjectIsSerializedAndDeserialized_thenItHasTheSameInternalState() throws IOException {
        String input = "foo";
        Serializer<String> serializer = new Serializer();

        String output = TestUtils.serializeAndDeserializeObject(serializer, input);

        assertEquals(input, output);
    }

    @Test
    public void givenTypeSpecificSerializerExist_whenObjectIsSerializedAndDeserialized_thenItHasTheSameInternalState() throws IOException {
        String input = "foo";
        Serializer<String> serializer = new Serializer(String.class);

        String output = TestUtils.serializeAndDeserializeObject(serializer, input);

        assertEquals(input, output);
    }

}
