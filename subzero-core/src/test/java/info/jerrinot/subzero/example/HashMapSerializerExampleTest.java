package info.jerrinot.subzero.example;

import info.jerrinot.subzero.test.TestUtils;
import org.junit.Test;

import java.util.HashMap;

import static info.jerrinot.subzero.test.TestUtils.newMockHazelcastInstance;
import static org.junit.Assert.*;

public class HashMapSerializerExampleTest {

    @Test
    public void testHashMapSerializer() throws Exception {
        HashMapSerializerExample serializer = new HashMapSerializerExample();
        serializer.setHazelcastInstance(newMockHazelcastInstance());

        HashMap<Integer, String> inputMap = new HashMap<>();
        inputMap.put(0, "Zero");

        HashMap deserializedMap = TestUtils.serializeAndDeserializeObject(serializer, inputMap);
        assertEquals(inputMap, deserializedMap);
    }

}