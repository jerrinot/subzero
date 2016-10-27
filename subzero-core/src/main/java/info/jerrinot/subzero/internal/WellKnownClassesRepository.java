package info.jerrinot.subzero.internal;

import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

class WellKnownClassesRepository {
    private static final Map<String, String> serializer2DomainClass = new HashMap<String, String>();

    static {
        //jdk stuff
        serializer2DomainClass.put("de.javakaffee.kryoserializers.ArraysAsListSerializer", Arrays.asList( "" ).getClass().getName());
        serializer2DomainClass.put("de.javakaffee.kryoserializers.CollectionsEmptyListSerializer", Collections.EMPTY_LIST.getClass().getName());
        serializer2DomainClass.put("de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer", Collections.EMPTY_MAP.getClass().getName());
        serializer2DomainClass.put("de.javakaffee.kryoserializers.CollectionsEmptySetSerializer", Collections.EMPTY_SET.getClass().getName());
        serializer2DomainClass.put("de.javakaffee.kryoserializers.CollectionsSingletonListSerializer", Collections.singletonList( "" ).getClass().getName());
        serializer2DomainClass.put("de.javakaffee.kryoserializers.CollectionsSingletonSetSerializer", Collections.singleton( "" ).getClass().getName());
        serializer2DomainClass.put("de.javakaffee.kryoserializers.CollectionsSingletonMapSerializer", Collections.singletonMap( "", "" ).getClass().getName());
        serializer2DomainClass.put("de.javakaffee.kryoserializers.GregorianCalendarSerializer", GregorianCalendar.class.getName());
        serializer2DomainClass.put("de.javakaffee.kryoserializers.JdkProxySerializer", InvocationHandler.class.getName());
    }

    static String findDomainClassNameForWellKnownSerializer(Class clazz) {
        return serializer2DomainClass.get(clazz.getName());
    }
}
