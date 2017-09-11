FROM us.gcr.io/echoparklabs/proj.4:latest-debian-slim as builder

COPY ./ /opt/src/geometry-api-java

WORKDIR /opt/src/geometry-api-java

RUN ./gradlew build install



FROM us.gcr.io/echoparklabs/proj.4:latest-debian-slim

WORKDIR /opt/src/geometry-api-java/build/libs

COPY --from=builder /opt/src/geometry-api-java/build/libs .

#TODO, I should be able to make a test image and copy from that, right?
WORKDIR /opt/src/geometry-api-java/build/test-results
COPY --from=builder /opt/src/geometry-api-java/build/test-results .
#COPY ./build/resources/main /opt/src/geometry-api-java/build/resources/main

# TODO how to create an 'latest-apline' as well?

