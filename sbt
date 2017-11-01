#!/bin/bash

SBT_VERSION=0.13.11
SBT_JAR_MD5=f6242d8097ac039ed3b32a6f7ba46343

root=$(
  cd $(dirname $(readlink $0 || echo $0))/..
  /bin/pwd
)

sbtjar=sbt-launch.jar

is_valid_jar() {
  md5=$(openssl md5 < $sbtjar|cut -f2 -d'='|awk '{print $1}')
  if [ "${md5}" = ${SBT_JAR_MD5} ]; then
    return 0
  else
    return 1
  fi
}

if [ ! -f $sbtjar ] || ! is_valid_jar; then
  echo 'downloading '$sbtjar 1>&2
  curl -L -o $sbtjar http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/${SBT_VERSION}/sbt-launch.jar

  test -f $sbtjar || exit 1
  if ! is_valid_jar; then
    echo 'bad sbtjar!' 1>&2
    exit 1
  fi
fi


test -f ~/.sbtconfig && . ~/.sbtconfig

java -ea                          \
  $SBT_OPTS                       \
  $JAVA_OPTS                      \
  -Dsbt.ivy.home=$HOME/.ivy2/     \
  -Djava.net.preferIPv4Stack=true \
  -XX:+AggressiveOpts             \
  -XX:+UseParNewGC                \
  -XX:+UseConcMarkSweepGC         \
  -XX:+CMSParallelRemarkEnabled   \
  -XX:+CMSClassUnloadingEnabled   \
  -XX:MaxPermSize=1024m           \
  -XX:SurvivorRatio=128           \
  -XX:MaxTenuringThreshold=0      \
  -XX:ReservedCodeCacheSize=128m  \
  -Xss8M                          \
  -Xms512M                        \
  -Xmx2G                          \
  -server                         \
  -jar $sbtjar "$@"
