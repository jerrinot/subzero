package info.jerrinot.subzero.internal;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import info.jerrinot.subzero.UserSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.System.getProperty;

public final class PropertyUserSerializer implements UserSerializer {
    private static final String CUSTOM_CONFIG_PROPERTY_NAME = "subzero.custom.serializers.config.filename";
    private static final String DEFAULT_CONFIG_FILENAME = "subzero-serializers.properties";

    private static final String CONFIG_FILE_NAME = getProperty(CUSTOM_CONFIG_PROPERTY_NAME, DEFAULT_CONFIG_FILENAME);

    private static Map<Class, Serializer> customerSerializers;

    public PropertyUserSerializer() {
        if (customerSerializers != null) {
            //custom serializers were already initialized
            return;
        }

        customerSerializers = new LinkedHashMap<Class, Serializer>();
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
        if (split.length != 2) {
            throw new IllegalStateException("Invalid property " + line);
        }
        String domainClassName = split[0].trim();
        String serializerClassName = split[1].trim();

        addNewSerializer(domainClassName, serializerClassName);
    }

    private void addNewSerializer(String domainClassName, String serializerClassName) {
        try {
            Class domainClazz = Class.forName(domainClassName);
            Class serializerClass = Class.forName(serializerClassName);
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
        Serializer serializer = customerSerializers.get(clazz);
        if (serializer != null) {
            kryo.register(clazz, serializer);
        }
    }

    @Override
    public void registerAllSerializers(Kryo kryo) {
        for (Map.Entry<Class, Serializer> entry : customerSerializers.entrySet()) {
            Class clazz = entry.getKey();
            Serializer serializer = entry.getValue();
            kryo.register(clazz, serializer);
        }
    }
}
