# 설명

- Spring 기반으로 운영되는 모임(Crew) 서버
- [PlayGround Link](https://playground.sopt.org/group/)

# 배경

- Crew 서비스는 사람들을 모으고 융합하는 데에 목적을 두고있습니다.
- Crew 서비스는 Playground와 같은 Front-end Domain으로 운영되고 있지만, 서버는 Crew와 Playground를 분리하여 운영하고 있습니다.
- PlayGround에서 발급하는 token을 가져와서 Playground 사용자의 정보를 이용해 Crew의 사용자로 재등록하고 있습니다.

# 기술스택

- DB: PostgreSQL
- ORM: jpa
- API 문서: Swagger
- 배포: AWS EC2, Docker Compose, Docker hub
- 인증: JWT
- 테스트: JUnit5, Jest 
- 서버 프레임워크: Spring
- 웹서버 프레임워크: Caddy
- 언어: Java

# 아키텍처

## flow

1. Caddy를 통해 HTTPS를 적용하고, HTTP로 들어오는 요청을 HTTPS로 리다이렉트한다.
2. HTTPS로 들어온 요청을 Caddy가 받아서, Caddy는 요청을 받은 후에 해당 요청을 NestJS/Spring 서버로 리버스프록시한다.
3. NestJS/Spring 서버는 요청을 받아서, 요청에 맞는 Controller를 찾아서 해당 Controller에서 Service를 호출한다.
4. Service에서 로직을 처리한 후에, Repository를 통해 DB에 접근한다.
5. Repository는 DB에 접근해서 데이터를 가져온 후에, Service에게 데이터를 전달한다.
6. Service는 Repository로부터 받은 데이터를 가공해서 Controller에게 전달한다.
7. Controller는 Service로부터 받은 데이터를 가공해서 Client에게 전달한다.

# 개발

## 환경 변수

- 환경 변수는 dev/prod 환경에 따라 다르게 설정되어야 한다.
  - Spring
    - dev 환경: application-dev.yml
    - prod 환경: application-prod.yml
    - application-secret.properties: secret key를 관리한다.

## API 문서

- `/api-docs`로 접속하면 볼 수 있다.
- [Crew API Docs - Dev](https://crew.api.dev.sopt.org/docs)
- [Crew API Docs - Prod](https://crew.api.prod.sopt.org/docs)
- [Playground API Docs - Dev](https://playground.dev.sopt.org/swagger-ui/index.html)

## 인증

- auth 디렉토리에 참조
- Playground에서 accessToken을 발급하면, 해당 accessToken을 받아서 Playground API에 요청 후 해당 결과를 Crew DB에 저장.
- 회원가입이 존재하지 않음
- [Dev환경에서 accessToken을 확인하는 방법](https://www.notion.so/sopt-makers/aff60effd3b342b7a8bc8090ddc4a652?pvs=4)

# 배포

## 배포 전략

- Blue-Green 방식의 배포 자동화를 구축했다.
- 그럼에도 불구하고, 사용자가 몰리는 시간대는 피해서 배포를 진행한다. (Prod환경 기준)
- Prod환경의 경우는 `main` 브랜치를, Dev환경의 경우는 `develop` 브랜치를 기준으로 배포를 진행한다.

## 배포 정보

- 배포 서버: AWS EC2
- 배포 툴: Docker Compose

## 수동 배포 방법
- 현재는 배포 자동화가 되어있다.

```bash
# Prod 배포
$ sudo docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build

# Dev 배포
$ sudo docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build

# 배포 후 로그 확인
$ sudo docker compose logs -f -t 10

# 배포 후 컨테이너 상태 확인
$ sudo docker compose ps -a

# 배포 후 사용하지 않는 이미지 자동 삭제
$ sudo docker image prune
```

# 협업

## Git

1. 이슈를 생성한다.
2. 이슈를 기반으로 브랜치를 생성한다.
   - ex: `feature/crew-1`
3. 브랜치를 생성한 후에 작업을 진행한다.
4. 진행한 후에 커밋을 한다.
5. 작업이 완료되면 PR을 생성한다.
6. PR을 생성한 후에 팀원들에게 리뷰를 요청한다.
7. 리뷰를 받은 후에 PR을 develop에 merge한다.
8. develop에 merge된 후에 develop환경 배포를 진행한다.

## 🙏 Commit Convention
- <a href="https://udacity.github.io/git-styleguide/">유다시티 컨벤션

```
feat: 새로운 기능 구현
add: 기능구현까지는 아니지만 새로운 파일이 추가된 경우
del: 기존 코드를 삭제한 경우
fix: 버그, 오류 해결
docs: README나 WIKI 등의 문서 작업
style: 코드가 아닌 스타일 변경을 하는 경우
refactor: 리팩토링 작업
test: 테스트 코드 추가, 테스트 코드 리팩토링
chore: 코드 수정, 내부 파일 수정
```

## 기여
