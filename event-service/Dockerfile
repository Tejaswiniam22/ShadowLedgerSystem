FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/event-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
