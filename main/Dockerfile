FROM openjdk:17-jdk-slim AS build
WORKDIR /app
COPY . /app
RUN ./gradlew build -x test

ENV JAVA_OPTS="-javaagent:/pinpoint-agent/pinpoint-bootstrap-2.5.1.jar -Dpinpoint.agentId=app-in-docker -Dpinpoint.applicationName=monitorapp -Dpinpoint.profiler.profiles.active=release"

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]