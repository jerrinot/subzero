package info.jerrinot.subzero.test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class MutatingObjectSerializer extends Serializer<NonSerializableObjectRegisteredInDefaultConfigFile> {

    @Override
    public void write(Kryo kryo, Output output, NonSerializableObjectRegisteredInDefaultConfigFile object) {

    }

    @Override
    public NonSerializableObjectRegisteredInDefaultConfigFile read(Kryo kryo, Input input, Class type) {
        NonSerializableObjectRegisteredInDefaultConfigFile object = new NonSerializableObjectRegisteredInDefaultConfigFile();
        object.name = "deserialized";
        return object;
    }
}
