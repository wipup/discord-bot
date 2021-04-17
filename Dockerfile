FROM maven:3.6.3-jdk-8-slim
COPY . .
RUN mvn clean package


FROM openjdk:8-alpine
COPY target/*.jar application.jar
COPY src/main/resources/audio/ /audio/
CMD ["java", "-jar", "application.jar"] 