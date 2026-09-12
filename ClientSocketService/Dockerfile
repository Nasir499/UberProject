FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY ClientSocketService/build/libs/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
