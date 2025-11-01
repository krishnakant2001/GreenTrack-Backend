# Step 1: Use an official OpenJDK image
FROM eclipse-temurin:21-jdk-alpine

# Step 2: Add metadata (optional)
LABEL maintainer="krishnakantbha@gmail.com"

# Step 3: Create an app directory
WORKDIR /app

# Step 4: Copy your JAR file to the container
COPY target/carbon-tracker-api-0.0.1-SNAPSHOT.jar app.jar

# Step 5: Expose the port your app runs on (e.g. 8080)
EXPOSE 8080

# Step 6: Command to run the jar
ENTRYPOINT ["java","-jar","app.jar"]
