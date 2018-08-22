

# geometry-api-java

This fork of the Esri Geometry API for Java exists in order to run the geo-grpc geometry-operators-service. 

The Esri Geometry API for Java can be used to enable spatial data processing in 3rd-party data-processing solutions.  Developers of custom MapReduce-based applications for Hadoop can use this API for spatial processing of data in the Hadoop system.  The API is also used by the [Hive UDF’s](https://github.com/Esri/spatial-framework-for-hadoop) and could be used by developers building geometry functions for 3rd-party applications such as [Cassandra]( https://cassandra.apache.org/), [HBase](http://hbase.apache.org/), [Storm](http://storm-project.net/) and many other Java-based “big data” applications.

## Features
* API methods to create simple geometries directly with the API, or by importing from supported formats: JSON, WKT, WKB, and Shape
* API methods for spatial operations: union, difference, intersect, clip, cut, and buffer
* API methods for topological relationship tests: equals, within, contains, crosses, and touches


## Build Locally

This library depends on the JNI enabled Proj.4 repo maintained [here](https://github.com/geo-grpc/proj.4). You must build the JNI version of Proj.4 before you can build and run this geometry library locally. After building Proj.4 you can build the jars by using the gradle script:
```bash
./gradlew build install
```

## Building for developement

As of right now I've only debugged this library using Intellij. The .idea file is included in the repo, so if you'd like you can call `/usr/local/bin/idea .idea` to open up the project. You might have to go into your preferences `Build, Execution, Deployment` -> `Build Tools` -> `Gradle` -> `Runner` and for the "Run Tests using" drop down select `Gradle Test Runner`

### Building Protobuf
To compile the protobuf code you'll need to follow the below instructions
https://github.com/grpc/grpc-java/blob/master/COMPILING.md#build-protobuf

### Test Running and examples
For Intellij set the `java.library.path`, [this StackOverflow post](http://stackoverflow.com/a/19311972/445372) describes debugging and building with it. And in the case of Gradle, I don't know where to set the following:
```bash
-Djava.library.path=<path to the libproj, if needed>
```
for example on my mac, in Intellij, I have set the `VM Options` in my test configuration to:
```bash
-ea  -Djava.library.path=/usr/local/lib/
```

## Build Docker Image

The Docker images are based off of the [openjdk](https://hub.docker.com/_/openjdk/) images. You can build a jdk image or a jre image, you can use Java 8 or 10 (maybe 11, haven't tested), and you can use debian or alpine.

### Building Debian
To build the latest debian 8 jdk image:
```bash
docker build -t us.gcr.io/echoparklabs/geometry-api-java:8-jdk-slim .
```
The latest debian 8 jre image
```bash
docker build --build-arg JRE_TAG=8-jre-slim -t us.gcr.io/echoparklabs/geometry-api-java:8-jre-slim .
```
To build the latest debian 10 jdk:
```bash
docker build --build-arg JDK_TAG=10-jdk-slim -t us.gcr.io/echoparklabs/geometry-api-java:10-jdk-slim .
```
To build the latest debian 10 jre:
```bash
docker build --build-arg JDK_TAG=10-jdk-slim --build-arg JRE_TAG=10-jre-slim \
       -t us.gcr.io/echoparklabs/geometry-api-java:10-jdk-slim .
```


### Building Alpine
At this time, the resulting Alpine docker image is about 50% smaller than the slim debian images. The default Alpine image uses the `8-jdk-apline` image

To build the latest Alpine JDK 8 image:
```bash
docker build -t us.gcr.io/echoparklabs/geometry-api-java:8-jdk-alpine -f Dockerfile.alpine .
```

To build the latest Alpine JRE image use the jre tag with a `--build-arg` (it will default to the latest JDK 8 alpine image):
```bash
docker build --build-arg JRE_TAG=8-jre-alpine \
       -t us.gcr.io/echoparklabs/geometry-api-java:8-jre-alpine -f Dockerfile.alpine .
```


### Building with specific jdk docker images:

To build a specific Alpine JDK 8 image use the `--build-arg`. For example if you wanted to build off of the `8u171-jdk-alpine3.8` openjdk image:
```bash
docker build --build-arg JDK_TAG=8u171-jdk-alpine3.8 \
       -t us.gcr.io/echoparklabs/geometry-api-java:8u171-jdk-alpine3.8 -f Dockerfile.alpine .
```

And to build a specific jre image use the following `--build-args`. For example if you wanted to the `8u171-jre-alpine3.8`  you would need to also specifiy `8u171-jdk-alpine3.8` JDK:
```bash
docker build --build-arg JRE_TAG=8u171-jre-alpine3.8 \
       --build-arg JDK_TAG=8u171-jdk-alpine3.8 \
       -t us.gcr.io/echoparklabs/geometry-api-java:8u171-jre-alpine3.8 -f Dockerfile.alpine .
```


## Requirements

* Java JDK 1.8 or greater.
* Gradle (Gradle required for development, for build, you only need to execute ./gradlew)

## Documentation
* [geometry-api-java/Wiki](https://github.com/Esri/geometry-api-java/wiki/)
* [geometry-api-java/Javadoc](http://esri.github.com/geometry-api-java/javadoc/)

## Resources

* [Website](http://echoparklabs.io)
* [twitter@Fogmodeler](http://twitter.com/Fogmodeler)

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Licensing ESRI
Copyright 2013-2018 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt](https://raw.github.com/Esri/geometry-api-java/master/license.txt) file.

## Licensing Echo Park Labs

Copyright 2017-2018 Echo Park Labs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt](https://raw.github.com/Esri/geometry-api-java/master/license.txt)
