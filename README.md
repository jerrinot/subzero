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
have to be declared in the same order. As of Hazelcast 3.7 programmatic 
configuration will result in somewhat higher performance - this is given
by a limitation of Hazelcast declarative configuration API. It should be 
fixed Hazelcast 3.8

## Maven Coordinates
SubZero is available in Maven Central. Just insert this snippet into 
pom.xml and you are ready to roll! 
````xml
<dependency>
    <groupId>info.jerrinot</groupId>
    <artifactId>subzero-all</artifactId>
    <version>0.6</version>
</dependency>
````        
This version has all dependencies packaged inside. You can also use a 
version with regular dependencies:
````xml
<dependency>
    <groupId>info.jerrinot</groupId>
    <artifactId>subzero-core</artifactId>
    <version>0.6</version>
</dependency>
````

## Configuration
- System property `subzero.buffer.size.kb` sets buffer size for Kryo.
  Default value: 16KB
- System property `subzero.base.type.id` sets base for auto-generated
  type id

## Extensions
SubZero aims to provide the simplest possible way to hook Kryo
serialization into Hazelcast.   

Default SubZero serializer implementation uses auto-generated class
type IDs and relies on a serializer registration order. This means all
your cluster members have to use the same order in Hazelcast serializer
configuration. This can be somewhat fragile. You can make it more robust
by subclassing Serializer and returning a fixed class ID:
````java
public class HashMapSerializerExample extends Serializer<HashMap> {

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
SubZero is continuously tested with Hazelcast 3.6, 3.7 and 3.8-SNAPSHOT.

## Further Ideas
- More serialization strategies. Currently Kryo is the only supported
  strategy. I would like to add Fast Serialization
- Serializer Generator - SubZero could generate highly-optimized
  serializer for simple classes
- AutoPortable - serialize an ordinary class as it implemented
  Portable interface 
  


## Notable Contributors
- [Ashok Koyi](https://github.com/thekalinga) reported a [first bug](https://github.com/jerrinot/subzero/issues/3) and also came up with a solution. 


## Disclaimer
This is a community project not affiliated with the Hazelcast project. 
