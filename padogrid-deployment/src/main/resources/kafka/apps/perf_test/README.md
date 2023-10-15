# Kafka `perf_test` App

✏️  *To run the Kafka `perf_test` app, you must first run `builid_app` to download the Avro binaries and start a schema registry as described in the [Scripts](#scripts) section.*

The `perf_test` app provides Kafka client programs to perform the following:

- Ingest mock data of any size
- Ingest mock data with entity relationships (ER)
- Ingest mock data directly to databases
- Simulate complex application workflows that invoke Kafka operations without coding
- Measure Kafka latencies and throughputs in a multi-threaded user session environment

The `perf_test` app provides a pair of scripts to ingest and transact mock data for testing Kafka throughputs and latencies. It provides a quick way to run your performance tests by configuring a few properties such as the payload size, the number of objects (entries), and the number of worker threads. Out of the box, these properties have already been pre-configured in `etc/group-*.properties`, which you can modify as needed to meet your test criteria.

The `perf_test` app includes the `test_group` script that allows you to configure one or more groups of Kafka data structure operations and execute them in sequence and/or in parallel. A group is analogous to a function that makes multiple data structure method calls in the order they are specified in the `etc/group.properties` file. The `etc` directory also contains the `group-send.properties` file for demonstrating complex workflows that invoke 22 send calls on 22 different topics. There are also several example `group-*.properties` files. You can also configure the Kafka producer in `etc/kafka-producer.properties` to measure the improved throughput. 

The `perf_test` app can also directly *upsert* mock data into your database of choice using Hibernate, which automatically creates tables as needed. This capability provides a means to synchronize Kafka with your database and perform latency tests.

## Configuration Files

The following table describes a list of preconfigured properties files in the `etc/` directory.

| Properties File | Description |
| --------------- | ----------- |
| `group.properties`      | Defines properties for performing groups of `KafkaProucer.send()` method calls. |
| `group-send.properties` | Defines properties for making 22 send calls on 22 different topics in a single group. |
| `group-send-sleep.properties` | Defines properties for testing the sleep operation. |
| `group-topic.properties` | Defines properties for KafkaProucer.send() operations. |
| `group-factory.properties` | Defines properties for ingesting mock data. |
| `group-factory-er.properties` | Defines properties for ingesting mock data with entity relationships. |

You can introduce your own test criteria by modifying the properties the above files or supply another properties file by specifying the `-prop` option of the scripts described below.

## Scripts

The `bin_sh/` directory contains the following scripts. By default, these scripts simply prints the configuration information obtained from the `etc/<perf>.properties` file. To run the test cases, you must specify the `-run` option.

| Script | Default Config | Description |
| ------ | -------------- | ----------- |
| `build_app` | pom.xml | Downloads the required libraries by running Maven. **You must first run the `build_app` script before you can run other scripts.** If you are behind a firewall then you can manually download `kafka-clients-<version>.jar` from the following MVNRepository and place it in the workspace `lib` directory: https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients |
| `test_group` | etc/group.properties | Displays or runs group test cases (`send`, `sendbatch`, `sleep`). A group represents a function that executes one or more Kafka client operations. |

✏️  The `perf_test` app uses Avro classes and requires a schema registry. If you have configured the cluster with Confluent in PadoGrid, then you can start its schema registry as follows.

```bash
# Start Conflent schema registry. Make sure to unset CLASSPATH proior to
# running it to avoid jar conflicts.
CLASSPATH="" schema-registry-start $CONFLUENT_HOME/etc/schema-registry/schema-registry.properties
```

## Script Usages

### test_group

```bash
./test_group -?
```

Output:

```console
Usage:
   test_group [-run|-list] [-db|-delete] [-prop <properties-file>] [-?]

   Displays or runs group test cases specified in the properties file.
   A group represents a function that executes one or more Kafka data
   structure operations. This program measures average latencies and throughputs
   of group (or function) executions.
   The default properties file is
      ../etc/group.properties

       -run              Runs test cases.

       -list             Lists data structures and their sizes.

       -db               Runs test cases on database instead of Kafka. To use this
                         option, each test case must supply a data object factory class
                         by specifying the 'factory.class' property and Hibernate must
                         be configured by running the 'build_app' command.

       -delete           Deletes (destroys) all the data structures pertaining to the group
                         test cases that were created in the Kafka cluster. If the '-run'
                         option is not specified, then it has the same effect as the '-list'
                         option. It only lists data strcutures and their without deleting them.

       <properties-file> Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/Padogrid/workspaces/rwe-bundles/bundle-kafka-3-examples-python/apps/perf_test5/results

Notes:
   The 'perf_test' app uses Avro classes and requires Schema Registry.
```

Run `test_group` by sepcifying the `-run` option.

```bash
./test_group -run
```

## Results

Upon successful run, the test results are outputted in the `results/` directory. The following shows an example.

```bash
cat ../results/group-g1-kafka-221014-155110.txt
```

Output:

```console
******************************************
Group Test
******************************************

                       Product: kafka
                         Group: g1
           Concurrent Group(s): g1
                       Comment: Stores Customer objects into nw.customers.
                    Operations: send1
                Test Run Count: 1
      Test Run Interval (msec): 0
Total Invocation Count per Run: 1000
                  Thread Count: 8
   Invocation Count per Thread: 125

Start Time: Fri Oct 14 15:51:10 EDT 2022

Actual Total Number of Invocations: 1000

Time unit: msec
   Thread 1: 630
   Thread 2: 615
   Thread 3: 616
   Thread 4: 620
   Thread 5: 616
   Thread 6: 619
   Thread 7: 617
   Thread 8: 617

                Max Time (msec): 630
            Elapsed Time (msec): 633
         Total Invocation Count: 1000
 M Throughput (invocations/sec): 1587.3016
M Latency per invocation (msec): 0.63
 E Throughput (invocations/sec): 1579.7788
E Latency per invocation (msec): 0.633

Stop Time: Fri Oct 14 15:51:10 EDT 2022
```

## Inserting and Updating Database Tables

The `group_test -db` command directly loads mock data into database tables without connecting to Kafka. You can use this command to pre-populate the database before testing database synchronization tests in Kafka. This command is also useful for testing the CDC use case in which database changes are automatically ingested into Kafka via a CDC product such as Debezium and Striim.

```bash
# Edit setenv.sh to set the correct hibernate configuration file.
vi setenv.sh
```

By default, `setenv.sh` is configured with `hibernate.cfg-mysql.xml`. Change it to another if you are using a different database. Please see the `etc` directory for all the available database configuration files. If your database is not listed, then you can create one by copying one of the `hibernate-*` files and specifying that file name in the `setenv.sh` file.

Make sure to set the correct database user name and password in the Hibernate configuration file.

```bash
# Hibernate
JAVA_OPTS="$JAVA_OPTS -Dkafka-addon.hibernate.config=$APP_ETC_DIR/hibernate.cfg-mysql.xml"
```

Run `test_group -db`.

```bash
./test_group -db -run -prop ../etc/group-factory.properties
```

## Generating Entity Relationships (ER)

If you want to add entity relationships to your data, then you can implement [`DataObjectFactory`](https://github.com/padogrid/padogrid/blob/develop/kafka-addon-core/src/test/java/org/kafka/addon/test/perf/data/DataObjectFactory.java) or extend [`AbstractDataObjectFactory`](https://github.com/padogrid/padogrid/blob/develop/kafka-addon-core/src/test/java/org/kafka/demo/nw/impl/AbstractDataObjectFactory.java) and pass the object key to the `createEntry()` method using the `factory.er.operation` property. The `perf_test` app includes an ER example that creates one-to-many ER between `Customer` and `Order` objects by setting `Customer.customerId` to `Order.customerId` while ingesting mock data. Please see [`org.kafka.demo.nw.impl.OrderFactoryImpl`](https://github.com/padogrid/padogrid/blob/develop/kafka-addon-core/src/test/java/org/kafka/demo/nw/impl/OrderFactoryImpl.java) for details. You can run the example as follows:

```bash
./test_group -run -prop ../etc/group-factory-er.properties
```

The ER capability provides you a quick way to ingest co-located data into Kafka and test server-side operations that take advatange of data affinity.
