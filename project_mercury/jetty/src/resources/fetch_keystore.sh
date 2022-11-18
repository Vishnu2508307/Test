#!/bin/bash

if [ -z $1 ]; then
  printf "\nERROR: Secret key reference must be provided as a parameter, e.g. fetch_keystore.sh <secrets_key_name> <destination>\n"
  exit 1
fi

if [ -z $2 ]; then
  printf "\nERROR: Keystore destination must be provided as a parameter, e.g. fetch_keystore.sh <secrets_key_name> <destination>\n"
  exit 1
fi

keystore_secrets_key="${1}"
keystore_dir="$(dirname $2)"
keystore="$(basename $2)"
region="${3}"

printf "\nAttempting to fetch the keystore ${keystore_secrets_key}...\n"

mkdir -p ${keystore_dir}

rm -f ${keystore_dir}/${keystore}

aws secretsmanager get-secret-value --secret-id "${keystore_secrets_key}" --region "${region}" --query 'SecretBinary' --output text | base64 --decode > ${keystore_dir}/${keystore}

if [ -s ${keystore_dir}/${keystore} ]; then
  printf "\nSUCCESS: Saved the secret to ${keystore_dir}/${keystore}\n"
else
  printf "\nERROR: Failed to fetch secret ${keystore_secrets_key}.\n"
  printf "\nERROR: Check that the secret exists OR the necessary permissions have been granted!\n"
  exit 1
fi
