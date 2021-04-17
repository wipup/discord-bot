FROM maven:3.6.3-jdk-8-slim as builder
WORKDIR /tmp/maven
COPY . /tmp/maven
RUN mvn clean package


FROM adoptopenjdk:8-hotspot
COPY --from=builder /tmp/maven/target/discord-bot-1.0.0.jar /application.jar
COPY --from=builder /tmp/maven/src/main/resources/audio/ /audio/
CMD ["java", "-jar", "/application.jar"] 