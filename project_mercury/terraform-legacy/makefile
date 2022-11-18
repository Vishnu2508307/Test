# COLORS
GREEN  := $(shell tput -Txterm setaf 2)
YELLOW := $(shell tput -Txterm setaf 3)
BLUE   := $(shell tput -Txterm setaf 4)
RESET  := $(shell tput -Txterm sgr0)

SHELL  := /bin/bash

TARGET_MAX_CHAR_NUM=25

## Show Available Tasks:
help:
	@echo ''
	@echo 'Usage:'
	@awk '/^[a-zA-Z\-\_0-9]+:/ { \
		helpMessage = match(lastLine, /^## (.*)/); \
		Params = match(lastLine, / @/); \
		if (helpMessage) { \
			helpCommand = substr($$1, 0, index($$1, ":")-1); \
			if (Params) { \
				Params = substr(lastLine, index(lastLine, "@")); \
			} \
			else { \
				Params = " "; \
			} \
			helpMessage = substr(lastLine, 3, index(lastLine, ":")-1); \
			printf " ${YELLOW}make %-$(TARGET_MAX_CHAR_NUM)s${RESET} ${BLUE}%-125s${RESET} ${GREEN}%s${RESET}\n", helpCommand, Params, helpMessage; \
		} \
	} \
	{ lastLine = $$0 }' $(MAKEFILE_LIST)

validate_input:
define n


endef

ifneq ($(MAKECMDGOALS), $(filter $(MAKECMDGOALS), help))
  ifndef ENV
    $(error ENV is not set)
  endif

  ifndef REGION
    $(error REGION is not set)
  endif

  ifndef RELEASE_VERSION
    $(error RELEASE_VERSION is not set)
  endif
endif

terraform_dir_prefix := ${PWD}

terraform_preflight_check := terraform --version && terraform init && terraform get
terraform_dir := ${terraform_dir_prefix}/targets/${ENV}/${REGION}/${MAKECMDGOALS}
terraform_preflight_check := terraform --version && terraform init && terraform get

ifeq ($(ACTION), apply)
  terraform_action := ${terraform_preflight_check} && terraform apply ${terraform_dir_prefix}/build/targets-${ENV}-${REGION}-${MAKECMDGOALS}.plan
else ifeq ($(ACTION), init)
  terraform_action := ${terraform_preflight_check}
else ifeq ($(ACTION), destroy)
  terraform_action := ${terraform_preflight_check} && terraform destroy \
	  -var-file="${terraform_dir_prefix}/targets/variables/common.tfvars" \
	  -var-file="${terraform_dir_prefix}/targets/variables/${ENV}.tfvars" \
	  -var ENV=${ENV} \
	  -var release_version='${RELEASE_VERSION}'
else ifeq ($(ACTION), output)
  terraform_action := terraform output -json
else
  terraform_action := ${terraform_preflight_check} && terraform plan \
	  -var-file="${terraform_dir_prefix}/targets/variables/common.tfvars" \
	  -var-file="${terraform_dir_prefix}/targets/variables/${ENV}.tfvars" \
	  -var ENV=${ENV} \
	  -var release_version='${RELEASE_VERSION}' \
	  -out=${terraform_dir_prefix}/build/targets-${ENV}-${REGION}-${MAKECMDGOALS}.plan
endif

## Action terraform on cache: @ENV=(dev|qaint|ppe|stg|prod) @REGION=(ap-southeast-2|us-east-1|us-west-1) @ACTION=(plan|apply|destroy)
cache: validate_input
	@pushd ${terraform_dir} > /dev/null 2>&1 && \
	${terraform_action} && \
	popd > /dev/null 2>&1

## Action terraform on mercury: @ENV=(dev|qaint|ppe|stg|prod) @REGION=(ap-southeast-2|us-east-1|us-west-1) @ACTION=(plan|apply|destroy)
mercury: validate_input
	@pushd ${terraform_dir} > /dev/null 2>&1 && \
	${terraform_action} && \
	popd > /dev/null 2>&1

## Action terraform on ext-http: @ENV=(dev|qaint|ppe|stg|prod) @REGION=(ap-southeast-2|us-east-1|us-west-1) @ACTION=(plan|apply|destroy)
ext-http: validate_input
	@pushd ${terraform_dir} > /dev/null 2>&1 && \
	${terraform_action} && \
	popd > /dev/null 2>&1

## Action terraform on ambrosia-export: @ENV=(dev|qaint|ppe|stg|prod) @REGION=(ap-southeast-2|us-east-1|us-west-1) @ACTION=(plan|apply|destroy)
ambrosia-export: validate_input
	@pushd ${terraform_dir} > /dev/null 2>&1 && \
	${terraform_action} && \
	popd > /dev/null 2>&1

## Action terraform on ingestion: @ENV=(dev|qaint|ppe|stg|prod) @REGION=(ap-southeast-2|us-east-1|us-west-1) @ACTION=(plan|apply|destroy)
ingestion: validate_input
	@pushd ${terraform_dir} > /dev/null 2>&1 && \
	${terraform_action} && \
	popd > /dev/null 2>&1

## Action terraform on asset: @ENV=(dev|qaint|ppe|stg|prod) @REGION=(ap-southeast-2|us-east-1|us-west-1) @ACTION=(plan|apply|destroy)
asset: validate_input
	@pushd ${terraform_dir} > /dev/null 2>&1 && \
	${terraform_action} && \
	popd > /dev/null 2>&1
