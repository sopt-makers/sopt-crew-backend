#!/bin/sh
# SPRING_PROFILES_ACTIVE에 따라 Pinpoint Agent 설정 변경
if [ "$SPRING_PROFILES_ACTIVE" = "dev" ]; then
    export AGENT_ID="dev-agent"
    export APPLICATION_NAME="spring-dev"
elif [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
    export AGENT_ID="prod-agent"
    export APPLICATION_NAME="spring-prod"
else
    export AGENT_ID="agent-in-docker"
    export APPLICATION_NAME="spring"
fi

# JAVA_OPTS 구성
export JAVA_OPTS="-javaagent:/pinpoint-agent/pinpoint-bootstrap-3.0.1.jar \
-Dpinpoint.agentId=$AGENT_ID \
-Dpinpoint.applicationName=$APPLICATION_NAME \
-Dpinpoint.profiler.profiles.active=release"

echo "Using AGENT_ID=$AGENT_ID and APPLICATION_NAME=$APPLICATION_NAME"

# 애플리케이션 실행
exec java $JAVA_OPTS -jar /app/app.jar
