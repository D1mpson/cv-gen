FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests=true

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/cv-generator-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads/photos && chmod -R 777 /app/uploads

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
# Повністю вимикаємо всі JMX, метрики та моніторинг
CMD ["java", "-Xmx256m", "-Xms128m", "-XX:+UseSerialGC", "-XX:MaxRAM=300m", "-Djava.awt.headless=true", "-Djava.security.egd=file:/dev/./urandom", "-XX:+DisableAttachMechanism", "-Dcom.sun.management.jmxremote=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false", "-Dcom.sun.management.jmxremote.local.only=true", "-Dspring.jmx.enabled=false", "-Djdk.management.agent.disable=true", "-Djava.rmi.server.hostname=localhost", "-jar", "/app/app.jar"]