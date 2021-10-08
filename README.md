# SubZero - Fast Serialization for Hazelcast

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/info.jerrinot/subzero-all/badge.svg)](https://maven-badges.herokuapp.com/maven-central/info.jerrinot/subzero-all)
[![Build Status](https://travis-ci.org/jerrinot/subzero.svg?branch=master)](https://travis-ci.org/jerrinot/subzero)
[![Join the chat at https://gitter.im/subzero-hz/Lobby](https://badges.gitter.im/subzero-hz/Lobby.svg)](https://gitter.im/subzero-hz/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

SubZero provides dead easy Hazelcast - Kryo integration. No coding required.

## Why?
Kryo is a popular serialization library. It's [super-fast](https://github.com/eishay/jvm-serializers/wiki) yet easy-to-use.
It does not pollute your domain model and it can even serialize classes
which are not marked as `Serializable`. 

Hazelcast has no out-of-the box support for Kryo. It's [rather easy](http://blog.hazelcast.com/kryo-serializer/) to
integrate it, however it means everyone has to write the same code and
face the [same bugs](https://github.com/hazelcast/hazelcast/issues?utf8=%E2%9C%93&q=is%3Aissue%20kryo).
 
SubZero aims to make Kryo - Hazelcast integration as simple as possible.
 

## How to Use SubZero?

### Use SubZero for all classes
SubZero will completely replace Java serialization. Hazelcast internal
serializers will still take precedence.

#### Declarative Configuration:
Insert this snippet into your Hazelcast configuration XML:
````xml
<serialization>
    <serializers>
        <global-serializer override-java-serialization="true">
            info.jerrinot.subzero.Serializer
        </global-serializer> 
   </serializers>
</serialization>
````

#### Programmatic Configuration:
````java
Config config = new Config();
SubZero.useAsGlobalSerializer(config);
HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
````

### Use SubZero for selected classes only
In this mode Hazelcast will use SubZero for selected classes only. 

#### Declarative Configuration:
````xml
<serialization>
    <serializers>
        <serializer type-class="some.package.Foo"
            class-name="info.jerrinot.subzero.Serializer"/> 
        <serializer type-class="some.package.Bar"
            class-name="info.jerrinot.subzero.Serializer"/>
   </serializers>
</serialization>
````

#### Programmatic Configuration:
````java
Config config = new Config();
SubZero.useForClasses(config, Foo.class, Bar.class);
HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
````

All cluster members have to use SubZero for the same types and the types
have to be declared in the same order. 

## Maven Coordinates
SubZero is available in Maven Central. Just insert this snippet into 
pom.xml and you are ready to roll! 
````xml
<dependency>
    <groupId>info.jerrinot</groupId>
    <artifactId>subzero-all</artifactId>
    <version>0.11</version>
</dependency>
````        
This version has all dependencies packaged inside. You can also use a 
version with regular dependencies:
````xml
<dependency>
    <groupId>info.jerrinot</groupId>
    <artifactId>subzero-core</artifactId>
    <version>0.11</version>
</dependency>
````

## Configuration Options
- System property `subzero.buffer.size.kb` sets buffer size for Kryo.
  Default value: 16KB
- System property `subzero.base.type.id` sets base for auto-generated
  type id
- System property `subzero.referenceresolver.class` sets the ReferenceResolver implementation. Default value: `com.esotericsoftware.kryo.util.MapReferenceResolver`
  
## Custom Kryo Serializers
SubZero can use custom Kryo serializers. 
### Simple Registration
Just create a file `subzero-serializers.properties`
and have it on a classpath of your project. SubZero expects the property file to have the following format:
````
some.package.YouDomainClass=other.package.KryoSerializer
````
### Advanced Registration
The simple approach works fine in most cases, but sometimes you do not know
domain classnames up front - for example when the class is 
created by a factory - think of `Collections::unmodifiableList`

In this case it's OK to have just the Kryo serializers in property file.
For example:
````
my.package.KryoSerializerClass
````
Subzero expects the serializer to have a method `registerSerializers`
which accepts an instance of `Kryo` as its only argument. 

It's up to the serializer to register itself into Kryo. This approach
works for most serializer from [this project](https://github.com/magro/kryo-serializers).
Actually serializers from this project are considered to be well-known and it's ok to use just a classname
without package in the property file.
For example:
````
UnmodifiableCollectionsSerializer
ArraysAsListSerializer
````  

### Default Kryo Serializer
Kryo uses [FieldSerializer](https://github.com/EsotericSoftware/kryo/blob/498cd2b9765d3af8c77d3c4b1ede993fe3dd45a7/src/com/esotericsoftware/kryo/serializers/FieldSerializer.java) 
by default. Sometimes you want to change it. 

For example when you want support adding new fields then you have to use [CompatibleFieldSerializer](https://github.com/EsotericSoftware/kryo/blob/498cd2b9765d3af8c77d3c4b1ede993fe3dd45a7/src/com/esotericsoftware/kryo/serializers/CompatibleFieldSerializer.java).
You can do this by using the key `defaultSerializer` in the property file.

Example:
`defaultSerializer=com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer`

This mechanism also understands the concept of well-known packages: 
Serializers from the Kryo project itself and [Magro serializers](https://github.com/magro/kryo-serializer) 
can be referenced just by a simple classname, no need to specify package. This is handy
when using the Subzero-all version which has the Kryo serializers relocated into
another package. 

## Extensions
SubZero aims to provide the simplest possible way to hook Kryo
serialization into Hazelcast.   

Default SubZero serializer implementation uses auto-generated class
type IDs and relies on a serializer registration order. This means all
your cluster members have to use the same order in Hazelcast serializer
configuration. This can be somewhat fragile. You can make it more robust
by subclassing Serializer and returning a fixed class ID:
````java
public class HashMapSerializerExample extends AbstractTypeSpecificUserSerializer<HashMap> {

    public HashMapSerializerExample() {
        super(HashMap.class);
    }

    /**
     * TypeId has to be a unique for each registered serializer.
     *
     * @return TypeId of the class serialized by this serializer
     */
    @Override
    public int getTypeId() {
        return 10000;
    }
}
````

## Hazelcast Compatibility
SubZero is continuously tested with Hazelcast 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 3.12, 4.0 and 5.0

## Requirments
Subzero requires JDK8 or newer.

## Known Issues
Subzero-All currently [does not support](https://github.com/jerrinot/subzero/issues/46) 3rd party Kryo serializers. If you need to register a 3rd party serializer then use Subzero-Core. 

## Further Ideas
- More serialization strategies. Currently Kryo is the only supported
  strategy. I would like to add Fast Serialization
- Serializer Generator - SubZero could generate highly-optimized
  serializer for simple classes
- AutoPortable - serialize an ordinary class as it implemented
  Portable interface 
  


## Notable Contributors
- [Ashok Koyi](https://github.com/thekalinga) reported a [first bug](https://github.com/jerrinot/subzero/issues/3) and also came up with a solution.
- [Will Neild](https://github.com/wneild) came up with an idea to register [custom Kryo serializers](https://github.com/jerrinot/subzero/issues/6) and contributed to other parts as well.   
- [Adrian](https://github.com/acieplak) contributed support for [custom reference resolvers](https://github.com/jerrinot/subzero/pull/25).
- [Tomasz Gawęda](https://github.com/TomaszGaweda) for [Hazelcast 4.x compatibility](https://github.com/jerrinot/subzero/pull/40).


## Disclaimer
This is a community project not affiliated with the Hazelcast project. 
