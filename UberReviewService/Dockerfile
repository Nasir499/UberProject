FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY UberReviewService/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 7475
ENTRYPOINT ["java", "-jar", "app.jar"]

