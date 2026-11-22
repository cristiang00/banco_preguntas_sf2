FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY contracts/pom.xml contracts/
COPY contracts/src contracts/src
RUN mvn install -N -DskipTests && mvn -f contracts/pom.xml install -DskipTests

COPY services/users-service/pom.xml services/users-service/
COPY services/users-service/src services/users-service/src
RUN mvn -f services/users-service/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /workspace/services/users-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
