# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy poms first for better caching
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY frontend/pom.xml frontend/pom.xml

# Cache dependencies
RUN mvn -B -ntp dependency:go-offline

# Copy full source and build from ROOT (builds backend first, then frontend)
COPY . .
RUN mvn -B -ntp clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run the JavaFX module jar (frontend)
COPY --from=builder /app/frontend/target/frontend.jar app.jar

CMD ["java", "-jar", "app.jar"]