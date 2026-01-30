# =====================================================
# E-Commerce API - Multi-Stage Docker Build
# =====================================================

# Stage 1: Build
# Uses Maven image to compile and package the Spring Boot app
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set working directory inside build container
WORKDIR /app

# Copy Maven configuration first (for dependency caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application JAR (skip tests for faster build)
RUN mvn package -DskipTests -B

# =====================================================
# Stage 2: Runtime
# Uses slim JDK image for smaller final image size
# =====================================================
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create uploads directory with correct permissions
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the JAR file
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose application port
EXPOSE 8080

# Health check - verifies app is responding
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/products || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
