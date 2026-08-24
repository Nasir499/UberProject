FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY UberBookingService/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 7777
ENTRYPOINT ["java", "-jar", "app.jar"]

