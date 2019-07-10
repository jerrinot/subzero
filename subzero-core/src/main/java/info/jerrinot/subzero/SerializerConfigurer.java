package info.jerrinot.subzero;

public interface SerializerConfigurer {
    void configure(Class<?> clazz, Object serializer);
}
