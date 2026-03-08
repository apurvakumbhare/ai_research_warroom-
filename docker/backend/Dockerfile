# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build jar
COPY src ./src
RUN mvn clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Add a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the built JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Tune JVM for container environment
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
