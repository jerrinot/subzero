package info.jerrinot.subzero.example;

import info.jerrinot.subzero.AbstractTypeSpecificUserSerializer;

import java.util.HashMap;

/**
 * Example of a custom SubZero serializer for a specific type. It returns a constant
 * {@link #getTypeId()} hence it does not rely on serializer configuration order.
 * <br>
 * This is how you would use this serializer in Hazelcast configuration:
 * <pre>
 * {@code
 * <serialization>
 *   <serializers>
 *     <serializer type-class="java.util.HashMap" class-name="info.jerrinot.subzero.example.HashMapSerializerExample"/>
 *   </serializers>
 * </serialization>
 * }
 * </pre>
 */
public class HashMapSerializerExample extends AbstractTypeSpecificUserSerializer<HashMap> {

    public HashMapSerializerExample() {
        super(HashMap.class);
    }

    /**
     * TypeId has to be a unique for each registered serializer.
     *
     * @return TypeId of the class serialized by this serializer
     */
    @Override
    public int getTypeId() {
        return 10000;
    }
}
