FROM us.gcr.io/echoparklabs/proj.4:latest

COPY ./ /opt/src/geometry-api-java

WORKDIR /opt/src/geometry-api-java

RUN ./gradlew build install
