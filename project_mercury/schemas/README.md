# Schema Management

The purpose of this project it to manage schemas. It is responsible for:

* Management of schemas (create new keyspaces/tables, modify schemas, etc.)
* Deployment of changes

Changes are created and/or applied through various gradle tasks. 

It uses a keyspace (changelog) and underlying tables (changelog_*) to keep the schemas in sync.


## Usage

### Overview
```
./gradlew tasks

Schema Management - All tasks
-----------------------------
applySchemaChanges - Apply all schema changes; use -Penvironment=env_name
outputSchemaChanges - Output all schema changes; use -Penvironment=env_name

Schema Management - Cassandra tasks
-----------------------------------
applyCassandraMutations - Apply the Cassandra mutations to an environment; use -Penvironment=env_name
createCassandraChangelog - Create the changelog tracking schema for Cassandra; use -Penvironment=env_name
createCassandraKeyspace - Create a new Cassandra keyspace; use -Pkeyspace=keyspace_name
createCassandraTable - Create a Cassandra table; use -Pkeyspace=keyspace_name -Ptable=table_name
createCassandraTableAlter - Alter a Cassandra table; use -Pkeyspace=keyspace_name -Ptable=table_name
outputCassandraMutations - Output the Cassandra mutations to stdout; use -Penvironment=env_name
```

### General usage

Apply all changes across all managed schemas: 

```
gradlew applySchemaChanges
```

### Fetching keystore from secretsmanager
To connect to each cassandra database, you can fetch their corresponding environment keystore via AWS secretsmanager:

Make sure to replace INSERT_ENVIRONMENT with the desired environment to fetch.
```
AWS_PROFILE=ENG aws secretsmanager get-secret-value --secret-id "/INSERT_ENVIRONMENT/mercury/cassandra/smartsparrow_keystore.jks" --region ap-southeast-2 --query "SecretBinary" --output text | base64 --decode > etc/tls/dse/INSERT_ENVIRONMENT/keystore.jks
```

If you are denied access to fetch this secret, it likely means you will need to request access to the AWS Engineering account.

## Configuration

See `defaultSample` in config.groovy for available options and required/optional attributes and defaults.

A working SSH tunnel configurations is  e.g. `prod`

Anything can be overridden with -P and the flattened name of the property e.g.:

     g  :schemas:applySchemaChanges -Penvironment=uat \
      -Pcassandra.authentication.username=cassandra \
      -Pcassandra.authentication.password=cassandra \
      -Pssh.user=markus -Pssh.identity=$HOME/.ssh/id_rsa_test \
      -Pssh.localPortForward.remoteHost=cassandra.local.smartsparrow.com

A special case is the ssh.remoteHost setting. 
When the ssh block is configured and remoteHost isnt set, the cassandra.contactPoint is used as a remoteHost
And the  cassandra.contactPoint ois changed to default.

Only after the configuration is read in and all defaults are set are settings overridden by the command line properties. SSH cannot be set up with the commandline without an appropriate ssh config block  since by default
SSH tunneling is not enabled.

To check what final settings are used pass `--info` 

Make sure that you have  keytool import the keys of the relevant environment (uat keys are used for many none-production aws cassandra clusters)

## Troubleshooting

When you see one of these :

    > Auth fail
    > USERAUTH fail

You are either using the wrong ssh key, or user or both.



