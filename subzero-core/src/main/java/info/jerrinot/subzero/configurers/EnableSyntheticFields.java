package info.jerrinot.subzero.configurers;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import info.jerrinot.subzero.SerializerConfigurer;

public final class EnableSyntheticFields implements SerializerConfigurer {
    @Override
    public void configure(Class<?> clazz, Object serializer) {
        if (serializer instanceof FieldSerializer) {
            ((FieldSerializer<?>) serializer).setIgnoreSyntheticFields(false);
        }
    }
}
