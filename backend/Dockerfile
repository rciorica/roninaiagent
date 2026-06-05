# Multi-stage Dockerfile for building and running the Ronin backend

# Build stage: use Maven + JDK 21 to build the application
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the current directory (backend/) into the image and build
COPY . /app/
RUN mvn -B -DskipTests clean package

# Runtime stage: lightweight JRE image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the build stage
ARG JAR=target/backend-0.0.1-SNAPSHOT.jar
COPY --from=build /app/${JAR} /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
