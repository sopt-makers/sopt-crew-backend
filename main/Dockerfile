# Build stage
FROM openjdk:17-jdk-slim AS build
WORKDIR /app
COPY . /app
RUN ./gradlew build -x test

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# 빌드 인자 및 런타임 환경 변수 설정
ARG SPRING_PROFILES_ACTIVE=default
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# entrypoint 스크립트 복사 및 실행 권한 부여
COPY scripts/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# 컨테이너 시작 시 entrypoint 스크립트 실행
ENTRYPOINT ["/app/entrypoint.sh"]
