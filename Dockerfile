# Step 1: Use a lightweight JDK base image
FROM openjdk:17-jdk-alpine

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Copy the JAR file from the host machine into the container
COPY target/Project-Manger-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Expose port 8080 (the default Spring Boot port)
EXPOSE 8080

# Step 5: Define the command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
