# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY core core
COPY identity-follow identity-follow
COPY tweets tweets
COPY messaging messaging
COPY timeline timeline
COPY agent-management agent-management
COPY app app
RUN --mount=type=cache,id=nexus-maven-repository,target=/root/.m2,sharing=locked mvn -pl app -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/app/target/app-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]