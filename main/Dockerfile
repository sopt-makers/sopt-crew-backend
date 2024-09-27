FROM openjdk:17-jdk-slim AS build
WORKDIR /app
COPY . /app
RUN ./gradlew build -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ARG SPRING_PROFILES_ACTIVE

# Default values
ENV AGENT_ID="agent-in-docker"
ENV APPLICATION_NAME="spring"

# Set environment variables based on the active profile
RUN if [ "$SPRING_PROFILES_ACTIVE" = "dev" ]; then \
        AGENT_ID="dev-agent"; \
        APPLICATION_NAME="spring-dev"; \
    elif [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then \
        AGENT_ID="prod-agent"; \
        APPLICATION_NAME="spring-prod"; \
    fi && \
    echo "Using AGENT_ID=$AGENT_ID and APPLICATION_NAME=$APPLICATION_NAME"

ENV JAVA_OPTS="-javaagent:/pinpoint-agent/pinpoint-bootstrap-2.5.3.jar -Dpinpoint.agentId=$AGENT_ID -Dpinpoint.applicationName=$APPLICATION_NAME -Dpinpoint.profiler.profiles.active=release"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
