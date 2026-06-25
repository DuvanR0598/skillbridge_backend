# ─────────────────────────────────────────────────────────────
# Etapa 1: build — compila el JAR con Maven + JDK 17
# ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar primero el pom para cachear la descarga de dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copiar el código y empaquetar (sin tests; los tests necesitan BD)
COPY src ./src
RUN mvn -B clean package -DskipTests

# ─────────────────────────────────────────────────────────────
# Etapa 2: runtime — solo el JRE 17 + el JAR (imagen ligera)
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiar el fat-jar generado en la etapa anterior
COPY --from=build /app/target/skillbridge_backend-0.0.1-SNAPSHOT.jar app.jar

# Carpeta para las imágenes/avatares subidos (se monta un volumen en compose)
RUN mkdir -p /app/uploads

# El puerto efectivo lo define la variable PORT (Render/Railway la inyectan);
# por defecto 8083 en local.
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
