# ---------- 1️⃣  Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy the rest of the source and build
COPY src ./src
RUN mvn -B clean package -DskipTests        # produces /app/target/*.jar

# ---------- 2️⃣  Runtime stage ----------
FROM eclipse-temurin:17-jre                 # slim JRE‑only image
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
