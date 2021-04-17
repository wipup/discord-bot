FROM maven:3.6.3-jdk-8-slim

COPY . .
RUN mvn clean package


FROM openjdk:8-alpine

RUN pwd
RUN ls -l
RUN cat pom.xml
COPY target/discord-bot-1.0.0.jar /application.jar
COPY src/main/resources/audio/ /audio/
CMD ["java", "-jar", "/application.jar"] 