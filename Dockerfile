# Étape 1 : Build de l'application avec Maven
FROM maven:3.9-eclipse-temurin-11 AS builder
WORKDIR /app

# Copier les fichiers Maven
COPY pom.xml .
COPY src ./src

# Compiler l'application
RUN mvn clean package -DskipTests

# Étape 2 : Image finale légère avec Java
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

# Copier le JAR compilé depuis l'étape de build
COPY --from=builder /app/target/*.jar app.jar

# Exposer le port (ajustez selon votre application)
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
