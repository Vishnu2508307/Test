These are the TLS certificates which are used by DataStax Enterprise (DSE).

They are used in the following context:

* Internode communications
* Client to cluster communications

Some useful keytool commands can be found:

* [The Most Common Java Keytool Keystore Commands](https://www.sslshopper.com/article-most-common-java-keytool-keystore-commands.html)
* [github/PatrickCallaghan/datastax-ssl-example](https://github.com/PatrickCallaghan/datastax-ssl-example)

# SANDBOX Deployment
### Encode for use with AWS Secrets Manager binary data
```bash
# for sandbox and dev, keystore and truststore are all the same
# (as in you only need to encode the truststore.jks and use it for both secrets in both accounts)
base64 keystore.jks | sed ':a;N;$!ba;s/\n//g' > keystore.jks.b64
base64 truststore.jks | sed ':a;N;$!ba;s/\n//g' > truststore.jks.b64
```
Save the encoded contents as binary data in the corresponding secrets

# BRONTE Deployment

### Example to decrypt and encode the truststore.jks file
```bash
# for sandbox and dev, keystore and truststore are all the same

# define the env variables
ENV="prod" # the environment you are doing this for
KEY_ALIAS="rootca" # per baybridge configuration
KEYSTORE_PASSWORD="cassandra" # the keystore password
GIT_BASE_FOLDER="/c/Users/fcosti/git/phx" # the folder where the relative paths of baybridge and aero repositories diverge

# define your local cert paths according to your own environment
ENCRYPTED_CERTS_PATH="${GIT_BASE_FOLDER}/baybridge/baybridge-ansible/cassandra/templates/apache-cassandra/phoenix/${ENV}/certs"
DECRYPTED_CERTS_PATH="${GIT_BASE_FOLDER}/aero/aero-service/aero-service-mercury/etc/tls/dse"

# decrypt the truststore
ansible-vault decrypt ${ENCRYPTED_CERTS_PATH}/truststore.jks

# copy it over to the aero-service-mercury repository
cp ${ENCRYPTED_CERTS_PATH}/truststore.jks ${DECRYPTED_CERTS_PATH}/${ENV}/truststore.jks

# encode it to use it with secretsmanager
base64 ${DECRYPTED_CERTS_PATH}/${ENV}/truststore.jks | sed ':a;N;$!ba;s/\n//g' > ${DECRYPTED_CERTS_PATH}/${ENV}/truststore.jks.b64

# list the certs to see that everything is fine
keytool -list -v -storepass ${KEYSTORE_PASSWORD} -keystore ${DECRYPTED_CERTS_PATH}/${ENV}/truststore.jks

# export the pem cert
keytool -export \
          -rfc \
          -alias ${KEY_ALIAS} \
          -file ${DECRYPTED_CERTS_PATH}/${ENV}/cassandra.pem \
          -keystore ${DECRYPTED_CERTS_PATH}/${ENV}/truststore.jks \
          -storepass ${KEYSTORE_PASSWORD}

# export the binary cert
keytool -export \
            -alias ${KEY_ALIAS} \
            -file ${DECRYPTED_CERTS_PATH}/${ENV}/cassandra.crt \
            -keystore ${DECRYPTED_CERTS_PATH}/${ENV}/truststore.jks \
            -storepass ${KEYSTORE_PASSWORD}
```

## Certificate Generation ##

```
$ PASSWD_PRIVKEY=cassandra
$ PASSWD_KEYSTORE=cassandra

# Generate the keystore and all the certificates, snip as necessary.
for env in local dev uat prod dls; 
do 
    mkdir -p ${env}

    # adjust the environment as appropriate (local|dev|uat|<empty>)
    KEY_ALIAS=${env}-dse-smartsparrow-com

    # Generate the private/public keypairs
    keytool -genkeypair \
          -alias ${KEY_ALIAS} \
          -keyalg RSA \
          -keysize 2048 \
          -validity 3650 \
          -keystore ${env}/keystore.jks \
          -keypass ${PASSWD_PRIVKEY} \
          -storepass ${PASSWD_KEYSTORE} \
          -dname 'cn=*.smartsparrow.com, ou=Eng, o=Smart Sparrow, l=Sydney, s=NSW, c=AU'

    # Export certificate
    keytool -export \
          -rfc \
          -alias ${KEY_ALIAS} \
          -file ${env}/cassandra.pem \
          -keystore ${env}/keystore.jks \
          -storepass ${PASSWD_KEYSTORE}

    # Export certificate as binary
    keytool -export \
            -alias ${KEY_ALIAS} \
            -file ${env}/cassandra.crt \
            -keystore ${env}/keystore.jks \
            -storepass ${PASSWD_KEYSTORE}
    
    # Create a truststore
    keytool -import \
            -noprompt \
            -v \
            -trustcacerts \
            -alias ${KEY_ALIAS} \
            -file ${env}/cassandra.pem \
            -keystore ${env}/truststore.jks \
            -storepass ${PASSWD_KEYSTORE}

done
```

## Certificate Import ##

For JVM Clients, it is best to import the certificate to the global truststore

```
$ KEY_ALIAS=local-dse-smartsparrow-com

$ keytool -import \
          -noprompt \
          -v \
          -trustcacerts \
          -alias ${KEY_ALIAS} \
          -file cassandra.pem \
          -storepass changeit \
          -keystore $JAVA_HOME/jre/lib/security/cacerts
```
