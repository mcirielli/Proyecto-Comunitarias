FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
# Compilar y ejecutar todas las pruebas antes de empaquetar.
RUN mvn --batch-mode --no-transfer-progress clean verify

FROM eclipse-temurin:21.0.12.1_1-jre-alpine-3.23 AS runtime
# curl se usa únicamente para el healthcheck HTTP del contenedor.
RUN apk add --no-cache curl \
    && addgroup -S comunitarias \
    && adduser -S -G comunitarias comunitarias
WORKDIR /app
COPY --from=build --chown=comunitarias:comunitarias /app/target/*.jar ./app.jar
USER comunitarias
ENV SPRING_PROFILES_ACTIVE=dev
ENV SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
