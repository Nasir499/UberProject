FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY UberReviewService/build/libs/*.jar app.jar
EXPOSE 7475
ENTRYPOINT ["java", "-jar", "app.jar"]
