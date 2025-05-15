FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Створення директорій для завантажень та логів
RUN mkdir -p /app/uploads/photos
RUN mkdir -p /app/logs
RUN chmod -R 777 /app/uploads
RUN chmod -R 777 /app/logs

# Змінна середовища для активації prod профілю
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]