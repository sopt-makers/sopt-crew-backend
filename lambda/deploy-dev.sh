#!/bin/bash
#
# AWS Lambda Dev 환경 배포 스크립트
#
# 사용법:
#   cd lambda && ./deploy-dev.sh
#
# 사전 요구사항:
#   - AWS CLI 설치 및 자격 증명 설정 (aws configure)
#   - SAM CLI 설치 (brew install aws-sam-cli)
#   - application-secret.properties 파일 존재

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
MAIN_DIR="$PROJECT_ROOT/main"
LAMBDA_DIR="$SCRIPT_DIR"
SECRET_FILE="$MAIN_DIR/src/main/resources/application-secret.properties"

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step() {
    echo -e "\n${BLUE}==>${NC} ${GREEN}$1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_step "Step 1: 사전 요구사항 확인"

if ! command -v aws &> /dev/null; then
    print_error "AWS CLI가 설치되어 있지 않습니다."
    echo "설치: brew install awscli"
    exit 1
fi
echo "  ✓ AWS CLI 설치됨"

if ! command -v sam &> /dev/null; then
    print_error "SAM CLI가 설치되어 있지 않습니다."
    echo "설치: brew install aws-sam-cli"
    exit 1
fi
echo "  ✓ SAM CLI 설치됨: $(sam --version)"

if ! aws sts get-caller-identity &> /dev/null; then
    print_error "AWS 자격 증명이 설정되지 않았습니다."
    echo "실행: aws configure 또는 aws sso login"
    exit 1
fi
echo "  ✓ AWS 자격 증명 확인됨"

if [ ! -f "$SECRET_FILE" ]; then
    print_error "application-secret.properties 파일이 없습니다!"
    echo ""
    echo "파일 위치: $SECRET_FILE"
    echo ""
    echo "이 파일은 DB, Redis 등의 민감한 설정을 포함합니다."
    echo "팀원에게 파일을 받거나 GitHub Secrets에서 복사하세요."
    exit 1
fi
echo "  ✓ application-secret.properties 존재"

if [ ! -f "$LAMBDA_DIR/template-dev.yaml" ]; then
    print_error "template-dev.yaml 파일이 없습니다!"
    exit 1
fi
echo "  ✓ template-dev.yaml 존재"

if [ ! -f "$LAMBDA_DIR/samconfig.toml" ]; then
    print_error "samconfig.toml 파일이 없습니다!"
    exit 1
fi
echo "  ✓ samconfig.toml 존재"

print_step "Step 2: Lambda JAR 빌드"

cd "$MAIN_DIR"
chmod +x ./gradlew

echo "  빌드 중... (약 10-20초 소요)"
./gradlew clean lambdaJar -x test --quiet

ZIP_FILE="$MAIN_DIR/build/distributions/main-lambda.zip"
if [ ! -f "$ZIP_FILE" ]; then
    print_error "Lambda ZIP 파일이 생성되지 않았습니다!"
    echo "예상 경로: $ZIP_FILE"
    ls -la "$MAIN_DIR/build/distributions/" 2>/dev/null || echo "distributions 디렉토리가 없습니다."
    exit 1
fi

ZIP_SIZE=$(du -h "$ZIP_FILE" | cut -f1)
print_success "빌드 완료: main-lambda.zip ($ZIP_SIZE)"

print_step "Step 3: AWS Lambda 배포"

cd "$LAMBDA_DIR"
echo "  SAM deploy 실행 중..."
echo ""

# samconfig.toml의 [dev.deploy.parameters] 섹션 사용
# S3 업로드는 SAM이 자동으로 처리 (resolve_s3 = true)
sam deploy \
    --template-file template-dev.yaml \
    --config-env dev \
    --no-confirm-changeset \
    --no-fail-on-empty-changeset

print_step "Step 4: 배포 결과 확인"

STACK_NAME="sopt-crew-dev"
REGION="ap-northeast-2"

API_ENDPOINT=$(aws cloudformation describe-stacks \
    --stack-name "$STACK_NAME" \
    --region "$REGION" \
    --query "Stacks[0].Outputs[?OutputKey=='CrewApiEndpoint'].OutputValue" \
    --output text 2>/dev/null)

FUNCTION_ARN=$(aws cloudformation describe-stacks \
    --stack-name "$STACK_NAME" \
    --region "$REGION" \
    --query "Stacks[0].Outputs[?OutputKey=='CrewApiFunctionArn'].OutputValue" \
    --output text 2>/dev/null)

print_step "Step 5: 헬스체크 검증"

CUSTOM_DOMAIN="https://crew.api.dev.sopt.org"

ACTUATOR_PATH=$(grep "^ACTUATOR_PATH=" "$SECRET_FILE" | cut -d'=' -f2 | tr -d '\r')
if [ -z "$ACTUATOR_PATH" ]; then
    print_error "ACTUATOR_PATH를 찾을 수 없습니다."
    echo "  헬스체크를 건너뜁니다."
else
    HEALTH_URL="${ACTUATOR_PATH}/health"

    echo "  Cold Start 대기 중... (10초)"
    sleep 10

    echo "  API Gateway 헬스체크 중..."
    API_HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${API_ENDPOINT}${HEALTH_URL}" --max-time 30 || echo "000")

    if [ "$API_HEALTH_STATUS" = "200" ]; then
        print_success "API Gateway 헬스체크 성공 (HTTP $API_HEALTH_STATUS)"
    else
        print_error "API Gateway 헬스체크 실패 (HTTP $API_HEALTH_STATUS)"
    fi

    echo "  Custom Domain 헬스체크 중..."
    CUSTOM_HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${CUSTOM_DOMAIN}${HEALTH_URL}" --max-time 30 || echo "000")

    if [ "$CUSTOM_HEALTH_STATUS" = "200" ]; then
        print_success "Custom Domain 헬스체크 성공 (HTTP $CUSTOM_HEALTH_STATUS)"
    else
        print_error "Custom Domain 헬스체크 실패 (HTTP $CUSTOM_HEALTH_STATUS)"
    fi
fi

print_step "Step 6: 배포 결과"

echo ""
echo "============================================================"
echo -e "${GREEN}🚀 배포 완료!${NC}"
echo "============================================================"
echo ""
echo -e "${BLUE}API Endpoint:${NC}"
echo "  $API_ENDPOINT"
echo ""
echo -e "${BLUE}Custom Domain:${NC}"
echo "  $CUSTOM_DOMAIN"
echo ""
echo -e "${BLUE}Lambda Function ARN:${NC}"
echo "  $FUNCTION_ARN"
echo ""
echo -e "${BLUE}유용한 링크:${NC}"
echo "  - Swagger UI: ${CUSTOM_DOMAIN}/swagger-ui/index.html"
echo "  - API Docs: ${CUSTOM_DOMAIN}/v3/api-docs"
echo "============================================================"
