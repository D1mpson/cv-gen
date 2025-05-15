FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests=true

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/cv-generator-0.0.1-SNAPSHOT.jar app.jar

# Створюємо директорії для файлів
RUN mkdir -p /app/uploads/photos && chmod -R 777 /app/uploads

# Встановлюємо змінну середовища для активації профілю production
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
CMD ["java", "-Xmx256m", "-Xms128m", "-XX:+UseSerialGC", "-XX:MaxRAM=300m", "-Djava.awt.headless=true", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]