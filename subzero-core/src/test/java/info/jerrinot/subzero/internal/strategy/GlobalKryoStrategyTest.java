package info.jerrinot.subzero.internal.strategy;

import info.jerrinot.subzero.test.NonSerializableObject;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static info.jerrinot.subzero.test.TestUtils.newMockHazelcastInstance;
import static org.junit.Assert.assertEquals;

public class GlobalKryoStrategyTest {

    @Test
    public void foo() throws IOException {
        NonSerializableObject joe = new NonSerializableObject("Joe");
        GlobalKryoStrategy kryoStrategy = new GlobalKryoStrategy();
        kryoStrategy.setHazelcastInstance(newMockHazelcastInstance());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        kryoStrategy.write(baos, joe);
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        NonSerializableObject deserialized = (NonSerializableObject) kryoStrategy.read(bais);

        assertEquals(joe, deserialized);
    }
}
