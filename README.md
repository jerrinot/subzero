![Phase Diagram](http://i.imgur.com/T080T6c.png)

## SubZero - Fast Serialization for Hazelcast

[![Join the chat at https://gitter.im/subzero-hz/Lobby](https://badges.gitter.im/subzero-hz/Lobby.svg)](https://gitter.im/subzero-hz/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/jerrinot/subzero.svg?branch=master)](https://travis-ci.org/jerrinot/subzero)

SubZero provides fast & non-invasive serialization for Hazelcast. 
It's easy-to-use integration of fast serialization libraries such as Kryo into Hazelcast. 
 
### How to Use SubZero?

#### Use SubZero for all classes
SubZero will completely replace Java serialization. Hazelcast internal serializers
will still take precedence.

##### Declarative Configuration:
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

##### Programmatic Configuration:
````java
Config config = new Config();
SubZero.useAsGlobalSerializer(config);
HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
````

#### Use SubZero for selected classes only
In this mode Hazelcast will use SubZero for selected classes only. 

##### Declarative Configuration:
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

##### Programmatic Configuration:
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

### Maven Configuration
The project is currently not in the Maven Central Repository. You can use 
the awesome [JitPack](https://www.jitpack.io/) service for simple integration 
into your project.

#### How to configure Maven to fetch SubZero from JitPack:
1. Add JitPack repository into your pom.xml:

    ````xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://www.jitpack.io</url>
        </repository>
    </repositories>
    ````

2. Add SubZero as a dependency into your project:

    ````xml
    <dependency>
        <groupId>com.github.jerrinot.subzero</groupId>
        <artifactId>subzero-all</artifactId>
        <version>master-SNAPSHOT</version>
    </dependency>
````

### Hazelcast Compatibility
SubZero is continuously tested with Hazelcast 3.6, 3.7 and 3.8-SNAPSHOT.

### TODO
- Upload into Maven Central
- More serialization strategies. Currently Kryo is the only supported strategy.
- Better Documentation

### Disclaimer
This is a community project not affiliated with the Hazelcast project. 
