FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY contracts/pom.xml contracts/
COPY contracts/src contracts/src
RUN mvn install -N -DskipTests && mvn -f contracts/pom.xml install -DskipTests

COPY services/validation-service/pom.xml services/validation-service/
COPY services/validation-service/src services/validation-service/src
RUN mvn -f services/validation-service/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /workspace/services/validation-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
