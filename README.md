## SubZero - Fast Serialization for Hazelcast

SubZero provides fast & non-invasive serialization to Hazelcast. 
It's a pre-canned integration of fast serialization libraries such as Kryo into Hazelcast. 
 
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
import static info.jerrinot.subzero.SubZero.subZeroAsDefaultSerializer;
[...]
Config hazelcastConfig = new Config();
subZeroAsDefaultSerializer(hazelcastConfig);
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
import static info.jerrinot.subzero.SubZero.subZeroForClasses;
[...]
Config hazelcastConfig = new Config();
subZeroForClasses(hazelcastConfig, Foo.class, Bar.class);
````

All cluster members have to use SubZero for the same types and the types
have to be declared in the same order. Currently programmatic configuration
will result in somewhat higher performance - this is given by a limitation
of Hazelcast declarative configuration API. It should be fixed in the next
version of Hazelcast.   

### Maven Configuration
The project is currently not in Maven Central Repository. You can use [JitPack](https://www.jitpack.io/) for simple integration into your project.

### TODO
- Upload into Maven Central
- More serialization strategies. Currently Kryo is the only supported strategy.
- Better Documentation   