# Étape 1 : Build avec Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copier les fichiers Maven
COPY pom.xml .
COPY src ./src

# Compiler l'application
RUN mvn clean package -DskipTests

# Étape 2 : Image finale légère
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copier le JAR compilé depuis l'étape builder
COPY --from=builder /app/target/student-management-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port
EXPOSE 8089

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
