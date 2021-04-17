FROM openjdk:8-alpine

COPY target/*.jar application.jar
COPY src/main/resources/audio/ /audio/

CMD ["java", "-jar", "application.jar"] 