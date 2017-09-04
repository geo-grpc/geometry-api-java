FROM us.gcr.io/echoparklabs/proj.4:latest

COPY ./ /opt/src/geometry-api-java

WORKDIR /opt/src/geometry-api-java

RUN ./gradlew build

RUN ./gradlew build install

#RUN chmod +x /opt/src/geometry-service-java/geometry-service/bin/geometry-operators-server
#
#EXPOSE 8980
#
#CMD ["/opt/src/geometry-service-java/geometry-service/bin/geometry-operators-server"]