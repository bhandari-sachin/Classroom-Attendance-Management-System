# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy poms first for better caching
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY frontend/pom.xml frontend/pom.xml

# Cache dependencies
RUN mvn -B -ntp dependency:go-offline

# Copy full source and build from root
COPY . .
RUN mvn -B -ntp clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Install Linux GUI libraries needed by JavaFX
RUN apt-get update && apt-get install -y \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgtk-3-0 \
    libgl1 \
    libasound2 \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Download JavaFX SDK
RUN mkdir -p /opt/javafx \
    && wget -O /tmp/javafx.zip https://download2.gluonhq.com/openjfx/21/openjfx-21_linux-x64_bin-sdk.zip \
    && unzip /tmp/javafx.zip -d /opt/javafx \
    && mv /opt/javafx/javafx-sdk-21 /opt/javafx/sdk \
    && rm -f /tmp/javafx.zip

# Copy built frontend jar
COPY --from=builder /app/frontend/target/frontend.jar app.jar

# Send GUI to VcXsrv on Windows host
ENV DISPLAY=host.docker.internal:0.0

# Run JavaFX app
CMD ["java", "--module-path", "/opt/javafx/sdk/lib", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "app.jar"]