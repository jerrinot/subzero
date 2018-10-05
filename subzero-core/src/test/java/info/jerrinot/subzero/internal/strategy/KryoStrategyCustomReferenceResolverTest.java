package info.jerrinot.subzero.internal.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ReferenceResolver;
import info.jerrinot.subzero.UserSerializer;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class KryoStrategyCustomReferenceResolverTest {

    @SuppressWarnings("unchecked")
    @Test
    public void should_use_strategy_with_specified_reference_resolver() throws Exception {
        // given
        System.setProperty("subzero.referenceresolver.class", "info.jerrinot.subzero.internal.strategy.NullReferenceResolver");
        GlobalKryoStrategy kryoStrategy = new GlobalKryoStrategy(NULL_USER_SERIALIZER);

        // when
        Field field = KryoStrategy.class.getDeclaredField("KRYOS");
        field.setAccessible(true);
        Class<? extends ReferenceResolver> actualClassResolver = ((ThreadLocal<KryoContext>) field.get(kryoStrategy))
                .get().getKryo().getReferenceResolver().getClass();

        // then
        assertEquals(NullReferenceResolver.class, actualClassResolver);
    }

    private static final UserSerializer NULL_USER_SERIALIZER = new UserSerializer() {
        @Override
        public void registerSingleSerializer(Kryo kryo, Class clazz) {
            // do nothing
        }
        @Override
        public void registerAllSerializers(Kryo kryo) {
            // do nothing
        }
    };
}
