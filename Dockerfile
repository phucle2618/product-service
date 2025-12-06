FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests dependency:go-offline
# Open port
EXPOSE 8080
# Run with devtools
CMD ["mvn", "spring-boot:run"]
