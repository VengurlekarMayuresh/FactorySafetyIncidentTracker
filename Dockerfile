# =======================================================
# Stage 1: Build Application with Maven & JDK 17
# =======================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy dependency configuration and pre-fetch plugins/dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy project source and package the JAR (skipping tests for swift builds)
COPY src ./src
RUN mvn clean package -DskipTests -B

# =======================================================
# Stage 2: Lean JRE 17 Runtime for Production (Render)
# =======================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Ensure directories for H2 file database and local storage fallback exist
RUN mkdir -p /app/data /app/uploads

# Copy built artifact from builder stage
COPY --from=builder /app/target/incident-tracker-*.jar app.jar

# Render injects $PORT environment variable automatically
ENV PORT=8080
EXPOSE 8080

# Production container flags: respect cgroup memory limits (Render Free tier: 512MB)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
