FROM us.gcr.io/echoparklabs/proj.4:latest as builder

COPY ./ /opt/src/geometry-api-java

WORKDIR /opt/src/geometry-api-java

RUN ./gradlew build install



FROM us.gcr.io/echoparklabs/proj.4:latest

WORKDIR /opt/src/geometry-api-java/build/libs

COPY --from=builder /opt/src/geometry-api-java/build/libs .
#COPY ./build/resources/main /opt/src/geometry-api-java/build/resources/main

