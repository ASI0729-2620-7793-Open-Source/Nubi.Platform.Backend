# Imagen del backend de Nubi para Render (u otro servicio que ejecute contenedores).
# Etapa 1: compila el jar con el Maven Wrapper del proyecto (Java 25, como el pom).
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# sed: por si mvnw se editó en Windows con fin de línea CRLF
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -B -DskipTests package && cp target/*.jar app.jar

# Etapa 2: solo el JRE y el jar, para una imagen liviana.
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --create-home nubi
USER nubi

COPY --from=build /app/app.jar app.jar

# Render indica el puerto en la variable PORT (application.properties la lee).
ENV PORT=8080
EXPOSE 8080

# Memoria acotada para el plan gratuito de Render (512 MB).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
ENTRYPOINT ["java", "-jar", "app.jar"]
