FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY UberProject-AuthService/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 7474
ENTRYPOINT ["java", "-jar", "app.jar"]

