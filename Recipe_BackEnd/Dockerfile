FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package


FROM curlimages/curl:8.14.1 AS insights-agent

ARG APPLICATIONINSIGHTS_AGENT_VERSION=3.7.8

RUN curl -fSL \
    "https://github.com/microsoft/ApplicationInsights-Java/releases/download/${APPLICATIONINSIGHTS_AGENT_VERSION}/applicationinsights-agent-${APPLICATIONINSIGHTS_AGENT_VERSION}.jar" \
    -o /tmp/applicationinsights-agent.jar


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=insights-agent /tmp/applicationinsights-agent.jar applicationinsights-agent.jar
COPY applicationinsights.json applicationinsights.json

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/app/applicationinsights-agent.jar", "-jar", "/app/app.jar"]
