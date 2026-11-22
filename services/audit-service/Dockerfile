FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY contracts/pom.xml contracts/
COPY contracts/src contracts/src
RUN mvn install -N -DskipTests && mvn -f contracts/pom.xml install -DskipTests

COPY services/audit-service/pom.xml services/audit-service/
COPY services/audit-service/src services/audit-service/src
RUN mvn -f services/audit-service/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /workspace/services/audit-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
