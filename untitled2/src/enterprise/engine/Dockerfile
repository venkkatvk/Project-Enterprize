# Stage 1: Build & Compilation Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Pre-fetch dependencies to maximize Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source and build fat JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production Runtime Stage
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create a restricted non-root system user for security isolation
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled artifact from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Enforce non-root file ownership and execution rights
RUN chown -R appuser:appgroup /app
USER appuser

# Expose internal application HTTP port
EXPOSE 8080

# Launch JVM tuned for container environment memory limits
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]