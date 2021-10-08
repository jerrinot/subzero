package info.jerrinot.subzero.internal;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import info.jerrinot.subzero.ClassFactory;
import info.jerrinot.subzero.SerializerConfigurer;
import info.jerrinot.subzero.UserSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.System.getProperty;

public final class PropertyUserSerializer implements UserSerializer {
    private static final String CUSTOM_CONFIG_PROPERTY_NAME = "subzero.custom.serializers.config.filename";
    private static final String DEFAULT_CONFIG_FILENAME = "subzero-serializers.properties";
    private static final String DEFAULT_SERIALIZER_CONFIG_KEY = "defaultSerializer";
    private static final String SERIALIZER_CONFIGURERS_CONFIG_KEY = "serializerConfigurers";
    private static final String WELL_KNOWN_PACKAGES[] = {
            "de.javakaffee.kryoserializers",
            "com.esotericsoftware.kryo.serializers",
            "info.jerrinot.subzero.relocated.com.esotericsoftware.kryo.serializers",
            "info.jerrinot.subzero.configurers"};


    private static Map<Class, Serializer> customerSerializers;
    private static Set<Method> specialSerializersRegistrationMethod;
    private static Class<? extends Serializer> defaultSerializerClass;
    private static List<SerializerConfigurer> serializerConfigurers;

    public static final PropertyUserSerializer INSTANCE = new PropertyUserSerializer();

    private PropertyUserSerializer() {

    }

    static {
        reinitialize();
    }

    //public for testing only
    public static void reinitialize() {
        customerSerializers = new LinkedHashMap<Class, Serializer>();
        serializerConfigurers = new ArrayList<SerializerConfigurer>();
        specialSerializersRegistrationMethod = new LinkedHashSet<Method>();
        String configFilename = getProperty(CUSTOM_CONFIG_PROPERTY_NAME, DEFAULT_CONFIG_FILENAME);
        initCustomSerializers(configFilename);
    }

    private static void initCustomSerializers(String configFilename) {
        ClassLoader classLoader = PropertyUserSerializer.class.getClassLoader();
        InputStream configStream = classLoader.getResourceAsStream(configFilename);
        if (configStream == null) {
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(configStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                readLineAndRegister(line);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void readLineAndRegister(String line) {
        line = line.trim();
        if (line.startsWith("#")) {
            return;
        }
        String[] split = line.split("=");
        if (split.length == 2) {
            String domainClassName = split[0].trim();
            String className = split[1].trim();
            if (DEFAULT_SERIALIZER_CONFIG_KEY.equals(domainClassName)) {
                defaultSerializerClass = findClass(className);
            } else if (SERIALIZER_CONFIGURERS_CONFIG_KEY.equals(domainClassName)) {
                Class clazz = findClass(className);
                SerializerConfigurer configurer = createNewInstance(clazz);
                serializerConfigurers.add(configurer);
            } else {
                addNewSerializer(domainClassName, className);
            }
        } else if (split.length == 1) {
            String serializerClass = split[0];
            addNewSpecialSerializer(serializerClass);
        } else {
            throw new IllegalStateException("Invalid property " + line);
        }
    }

    private static void addNewSpecialSerializer(String serializerClassName) {
        Class serializerClass = findClass(serializerClassName);
        Method registrationMethod;
        try {
            registrationMethod = serializerClass.getMethod("registerSerializers", Kryo.class);
        } catch (NoSuchMethodException e) {
            String className = WellKnownClassesRepository.findDomainClassNameForWellKnownSerializer(serializerClass);
            if (className != null) {
                addNewSerializer(className, serializerClassName);
                return;
            }
            throw new IllegalStateException("Serializer " + serializerClassName
                    + " does not have expected method 'registerSerializers()': ", e);
        }
        specialSerializersRegistrationMethod.add(registrationMethod);
    }

    private static <T> Class<T> findClass(String classname) {
        Class<T> clazz;
        try {
            clazz = (Class<T>) Class.forName(classname);
            return clazz;
        } catch (ClassNotFoundException e) {
            //ok, let's try to find prefix a well known package
            if (classname.indexOf('.') == -1) {
                for (String wellKnownPackage : WELL_KNOWN_PACKAGES) {
                    String enrichedClassName = wellKnownPackage + "." + classname;
                    try {
                        clazz = (Class<T>) Class.forName(enrichedClassName);
                        return clazz;
                    } catch (ClassNotFoundException e1) {
                        //ignored, we will throw IllegalStateException bellow
                    }
                }
            }
            throw new IllegalStateException("Class " + classname + " not found", e);
        }
    }

    private static void addNewSerializer(String domainClassName, String serializerClassName) {
        try {
            Class domainClazz = Class.forName(domainClassName);
            if (ClassFactory.class.isAssignableFrom(domainClazz)) {
                //special case where the domain class is not the actual class, but rather
                //a factory to create other classes
                ClassFactory factory = (ClassFactory) domainClazz.newInstance();
                domainClazz = factory.createClass();
            }
            Class serializerClass = findClass(serializerClassName);
            Serializer serializer = createNewInstance(serializerClass);

            customerSerializers.put(domainClazz, serializer);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T> T createNewInstance(Class<?> clazz) {
        try {
            Constructor<T> constructor = (Constructor<T>) clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void registerSingleSerializer(Kryo kryo, Class clazz) {
        //we delegate to registerAllSerializers as we always want to register all serializers found
        //in the property file
        registerAllSerializers(kryo);
    }

    @Override
    public void registerAllSerializers(Kryo kryo) {
        for (Map.Entry<Class, Serializer> entry : customerSerializers.entrySet()) {
            Class clazz = entry.getKey();
            Serializer serializer = entry.getValue();
            for (SerializerConfigurer configurer : serializerConfigurers) {
                configurer.configure(clazz, configurer);
            }
            kryo.register(clazz, serializer);
        }
        registerSpecialSerializers(kryo);
        registerDefaultSerializer(kryo);
    }

    private void registerDefaultSerializer(Kryo kryo) {
        if (defaultSerializerClass != null || !serializerConfigurers.isEmpty()) {
            Class<? extends Serializer> serializerClass = defaultSerializerClass == null
                    ? FieldSerializer.class
                    : defaultSerializerClass;

            SerializerFactory serializerFactory = new SerializerFactory.ReflectionSerializerFactory(serializerClass);
            if (!serializerConfigurers.isEmpty()) {
                SerializerConfigurer configurer = serializerConfigurers.get(0);
                serializerFactory = new PostProcessingSerializerFactory(serializerFactory, configurer);
            }
            kryo.setDefaultSerializer(serializerFactory);
        }
    }

    private void registerSpecialSerializers(Kryo kryo) {
        for (Method registrationMethod : specialSerializersRegistrationMethod) {
            try {
                registrationMethod.invoke(null, kryo);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
