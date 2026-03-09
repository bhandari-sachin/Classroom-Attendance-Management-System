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

# JavaFX runtime dependencies
RUN apt-get update && apt-get install -y \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgtk-3-0 \
    libgl1-mesa-glx \
    libgl1-mesa-dri \
    mesa-utils \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Download Linux JavaFX SDK
RUN mkdir -p /javafx-sdk \
    && wget -O /tmp/javafx.zip https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_linux-x64_bin-sdk.zip \
    && unzip /tmp/javafx.zip -d /javafx-sdk \
    && mv /javafx-sdk/javafx-sdk-21.0.2/lib /javafx-sdk/lib \
    && rm -rf /javafx-sdk/javafx-sdk-21.0.2 /tmp/javafx.zip

COPY --from=build /app/frontend/target/frontend.jar app.jar

ENV DISPLAY=host.docker.internal:0.0
ENV LIBGL_ALWAYS_INDIRECT=1
ENV BACKEND_URL=http://backend:8081

ENTRYPOINT ["java", "--module-path", "/javafx-sdk/lib", "--add-modules", "javafx.controls,javafx.graphics,javafx.base,javafx.swing", "-jar", "app.jar"]