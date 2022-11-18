# Configuration Management Module
This module is aimed to provide functionality to manage, load and use configuration properties in Mercury.

## Types of configuration
The different types of configuration are: static, bootstrap and dynamic.

### Static Configuration
Static configuration is defined at application start and can be specified as JVM parameters at application start. For example: `-Denv.region=US_EAST_1`.
These properties are used to connect to Cassandra DB and load Bootstrap configuration. If no static configuration is provided, default values are used.
There is a list of static properties with default values:

Property Key                        |Default Value 
------------------------------------|---------------
cassandra.contactPoints             |`192.168.192.201`
cassandra.authentication.username   |`cassandra`
cassandra.authentication.password   |`cassandra`
cassandra.keystore                  |`${user.dir} + "/etc/tls/dse/local/keystore.jks"`
cassandra.keystore.password         |`cassandra`
env.region                          |`LOCAL`

These properties are available in code via `System.getProperty()`. `env.region` can be injected using `@Named("env.region")`
Example:


```java
class ConfigurationService {
    @Inject
     public ConfigurationService(@Named("env.region") EnvRegion region) {
         //...
     }
}
```

Note: These cassandra properties are intended to be used only for loading Bootstrap Configuration not to open application session.

### Bootstrap Configuration
This configuration is fetched from DB from `env.config` table and is used to load configuration which needed to start application 
and can not be part of Dynamic Configuration: ex. Cassandra and Redis settings.  If no settings are provided in DB, 
default values are used (connects localhost instances).

`BootstrapConfiguration` can be injected in code:

```java
public class RedissonModule extends AbstractModule {
   
    @Provides
    @Singleton
    public RedissonReactiveClient getStringRedisReactiveCommands(BootstrapConfiguration bootstrapConfiguration) {
        CassandraConfig cassandraConfig = bootstrapConfiguration.getCassandraConfig();
        RedisConfig redisConfig = bootstrapConfiguration.getRedisConfig();
    }
}
```

### Dynamic Configuration
Represents application runtime configuration. It is loaded from `env.config` table, cached in Redis and re-loaded every 5 minutes.

This configuration is accessible via injection `ConfigurationService`. Example:

```java
public class MessageHandler {
    @Inject
    public MessageHandler(ConfigurationService configurationService) {
        MailConfig mailConfig = configurationService.get(MailConfig.class, "mail");
        
        int idleTimeout = configurationService.getInt("idle.timeout");
        String supportEmail = configurationService.getString("support.email");
    }
}

```
More methods can be found in `com.smartsparrow.config.service.ConfigurationService`

#### Available Configuration
Name|Description                                                 | Properties (with default values*) 
----------|------------------------------------------------------------|------------------------
cassandra |settings to establish connection with application DB|<ul><li>contactPoints (`192.168.192.201`)</li><li>username (`cassandra`) </li><li>password (`cassandra`) </li><li>certificate**</li><li>keystore** (`${user.dir} + "/etc/tls/dse/local/keystore.jks"`)</li><li>keystorePassword** (`cassandra`)</li></ul> _**either certificate or keystore/keystorePassword should be provided_
redis | settings to connect Redis | <ul><li>address (`redis://redis.local.smartsparrow.com:6379`)</li><li>password (`aelpredis`)</li></ul>
plugin | settings for Plugins | <ul><li>distribution.bucketName</li><li>repository.bucketName</li></ul> 

*default values are used if no values are provided in DB