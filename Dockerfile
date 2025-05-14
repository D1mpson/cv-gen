FROM openjdk:17-jdk-slim

WORKDIR /app

# Копіюємо JAR-файл
COPY target/*.jar app.jar

# Створюємо директорію для завантажень
RUN mkdir -p /app/uploads/photos

# Визначаємо змінні середовища
ENV SPRING_PROFILES_ACTIVE=prod

# Відкриваємо порт
EXPOSE 8080

# Команда запуску
ENTRYPOINT ["java", "-jar", "/app/app.jar"]