# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY backend/pom.xml backend/pom.xml
COPY frontend/pom.xml frontend/pom.xml

COPY common/src common/src
COPY frontend/src frontend/src

RUN mvn -pl frontend -am clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/frontend/target/frontend.jar app.jar

ENV DISPLAY=host.docker.internal:0.0
ENV LIBGL_ALWAYS_INDIRECT=1

ENTRYPOINT ["java", "-jar", "app.jar"]