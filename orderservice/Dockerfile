FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/orderservice-0.0.1-SNAPSHOT.jar orderservice.jar
ENTRYPOINT ["java", "-jar", "orderservice.jar"]
