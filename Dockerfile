# Dockerfile para microservicio Spring Boot - Optimizado para Render
FROM openjdk:17-jdk-slim

# Instalar Maven y curl
RUN apt-get update && \
    apt-get install -y curl maven && \
    rm -rf /var/lib/apt/lists/*

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven primero (para cache de dependencias)
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests

# Exponer puerto (Render usa PORT como variable de entorno)
EXPOSE 8083

# Variables de entorno para Render
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_ADDRESS=0.0.0.0
ENV SERVER_PORT=${PORT:-8083}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8083}/actuator/health || exit 1

# Ejecutar la aplicación
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8083} -jar target/UsuariosService-0.0.1-SNAPSHOT.jar"]
