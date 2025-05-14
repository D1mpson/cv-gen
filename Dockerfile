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
# Використовуємо команду запуску з аргументами Spring Boot
CMD ["java", "-noverify", "-XX:TieredStopAtLevel=1", "-Dspring.jmx.enabled=false", "-Dspring.config.location=classpath:/application.properties,classpath:/application-prod.properties", "-jar", "/app/app.jar", "--spring.jmx.enabled=false"]