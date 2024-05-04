#!/bin/bash
export $(grep -v '^#' docker/.env | sed 's/ *= */=/g' | xargs)

IS_GREEN_EXIST=$(sudo docker ps | grep green)
DIR=~/docker

SPRING_GREEN_PORT=4001
SPRING_BLUE_PORT=4002
NESTJS_GREEN_PORT=3001
NESTJS_BLUE_PORT=3002
DOCKERHUB_PASSWORD=$DOCKERHUB_PASSWORD

SPRING_GREEN_NAME="spring-green"
SPRING_BLUE_NAME="spring-blue"
NESTJS_GREEN_NAME="nestjs-green"
NESTJS_BLUE_NAME="nestjs-blue"


# green up
if [ -z $IS_GREEN_EXIST ];then
  echo "### BLUE -> GREEN ####"
  echo ">>> pull green image"

  sudo docker login -u makerscrew -p DOCKERHUB_PASSWORD

  (cd $DIR && sudo docker compose pull caddy)
  (cd $DIR && sudo docker compose pull swagger)
  (cd $DIR && sudo docker compose pull nestjs-green)
  (cd $DIR && sudo docker compose pull spring-green)

  (cd $DIR && sed -i "s/^SPRING_NAME=.*/SPRING_NAME=${SPRING_GREEN_NAME}/" .env)
  (cd $DIR && sed -i "s/^NESTJS_NAME=.*/NESTJS_NAME=${NESTJS_GREEN_NAME}/" .env)

  (cd $DIR && sudo docker network create caddy)

  sleep 5

  echo ">>> up green container"
  (cd $DIR && sudo docker compose --env-file .env -f docker-compose.yml -f docker-compose.prod.yml up caddy nestjs-green spring-green swagger -d --build)

  while [ 1 = 1 ]; do
    echo ">>> nestjs green health check ..."
    sleep 3
    STATUS_CODE_NESTJS=$(curl -o /dev/null -s -w "%{http_code}" localhost:3001/)
    if [ "$STATUS_CODE_NESTJS" -eq 200 ]; then
      echo ">>> nestjs health check success !"
      break;
    fi
  done;


  while [ 1 = 1 ]; do
    echo ">>> spring green health check ..."
    sleep 3
    STATUS_CODE_SPRING=$(curl -o /dev/null -s -w "%{http_code}" localhost:4001/health/v2)
    if [ "$STATUS_CODE_SPRING" -eq 200 ]; then
      echo ">>> spring health check success !"
      break;
    fi
  done;

  sleep 3
  echo ">>> down blue container"
  (cd $DIR && sudo docker compose stop spring-blue)
  (cd $DIR && sudo docker compose stop nestjs-blue)

  (cd $DIR && sudo docker compose rm -f spring-blue)
  (cd $DIR && sudo docker compose rm -f nestjs-blue)

# blue up
else
  echo "### GREEN -> BLUE ###"
  echo ">>> pull blue image"

  sudo docker login -u makerscrew -p DOCKERHUB_PASSWORD

  (cd $DIR && sudo docker compose pull caddy)
  (cd $DIR && sudo docker compose pull swagger)
  (cd $DIR && sudo docker compose pull nestjs-blue)
  (cd $DIR && sudo docker compose pull spring-blue)

  (cd $DIR && sed -i "s/^SPRING_NAME=.*/SPRING_NAME=${SPRING_BLUE_NAME}/" .env)
  (cd $DIR && sed -i "s/^NESTJS_NAME=.*/NESTJS_NAME=${NESTJS_BLUE_NAME}/" .env)

  (cd $DIR && sudo docker network create caddy)

  sleep 5

  echo ">>> up blue container"
  (cd $DIR && sudo docker compose --env-file .env -f docker-compose.yml -f docker-compose.prod.yml up caddy nestjs-blue spring-blue swagger -d --build)


  while [ 1 = 1 ]; do
    echo ">>> nestjs blue health check ..."
    sleep 3
    STATUS_CODE_NESTJS=$(curl -o /dev/null -s -w "%{http_code}" localhost:3002/)
    if [ "$STATUS_CODE_NESTJS" -eq 200 ]; then
      echo ">>> nestjs health check success !"
      break;
    fi
  done;


  while [ 1 = 1 ]; do
    echo ">>> spring blue health check ..."
    sleep 3
    STATUS_CODE_SPRING=$(curl -o /dev/null -s -w "%{http_code}" localhost:4002/health/v2)
    if [ "$STATUS_CODE_SPRING" -eq 200 ]; then
      echo ">>> spring health check success !"
      break;
    fi
  done;

  sleep 3
  echo ">>> down green container"
  (cd $DIR && sudo docker compose stop spring-green)
  (cd $DIR && sudo docker compose stop nestjs-green)

  (cd $DIR && sudo docker compose rm -f spring-green)
  (cd $DIR && sudo docker compose rm -f spring-green)
fi

sudo docker image prune -f