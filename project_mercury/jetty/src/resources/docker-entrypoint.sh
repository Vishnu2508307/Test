#!/bin/bash

set -e

if [ "$#" -eq 0 ]; then
  JAVA_OPTS+=("-server -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -XX:+UseG1GC")
  # Java 8u191+ && 10+, -XX:+UseContainerSupport

  # Common settings that can be applied on startup, -XX:InitialRAMPercentage=<float_number_with_1_decimal> -XX:MinRAMPercentage=<float_number_with_1_decimal> -XX:MaxRAMPercentage=<float_number_with_1_decimal>
  JAVA_OPTS+=("-XX:+UseContainerSupport")

  # Force LOCAL_ Consistency Levels
  JAVA_OPTS+=("-DforceLocalCL=1")

  # create dump in case of out of memory error
  if [ $OOM_ERROR_DUMP = "true" ]; then
    JAVA_OPTS+=("-XX:+HeapDumpOnOutOfMemoryError")
  fi

  if [ $SSH_ENABLED = "true" ]; then
    # create user ubuntu for ssh
    useradd -m -d /home/ubuntu ubuntu

    # create new random password for root at every boot
    echo "root:$(pwgen 128 1)" | chpasswd &> /dev/null

    # Create a folder to store user's SSH keys if it does not exist.
    USER_SSH_KEYS_FOLDER=~/.ssh
    UBUNTU_SSH_KEYS_FOLDER=/home/ubuntu/.ssh
    [ ! -d ${USER_SSH_KEYS_FOLDER} ] && mkdir -p ${USER_SSH_KEYS_FOLDER}
    [ ! -d ${UBUNTU_SSH_KEYS_FOLDER} ] && mkdir -p ${UBUNTU_SSH_KEYS_FOLDER}

    # SSHD wipes out environment variables; this means variables set as ENV
    # in the Dockerfile will not be carried across to the shell; this
    # happens to both login and non-login shells.
    # Below greps all the "non-blacklisted" variables and sets them in the .ssh/environment file
    # To enable this functionality, PermitUserEnvironment has to be enabled in sshd_config
    env \
      | egrep -v "^(PUB_SSH_KEY=|HOME=|USER=|MAIL=|LC_ALL=|LS_COLORS=|LANG=|HOSTNAME=|PWD=|TERM=|SHLVL=|LANGUAGE=|_=)" \
      | egrep -v '^$' \
      > ${USER_SSH_KEYS_FOLDER}/environment
    echo ${PUB_SSH_KEY} > ${USER_SSH_KEYS_FOLDER}/authorized_keys

    # Save environment file for ubuntu
    cp ${USER_SSH_KEYS_FOLDER}/environment ${UBUNTU_SSH_KEYS_FOLDER}/environment
    echo ${PUB_SSH_KEY} > ${UBUNTU_SSH_KEYS_FOLDER}/authorized_keys
    chown -R ubuntu:ubuntu ${UBUNTU_SSH_KEYS_FOLDER}

    # Start the SSH daemon
    service ssh restart
  else
    apt-get remove --purge openssh-server -y
    apt-get autoremove -y
  fi

  if [ $APM_ENABLED = "true" ]; then
    JAVA_OPTS+=("-javaagent:/newrelic/newrelic.jar -Dnewrelic.config.distributed_tracing.enabled=true")
    NEW_RELIC_INFINITE_TRACING_TRACE_OBSERVER_HOST="fc4530b0-7b12-4b98-aa93-c7bff5cb542d.aws-us-east-1.tracing.edge.nr-data.net"
    NEW_RELIC_ERROR_COLLECTOR_IGNORE_ERRORS="com.smartsparrow.iam.lang.UnauthorizedFault,com.smartsparrow.exception.NotFoundFault"
    NEW_RELIC_ERROR_COLLECTOR_IGNORE_STATUS_CODES=401

    if [ ! -z $APM_APP_NAME ]; then
      export NEW_RELIC_APP_NAME="${APM_APP_NAME}"
    elif [ ! -z $ENV_REGION ]; then
      export NEW_RELIC_APP_NAME="gpt-bronte-${ENV_REGION}"
    else
      export NEW_RELIC_APP_NAME="gpt-bronte-!Unnamed Environment!"
    fi
  fi

  if [ ! -z $ENV_REGION ]; then
    JAVA_OPTS+=("-Denv.region=$ENV_REGION")
  fi

  if [ ! -z $CASSANDRA_CONTACTPOINTS ]; then
    JAVA_OPTS+=("-Dcassandra.contactPoints=$CASSANDRA_CONTACTPOINTS")
  fi

  if [ ! -z $CASSANDRA_AUTHENTICATION_USERNAME ]; then
    JAVA_OPTS+=("-Dcassandra.authentication.username=$CASSANDRA_AUTHENTICATION_USERNAME")
  fi

  if [ ! -z $CASSANDRA_AUTHENTICATION_PASSWORD ]; then
    JAVA_OPTS+=("-Dcassandra.authentication.password=$CASSANDRA_AUTHENTICATION_PASSWORD")
  fi

  if [ ! -z $CASSANDRA_KEYSTORE_PASSWORD ]; then
    JAVA_OPTS+=("-Dcassandra.keystore.password=$CASSANDRA_KEYSTORE_PASSWORD")
  fi

  if [ ! -z $SNS_TOPIC_ARN_EXT_HTTP_SUBMIT ]; then
    JAVA_OPTS+=("-Dext_http.submitTopicNameOrArn=$SNS_TOPIC_ARN_EXT_HTTP_SUBMIT")
  fi

  if [ ! -z $SQS_ARN_EXT_HTTP_DELAY_QUEUE ]; then
    JAVA_OPTS+=("-Dext_http.delayQueueNameOrArn=$SQS_ARN_EXT_HTTP_DELAY_QUEUE")
  fi

  if [ ! -z $IES_S2S_PASSWORD ]; then
    JAVA_OPTS+=("-Dsystem.credentials.password=$IES_S2S_PASSWORD")
  fi

  # Default cassandra keystore path
  if [ -z $CASSANDRA_KEYSTORE ]; then
    CASSANDRA_KEYSTORE="/etc/tls/dse/local/keystore.jks"
  fi

  JAVA_OPTS+=("-Dcassandra.keystore=$CASSANDRA_KEYSTORE")

  if [ "$FETCH_TRUSTSTORE_PROVIDER" == "secretsmanager" ]; then
    if [ ! -z $FETCH_TRUSTSTORE_SOURCE_PATH ]; then
      /fetch_keystore.sh $FETCH_TRUSTSTORE_SOURCE_PATH $CASSANDRA_KEYSTORE $AWS_REGION
    else
      printf "\nERROR: FETCH_TRUSTSTORE_PROVIDER is set but requires \$FETCH_TRUSTSTORE_SOURCE_PATH to also be set\n"
      exit 1
    fi
  else
    printf "\nSkip fetching truststore...\nmercury will expect the keystore to be existing on the path:\n$CASSANDRA_KEYSTORE\n"
  fi

  set -- java ${JAVA_OPTS[@]} -jar /opt/mercury/*.jar
fi

if [ "$1" = 'java' -a "$(id -u)" = '0' ]; then
  exec gosu mercury "$BASH_SOURCE" "$@"
fi

exec "$@"
