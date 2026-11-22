FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
COPY contracts/pom.xml contracts/
COPY contracts/src contracts/src
RUN mvn install -N -DskipTests && mvn -f contracts/pom.xml install -DskipTests

COPY api-gateway/pom.xml api-gateway/
COPY api-gateway/src api-gateway/src
RUN mvn -f api-gateway/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /workspace/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
