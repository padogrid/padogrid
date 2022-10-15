# Kafka Addon Library

This package contains an open source Kafka addon API that supplements the Kafka API.

## Compiling and installing kafka-addon-core

```
mvn install
```


## Deploying kafka-addon-core

Place the `kafka-addon_core-<version>.jar` file in both client and server class paths. Upon successful build, the jar file can be found in the target directory as follows:

```
target/kafka-addon-core-<version>.jar
```

## Addon Features

### Debezium Kafka Sink

