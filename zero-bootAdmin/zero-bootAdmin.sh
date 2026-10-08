#!/bin/bash

APP_NAME=zero-bootAdmin-1.0.0.jar
PROFILE=${SPRING_PROFILES_ACTIVE:-test}
JAVA_OPTS=${JAVA_OPTS:-"-Xms1048m -Xmx2048m"}

usage() {
  echo "Usage: sh zero-bootAdmin.sh [start|stop|restart|status]"
  exit 1
}

is_exist() {
  pid=$(ps -ef | grep "$APP_NAME" | grep -v grep | awk '{print $2}')
  if [ -z "${pid}" ]; then
    return 1
  fi
  return 0
}

start() {
  is_exist
  if [ $? -eq 0 ]; then
    echo "${APP_NAME} is already running, pid=${pid}."
  else
    nohup java ${JAVA_OPTS} -jar "${APP_NAME}" --spring.profiles.active="${PROFILE}" > /dev/null 2>&1 &
    echo "${APP_NAME} started, profile=${PROFILE}."
  fi
}

stop() {
  is_exist
  if [ $? -eq 0 ]; then
    kill -9 "$pid"
    echo "${APP_NAME} stopped."
  else
    echo "${APP_NAME} is not running."
  fi
}

status() {
  is_exist
  if [ $? -eq 0 ]; then
    echo "${APP_NAME} is running, pid=${pid}."
  else
    echo "${APP_NAME} is not running."
  fi
}

restart() {
  stop
  start
}

case "$1" in
  "start")
    start
    ;;
  "stop")
    stop
    ;;
  "status")
    status
    ;;
  "restart")
    restart
    ;;
  *)
    usage
    ;;
esac
