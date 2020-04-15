FROM openjdk:8-alpine
MAINTAINER "Ukange Ushahemba <ukange1984@gmail.com>"
# Define working directory.
WORKDIR /work
ADD target/jtb-integration-service-0.0.1-SNAPSHOT.jar /work/jtb-integration-service-0.0.1-SNAPSHOT.jar
# Expose Ports
EXPOSE 8081
#EXPOSE 8443
ENTRYPOINT exec java -jar /work/jtb-integration-service-0.0.1-SNAPSHOT.jar --spring.config.location=/properties/application.properties