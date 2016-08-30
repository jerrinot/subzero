package info.jerrinot.subzero;

import com.hazelcast.config.Config;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.config.SerializerConfig;
import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;

public class SubZeroTest {

    @Test
    public void givenGlobalSerializerConfigDoesNotExist_whenUseAsGlobalSerializer_thenNewSubZeroIsUsedAsGlobalSerializer() {
        Config config = new Config();

        SubZero.useAsGlobalSerializer(config);

        assertEquals(Serializer.class.getName(), config.getSerializationConfig().getGlobalSerializerConfig().getClassName());
    }

    @Test
    public void givenGlobalSerializerConfigDoes_whenUseAsGlobalSerializer_thenNewSubZeroIsUsedAsGlobalSerializer() {
        Config config = new Config();
        config.getSerializationConfig().setGlobalSerializerConfig(new GlobalSerializerConfig().setClassName("foo"));

        SubZero.useAsGlobalSerializer(config);

        assertEquals(Serializer.class.getName(), config.getSerializationConfig().getGlobalSerializerConfig().getClassName());
    }

    @Test
    public void useForClasses() {
        Config config = new Config();
        SubZero.useForClasses(config, String.class);

        Collection<SerializerConfig> serializerConfigs = config.getSerializationConfig().getSerializerConfigs();
        assertThat(serializerConfigs)
                .extracting("typeClass")
                .contains(String.class);
    }
}
