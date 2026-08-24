FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY UberProject-LocationService/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 7478
ENTRYPOINT ["java", "-jar", "app.jar"]

