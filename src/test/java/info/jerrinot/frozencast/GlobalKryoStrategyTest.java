package info.jerrinot.frozencast;

import info.jerrinot.frozencast.internal.strategy.GlobalKryoStrategy;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GlobalKryoStrategyTest {

    @Test
    public void foo() throws IOException {
        Person joe = new Person("Joe");
        GlobalKryoStrategy kryoStrategy = new GlobalKryoStrategy();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        kryoStrategy.write(baos, joe);
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Person deserialized = (Person) kryoStrategy.read(bais);

        assertEquals(joe, deserialized);
    }
}
