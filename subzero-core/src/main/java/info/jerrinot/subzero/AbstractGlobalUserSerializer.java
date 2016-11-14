package info.jerrinot.subzero;

import info.jerrinot.subzero.internal.strategy.GlobalKryoStrategy;

/**
 * This class is for registering Kryo serialization globally with custom serialization for specific types
 * programatically rather than through use of subzero-serializers.properties properties file.
 *
 * <code>
 *      import static info.jerrinot.subzero.UserSerializerConfig.register;
 *      [...]
 *
 *      public static class MyGlobalSerializer extends AbstractGlobalUserSerializer {
 *
 *          public MyGlobalSerializer() {
 *              super(register(MyObject.class, new MyObjectKryoSerializer())
 *                   .register(MyOtherObject.class, new MyOtherObjectKryoSerializer());
 *          }
 *
 *      }
 * </code>
 *
 * or direclty acessing Kryo
 *
 * <code>
 *      import static info.jerrinot.subzero.UserSerializerConfig.delegate;
 *      [...]
 *
 *      public static class MyGlobalDelegateSerializationConfig extends AbstractGlobalUserSerializer {
 *          public MyGlobalDelegateSerlizationConfig() {
 *              super(UserSerializerConfig.delegate(new KryoConfigurer() {
 *                  @Override
 *                  public void configure(Kryo kryo) {
 *                      kryo.register(AnotherNonSerializableObject.class, new AnotherNonSerializableObjectKryoSerializer());
 *                  }
 *              }));
 *          }
 *      }
 * </code>
 *
 */
public abstract class AbstractGlobalUserSerializer extends AbstractSerializer {

    public AbstractGlobalUserSerializer(UserSerializerConfig.UserSerializationBuilder userSerializer) {
        super(new GlobalKryoStrategy(userSerializer));
    }
}
