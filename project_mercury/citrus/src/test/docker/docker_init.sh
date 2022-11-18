#!/bin/sh
# This script runs the docker container in detach mode
# it then waits for the docker container to exit and reads its exit code
# This script enables to save all the logs to files before the exit code
# is issued to the parent container. Using docker --exit-code-from stops the parent container before
# the logs can be saved

# Initialize docker container and wait for its exit value. Requires argument number to determine
# CITRUS_RUNNER_NUM value
function execDocker() {
  RED='\033[0;31m'
  GREEN='\033[0;32m'
  NO_COLOR='\033[0m'

  # validate that the required argument is supplied
  # TODO check that the argument is a number
  if [ -n "$1" ]; then
    echo "CITRUS_RUNNER_NUM -> $1"
  else
    echo "${RED}Test run error. Require number argument to determine CITRUS_RUNNER_NUM${NO_COLOR}"
    exit 1
  fi

  
  STATUS_CODE=""
  # run docker compose in detach mode
  CITRUS_RUNNER_NUM=$1 \
  NEXUS_USERNAME=`aws secretsmanager get-secret-value --secret-id "arn:aws:secretsmanager:ap-southeast-2:585397755241:secret:/nexus" --region ap-southeast-2 --query "SecretString" --output text | jq -r ".user"` \
  NEXUS_PASSWORD=`aws secretsmanager get-secret-value --secret-id "arn:aws:secretsmanager:ap-southeast-2:585397755241:secret:/nexus" --region ap-southeast-2 --query "SecretString" --output text | jq -r ".password"` \
  docker-compose up --no-color -d
  docker ps
  
  # no need to print the logs to the console since those will be redirected to a file
  # save space and time
  echo "Running feature set $1..."
  
  # wait for the docker container to exit and store its exit code result
  STATUS_CODE=$(docker wait citrus_runner)
  
  # print some colored messages so it is immediately visible if things succeeded or not
  if [[ $STATUS_CODE == '0' ]]; then
      echo "Citrus Test run completed"
  else
      echo "Citrus Test run failed. Docker container exited with code ${STATUS_CODE}"
  fi
  
  # Save all the logs
  docker-compose logs --tail="all" cassandra_node1 >& cassandra_logs_$1.log
  docker-compose logs --tail="all" redis >& redis_logs_$1.log
  docker-compose logs --tail="all" cassandra_init >& cassandra_init_logs_$1.log
  docker-compose logs --tail="all" apply_schema >& apply_schema_logs_$1.log
  docker-compose logs --tail="all" apply_config >& apply_config_logs_$1.log
  docker-compose logs --tail="all" mercury >& mercury_logs_$1.log
  docker-compose logs --tail="all" citrus_simulator >& citrus_simulator_logs_$1.log
  docker logs -f citrus_runner >& citrus_logs_$1.log

  # print the logs file names
  echo "Logs saved to:"
  echo "✓ citrus_logs_$1.log"
  echo "✓ cassandra_logs_$1.log"
  echo "✓ redis_logs_$1.log"
  echo "✓ cassandra_init_logs_$1.log"
  echo "✓ apply_schema_logs_$1.log"
  echo "✓ apply_config_logs_$1.log"
  echo "✓ mercury_logs_$1.log"
  echo "✓ citrus_simulator_logs_$1.log"

  # exit with 0 when the docker container exited ok
  if [[ $STATUS_CODE == '0' ]]; then
      exit 0
  fi
  
  # exit with code 1 when the docker container did not exit ok
  exit 1 
}

"$@"
