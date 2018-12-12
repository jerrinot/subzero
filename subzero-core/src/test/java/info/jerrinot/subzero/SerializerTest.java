package info.jerrinot.subzero;

import com.esotericsoftware.kryo.KryoException;
import com.hazelcast.core.HazelcastInstance;
import info.jerrinot.subzero.internal.PropertyUserSerializer;
import info.jerrinot.subzero.test.TestUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static info.jerrinot.subzero.test.TestUtils.newMockHazelcastInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SerializerTest {

    @After
    public void tearDown() {
        System.clearProperty("subzero.custom.serializers.config.filename");
        PropertyUserSerializer.reinitialize();
    }

    @Test
    public void givenSingleSerializerExist_whenHazelcastInstanceIsInjected_thenTypeIdIsGreaterThenZero() {
        Serializer serializer = new Serializer();
        serializer.setHazelcastInstance(newMockHazelcastInstance());

        HazelcastInstance hz = newMockHazelcastInstance();
        serializer.setHazelcastInstance(hz);

        int typeId = serializer.getTypeId();
        assertTrue(typeId > 0);
    }

    @Test
    public void givenTwoSerializerExist_whenTheSameHazelcastInstanceIsInjected_thenTheyHaveDifferentTypeId() {
        Serializer serializer1 = new Serializer();
        serializer1.setHazelcastInstance(newMockHazelcastInstance());
        Serializer serializer2 = new Serializer();
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
        Serializer serializer1 = new Serializer();
        serializer1.setHazelcastInstance(newMockHazelcastInstance());
        Serializer serializer2 = new Serializer();
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
        Serializer<String> serializer = new Serializer();
        serializer.setHazelcastInstance(newMockHazelcastInstance());

        String output = TestUtils.serializeAndDeserializeObject(serializer, input);

        assertEquals(input, output);
    }

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

    @Test
    public void givenJodaLocalDateIsRegisteredInProperties_whenLocalDateIsSerializedAndDeserialized_thenPrintlnDoesntThrowNPE() throws IOException {
        LocalDate input = LocalDate.now();
        Serializer<LocalDate> serializer = new Serializer<LocalDate>(LocalDate.class);
        serializer.setHazelcastInstance(newMockHazelcastInstance());

        LocalDate output = TestUtils.serializeAndDeserializeObject(serializer, input);

        //this throw an NPE when Joda LocalDate is not registered in Kryo
        System.out.println(output);
    }

    @Test(expected = KryoException.class)
    public void testAddedFields_defaultSerializer() throws Exception {
        testAddedFields();
    }

    @Test
    public void testAddedFields_compatibilitySerializer() throws Exception {
        System.setProperty("subzero.custom.serializers.config.filename", "compatible-field-default-serializer.properties");
        PropertyUserSerializer.reinitialize();

        testAddedFields();
    }

    private void testAddedFields() throws Exception {
        String classname = "some.pckage.SyntheticPerson";
        String v1Field = "firstname";
        String[] v2Fields = {v1Field, "lastname"};
        String expectedFirstname = "somename";

        //v1 class has just a single field: firstname
        ByteArrayClassLoader v1classLoader = createClass(classname, v1Field);
        Serializer<Object> serializerV1 = new Serializer<Object>((Class<Object>) v1classLoader.loadClass(classname));
        serializerV1.setHazelcastInstance(newMockHazelcastInstance(v1classLoader));

        //v2 class has two fields - the default Kryo serializer is not deserialize it
        ByteArrayClassLoader v2classLoader = createClass(classname, v2Fields);
        Serializer<Object> serializerV2 = new Serializer<Object>((Class<Object>) v2classLoader.loadClass(classname));
        serializerV2.setHazelcastInstance(newMockHazelcastInstance(v2classLoader));

        Object v1Instance = v1classLoader.loadClass(classname).newInstance();
        v1Instance.getClass().getField(v1Field).set(v1Instance, expectedFirstname);

        byte[] blob = TestUtils.serialize(serializerV1, v1Instance);
        Object v2Instance = TestUtils.deserialize(serializerV2, blob);
        String actualFirstname = (String) v2Instance.getClass().getField(v1Field).get(v2Instance);

        assertEquals(expectedFirstname, actualFirstname);
    }

    private ByteArrayClassLoader createClass(String classname, String...fields) {
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
