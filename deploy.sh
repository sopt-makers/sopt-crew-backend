#!/bin/bash

IS_GREEN_EXIST=$(sudo docker ps | grep green)
DIR=~/docker

SPRING_GREEN_PORT=4001
SPRING_BLUE_PORT=4002
NESTJS_GREEN_PORT=3001
NESTJS_BLUE_PORT=3002

SPRING_GREEN_NAME="spring-green"
SPRING_BLUE_NAME="spring-blue"
NESTJS_GREEN_NAME="nestjs-green"
NESTJS_BLUE_NAME="nestjs-blue"


# green up
if [ -z $IS_GREEN_EXIST ];then
  echo "### BLUE -> GREEN ####"
  echo ">>> pull green image"

  (cd $DIR && sed -i "s/^SPRING_PORT=.*/SPRING_PORT=${SPRING_GREEN_PORT}/" .env)
  (cd $DIR && sed -i "s/^NESTJS_PORT=.*/NESTJS_PORT=${NESTJS_GREEN_PORT}/" .env)
  (cd $DIR && sed -i "s/^SPRING_NAME=.*/SPRING_NAME=${SPRING_GREEN_NAME}/" .env)
  (cd $DIR && sed -i "s/^NESTJS_NAME=.*/NESTJS_NAME=${NESTJS_GREEN_NAME}/" .env)

  echo ">>> TEST >>> "
  (cd $DIR && sudo docker compose -f docker-compose.yml -f docker-compose.dev.yml up caddy nestjs-green spring-green swagger -d --build)
 # (cd $DIR && sudo docker compose -f docker-compose.yml -f docker-compose.dev.yml up nextjs-green -d --build)
# (cd $DIR && sudo docker compose -f docker-compose.yml -f docker-compose.dev.yml up spring-green -d --build)
#  (cd $DIR && sudo docker compose -f docker-compose.yml -f docker-compose.dev.yml up swagger -d --build)

  echo ">>> up green container"

  while [ 1 = 1 ]; do
    echo ">>> spring green health check ..."
    sleep 3
    REQUEST_SPRING=$(curl http://127.0.0.1:4001/actuator/health)
    if [ -n "$REQUEST_SPRING" ]; then
      echo ">>> spring health check success !"
      break;
    fi
  done;

   while [ 1 = 1 ]; do
    echo ">>> nextjs green health check ..."
    sleep 3
    REQUEST_NESTJS=$(curl http://127.0.0.1:3001/actuator/health)
    if [ -n "$REQUEST_NESTJS" ]; then
      echo ">>> nestjs health check success !"
      break;
    fi
  done;

  sleep 3
  echo ">>> down blue container"
  (cd $DIR && docker-compose stop spring-blue)
  (cd $DIR && docker-compose stop nestjs-blue)

# blue up
else
  echo "### GREEN -> BLUE ###"
  echo ">>> pull blue image"

  (cd $DIR && sed -i "s/^SPRING_PORT=.*/SPRING_PORT=${SPRING_BLUE_PORT}/" .env)
  (cd $DIR && sed -i "s/^NESTJS_PORT=.*/NESTJS_PORT=${NESTJS_BLUE_PORT}/" .env)
  (cd $DIR && sed -i "s/^SPRING_NAME=.*/SPRING_NAME=${SPRING_BLUE_NAME}/" .env)
  (cd $DIR && sed -i "s/^NESTJS_NAME=.*/NESTJS_NAME=${NESTJS_BLUE_NAME}/" .env)

  (cd $DIR && sudo docker compose -f docker-compose.yml -f docker-compose.dev.yml up caddy -d --build)
  echo ">>> TEST >>> "
  (cd $DIR && sudo docker compose -f docker-compose.yml -f docker-compose.dev.yml up nestjs-blue -d --build)
  (cd $DIR && sudo docker compose -f docker-compose.yml -f docker-compose.dev.yml up spring-blue -d --build)
  (cd $DIR && sudo docker compose -f docker-compose.yml -f docker-compose.dev.yml up swagger -d --build)
  echo ">>> up blue container"

   while [ 1 = 1 ]; do
    echo ">>> spring blue health check ..."
    sleep 3
    REQUEST_SPRING=$(curl http://127.0.0.1:4002/actuator/health)
    if [ -n "$REQUEST_SPRING" ]; then
      echo ">>> spring health check success !"
      break;
    fi
  done;

   while [ 1 = 1 ]; do
    echo ">>> nestjs blue health check ..."
    sleep 3
    REQUEST_NESTJS=$(curl http://127.0.0.1:3002/actuator/health)
    if [ -n "$REQUEST_NESTJS" ]; then
      echo ">>> nestjs health check success !"
      break;
    fi
  done;

  sleep 3
  echo ">>> down green container"
  (cd $DIR && docker-compose stop spring-green)
  (cd $DIR && docker-compose stop nestjs-green)
fi

docker image prune -f
