package info.jerrinot.subzero.internal;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import info.jerrinot.subzero.ClassFactory;
import info.jerrinot.subzero.UserSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.getProperty;

public final class PropertyUserSerializer implements UserSerializer {
    private static final String CUSTOM_CONFIG_PROPERTY_NAME = "subzero.custom.serializers.config.filename";
    private static final String DEFAULT_CONFIG_FILENAME = "subzero-serializers.properties";
    private static final String DEFAULT_SERIALIZER_CONFIG_KEY = "defaultSerializer";
    private static final String WELL_KNOWN_SERIALIZER_PACKAGES[] = {
            "de.javakaffee.kryoserializers",
            "com.esotericsoftware.kryo.serializers",
            "info.jerrinot.subzero.relocated.com.esotericsoftware.kryo.serializers"};


    private static Map<Class, Serializer> customerSerializers;
    private static Set<Method> specialSerializersRegistrationMethod;
    private static Class<? extends Serializer> defaultSerializerClass;

    public static final PropertyUserSerializer INSTANCE = new PropertyUserSerializer();

    private PropertyUserSerializer() {

    }

    static {
        reinitialize();
    }

    //public for testing only
    public static void reinitialize() {
        customerSerializers = new LinkedHashMap<Class, Serializer>();
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
            String serializerClassName = split[1].trim();
            if (DEFAULT_SERIALIZER_CONFIG_KEY.equals(domainClassName)) {
                defaultSerializerClass = findSerializerClass(serializerClassName);
            } else {
                addNewSerializer(domainClassName, serializerClassName);
            }
        } else if (split.length == 1) {
            String serializerClass = split[0];
            addNewSpecialSerializer(serializerClass);
        } else {
            throw new IllegalStateException("Invalid property " + line);
        }
    }

    private static void addNewSpecialSerializer(String serializerClassName) {
        Class serializerClass = findSerializerClass(serializerClassName);
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

    private static Class findSerializerClass(String serializerClassName) {
        Class serializerClass;
        try {
            serializerClass = Class.forName(serializerClassName);
            return serializerClass;
        } catch (ClassNotFoundException e) {
            //ok, let's try to find prefix a well known package
            if (serializerClassName.indexOf('.') == -1) {
                for (String wellKnownPackage : WELL_KNOWN_SERIALIZER_PACKAGES) {
                    String enrichedSerializerClassName = wellKnownPackage + "." + serializerClassName;
                    try {
                        serializerClass = Class.forName(enrichedSerializerClassName);
                        return serializerClass;
                    } catch (ClassNotFoundException e1) {
                        //ignored, we will throw IllegalStateException bellow
                    }
                }
            }
            throw new IllegalStateException("Serializer " + serializerClassName + " not found", e);
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
            Class serializerClass = findSerializerClass(serializerClassName);
            Constructor constructor = serializerClass.getConstructor();
            Serializer serializer = (Serializer) constructor.newInstance();

            customerSerializers.put(domainClazz, serializer);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
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
            kryo.register(clazz, serializer);
        }
        registerSpecialSerializers(kryo);
        if (defaultSerializerClass != null) {
            kryo.setDefaultSerializer(defaultSerializerClass);
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
