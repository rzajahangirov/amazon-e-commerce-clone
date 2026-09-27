# ==========================================
# Stage 1: Build Application with Maven & Java 21
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache Maven dependencies layer first
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source tree and compile/package runnable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Production JRE Alpine Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create static uploads directory and non-root system user for security
RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/uploads && \
    chown -R spring:spring /app

USER spring:spring

# Copy built executable JAR from builder stage
COPY --from=builder --chown=spring:spring /build/target/*.jar app.jar

# Render assigns dynamic PORT environment variable (default fallback: 8080)
EXPOSE 8080

ENV PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["java", "-jar", "app.jar"]
