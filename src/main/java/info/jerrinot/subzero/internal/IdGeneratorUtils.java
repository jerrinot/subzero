package info.jerrinot.subzero.internal;

import com.hazelcast.core.HazelcastInstance;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.newSetFromMap;

public class IdGeneratorUtils {
    private static final int BASE_ID = Integer.getInteger("subzero.base.id", 6000);

    private static ConcurrentHashMap<HazelcastInstance, IdSequence> counterMap =
            new ConcurrentHashMap<HazelcastInstance, IdSequence>();

    public static int newIdForType(HazelcastInstance hz, Class<?> type) {
        IdSequence idSequence = getOrCreateSequence(hz);
        return idSequence.newIdFor(type);
    }

    public static int newId(HazelcastInstance hz) {
        IdSequence idSequence = getOrCreateSequence(hz);
        return idSequence.newId();
    }

    public static void instanceDestroyed(HazelcastInstance hz) {
        counterMap.remove(hz);
    }

    private static IdSequence getOrCreateSequence(HazelcastInstance hazelcastInstance) {
        IdSequence currentSequence = counterMap.get(hazelcastInstance);
        if (currentSequence != null) {
            return currentSequence;
        }
        IdSequence newSequence = new IdSequence();
        currentSequence = counterMap.putIfAbsent(hazelcastInstance, newSequence);
        return currentSequence == null ? newSequence : currentSequence;
    }

    private static class IdSequence {
        private Set<Class<?>> knownTypes = newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());
        private AtomicInteger counter = new AtomicInteger(BASE_ID);

        private int newIdFor(Class<?> clazz) {
            boolean added = knownTypes.add(clazz);
            if (!added) {
                throw new AssertionError("A serializer for " + clazz + " has been configured twice");
            }
            return counter.getAndIncrement();
        }

        private int newId() {
            return counter.getAndIncrement();
        }
    }
}
