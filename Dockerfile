FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Копіюємо всі файли конфігурації, незалежно від їх розташування
COPY **/application*.properties ./
# Якщо файли в src/main/resources, це також буде працювати завдяки копіюванню src директорії вище
RUN mvn clean package -DskipTests=true

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/cv-generator-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads/photos && chmod -R 777 /app/uploads

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]