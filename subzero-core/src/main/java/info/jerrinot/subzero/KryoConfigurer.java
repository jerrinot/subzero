package info.jerrinot.subzero;

import com.esotericsoftware.kryo.Kryo;

/**
 * This class is for configuring direclty Kryo
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
public interface KryoConfigurer {

    void configure(Kryo kryo);
}
