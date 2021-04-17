FROM openjdk:11-alpine

COPY target/*.jar application.jar
COPY src/main/resources/audio/ /audio/

CMD ["java", "-jar", "application.jar"] 