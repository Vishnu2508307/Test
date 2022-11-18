# NOTE
Terraform on aero-infra-platform needs to call the arn:aws:iam::{AWS_ACCOUNT_OWNER_ID}:role/deploy_to_dev_aero_cdn_role role, which as of now can only be called by the gocd agent roles.
This means we shouldn't run this code directly either (same as aero-infra-frontend) and do it via commit to master on aero-app-learnspace|workspace repos, which will trigger the relevant pipeline jobs.

# Terraform Aero Platform
This project is aimed at managing the deployment of the Aero platform.

## Setup
The following needs to be installed:

* docker
* docker-machine (MacOSX only)
* awscli

## Pull down the devops-tool image
```bash
export AWS_PROFILE=<ENGINEERING_AWS_ACCOUNT_PROFILE>
eval $(aws ecr get-login --region ap-southeast-2 --no-include-email)
```

## Deploying Mercury
The following are commands to deploy mercury infrastructure components. The release version is injected from the go pipeline label during the deployment pipeline execution.

A smoke test is done on every deployment of the ECS mercury service to ensure that the service is stable before it is allowed to proceed further.

```sh
# Data tier (redis/elasticache and cassandra)
## Create the plan
make backend_mercury ENV=prod REGION_SCOPE=ap-southeast-2 RELEASE_VERSION=1 ACTION=plan
## Apply the changes
make backend_mercury ENV=prod REGION_SCOPE=ap-southeast-2 ACTION=apply

# App tier (mercury application)
## Create the plan
make backend_mercury ENV=prod REGION_SCOPE=ap-southeast-2 RELEASE_VERSION=1 ACTION=plan
## Apply the changes
make backend_mercury ENV=prod REGION_SCOPE=ap-southeast-2 ACTION=apply
```

#### Triggering cloudwatch alarm with cross account SNS topic policy
```sh
# From AELP account 836018766099
{
  "Version": "2008-10-17",
  "Statement": [
    {
      "Sid": "SubscribeToTopicFromCrossAccount",
      "Effect": "Allow",
      "Principal": {
        "AWS": [
          "arn:aws:iam::375169533211:root",
        ]
      },
      "Action": [
        "SNS:Subscribe"
      ],
      "Resource": "arn:aws:sns:ap-southeast-2:836018766099:ALARMS"
    },
    {
      "Sid": "PublishMessageOnTopic",
      "Effect": "Allow",
      "Principal": {
        "AWS": "*"
      },
      "Action": "SNS:Publish",
      "Resource": "arn:aws:sns:ap-southeast-2:836018766099:ALARMS",
      "Condition": {
        "ArnLike": {
          "AWS:SourceArn": "arn:aws:cloudwatch:ap-southeast-2:375169533211:alarm:*"
        }
      }
    }
  ]
}
```

# How To Plan Locally
You will NOT have the ability to apply or destroy locally.

To execute local plans (steps only required for aero-infra-platform local plan due to module sources being git repositories):
- your git command line must be properly configured and authorized, meaning you must be able to clone the aero-infra-service and aero-infra-platform repositories via command line
- you must be able to run terraform get from the root of aero-infra-platform/smartsparrow/{env}/{region}/backend_mercury/{target}
- create a .ssh folder on aero-infra-platform root, it must have a file named id_rsa with your authorized gitlab ssh key material
- .gitignore will make sure it's not committed to the repo

### Allow longpaths for git:
Aditionally, you may have to issue the following command to allow longpaths:
```bash
git config --system core.longpaths true
```

### Optional (to use a different file name or stop host key checking):
You can create a config file as well containing the following. Place it in aero-infra-platform/.ssh folder.
```bash
Host gitlab.com
  User git
  IdentityFile ~/.ssh/id_rsa
  StrictHostKeyChecking no
  IdentitiesOnly yes
```

### NOTE: append "RUN_TYPE=aws-runas" to each command if you are using aws-runas for authentication
#### DEV
```bash
make --makefile makefile_local cache           ENV=dev   REGION_SCOPE=ap-southeast-2 ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local mercury         ENV=dev   REGION_SCOPE=ap-southeast-2 ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ext-http        ENV=dev   REGION_SCOPE=ap-southeast-2 ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ambrosia-export ENV=dev   REGION_SCOPE=ap-southeast-2 ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ingestion       ENV=dev   REGION_SCOPE=ap-southeast-2 ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local asset           ENV=dev   REGION_SCOPE=ap-southeast-2 ACTION=plan #RUN_TYPE=aws-runas
```

#### QA-INT
```bash
make --makefile makefile_local cache           ENV=qaint REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local mercury         ENV=qaint REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ext-http        ENV=qaint REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ambrosia-export ENV=qaint REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ingestion       ENV=qaint REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local asset           ENV=qaint REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
```

#### PPE
```bash
make --makefile makefile_local cache           ENV=ppe   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local mercury         ENV=ppe   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ext-http        ENV=ppe   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ambrosia-export ENV=ppe   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ingestion       ENV=ppe   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local asset           ENV=ppe   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
```

#### STG
```bash
make --makefile makefile_local cache           ENV=stg   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local mercury         ENV=stg   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ext-http        ENV=stg   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ambrosia-export ENV=stg   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ingestion       ENV=stg   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local asset           ENV=stg   REGION_SCOPE=us-east-1      ACTION=plan #RUN_TYPE=aws-runas
```

#### PROD
```bash
make --makefile makefile_local cache           ENV=prod  REGION_SCOPE=us-west-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local mercury         ENV=prod  REGION_SCOPE=us-west-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ext-http        ENV=prod  REGION_SCOPE=us-west-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ambrosia-export ENV=prod  REGION_SCOPE=us-west-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local ingestion       ENV=prod  REGION_SCOPE=us-west-1      ACTION=plan #RUN_TYPE=aws-runas
make --makefile makefile_local asset           ENV=prod  REGION_SCOPE=us-west-1      ACTION=plan #RUN_TYPE=aws-runas
```
