# -------------------------------
# Stage 1: Build the JAR
# -------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy Maven files first (for better caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies offline (faster on rebuild)
RUN ./mvnw dependency:go-offline -B

# Copy source and package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# -------------------------------
# Stage 2: Create runtime image
# -------------------------------
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/carbon-tracker-api-0.0.1-SNAPSHOT.jar app.jar

# Expose API port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
