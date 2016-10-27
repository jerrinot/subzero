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

    private static final String WELL_KNOWN_SERIALIZER_PACKAGE = "de.javakaffee.kryoserializers";

    private static final String CONFIG_FILE_NAME = getProperty(CUSTOM_CONFIG_PROPERTY_NAME, DEFAULT_CONFIG_FILENAME);

    private static Map<Class, Serializer> customerSerializers;
    private static Set<Method> specialSerializersRegistrationMethod;

    public PropertyUserSerializer() {
        if (customerSerializers != null) {
            //custom serializers were already initialized
            return;
        }

        customerSerializers = new LinkedHashMap<Class, Serializer>();
        specialSerializersRegistrationMethod = new LinkedHashSet<Method>();
        initCustomSerializers();
    }

    private void initCustomSerializers() {
        ClassLoader classLoader = PropertyUserSerializer.class.getClassLoader();
        InputStream configStream = classLoader.getResourceAsStream(CONFIG_FILE_NAME);
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

    private void readLineAndRegister(String line) {
        line = line.trim();
        if (line.startsWith("#")) {
            return;
        }
        String[] split = line.split("=");
        if (split.length == 2) {
            String domainClassName = split[0].trim();
            String serializerClassName = split[1].trim();
            addNewSerializer(domainClassName, serializerClassName);
        } else if (split.length == 1) {
            String serializerClass = split[0];
            addNewSpecialSerializer(serializerClass);
        } else {
            throw new IllegalStateException("Invalid property " + line);
        }
    }

    private void addNewSpecialSerializer(String serializerClassName) {
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

    private Class findSerializerClass(String serializerClassName) {
        Class serializerClass;
        try {
            serializerClass = Class.forName(serializerClassName);
            return serializerClass;
        } catch (ClassNotFoundException e) {
            //ok, let's try to find prefix a well known package
            if (serializerClassName.indexOf('.') == -1) {
                String enrichedSerializerClassName = WELL_KNOWN_SERIALIZER_PACKAGE + "." + serializerClassName;
                try {
                    serializerClass = Class.forName(enrichedSerializerClassName);
                    return serializerClass;
                } catch (ClassNotFoundException e1) {
                    //ignored, we will throw
                }
            }
            throw new IllegalStateException("Serializer " + serializerClassName + " not found", e);
        }
    }

    private void addNewSerializer(String domainClassName, String serializerClassName) {
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
