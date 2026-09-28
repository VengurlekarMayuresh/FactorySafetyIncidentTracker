FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/incident-tracker-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
