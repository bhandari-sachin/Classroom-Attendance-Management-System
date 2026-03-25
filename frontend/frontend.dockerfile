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
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install minimal JavaFX GUI dependencies
RUN apt-get update && apt-get install -y \
    libgtk-3-0 \
    libgl1-mesa-glx \
    libglib2.0-0 \
    libasound2 \
    libpulse0 \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Download JavaFX SDK
RUN mkdir -p /javafx-sdk \
    && wget -O javafx.zip https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_linux-x64_bin-sdk.zip \
    && unzip javafx.zip -d /javafx-sdk \
    && mv /javafx-sdk/javafx-sdk-21.0.2/lib /javafx-sdk/lib \
    && rm -rf /javafx-sdk/javafx-sdk-21.0.2 javafx.zip

# Copy fat JAR
COPY --from=build /app/frontend/target/frontend.jar app.jar

# WSLg will provide DISPLAY automatically → do NOT hardcode it
ENV BACKEND_URL=http://backend:8081

# Run JavaFX app
CMD ["java", \
"--module-path", "/javafx-sdk/lib", \
"--add-modules", "javafx.controls,javafx.graphics,javafx.base,javafx.swing", \
"-jar", "app.jar"]
