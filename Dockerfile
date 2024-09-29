# Use a Maven image to build the application
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use a lighter JRE image to run the application
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/power-pdf-manager-1.0.0.jar app.jar
RUN mkdir files
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]




