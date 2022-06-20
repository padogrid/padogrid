# Redisson `perf_test` App

The `perf_test` app provides Redisson client programs to perform the following:

- Ingest mock data of any size
- Ingest transactional data of any size
- Ingest mock data with entity relationships (ER)
- Ingest mock data directly to databases
- Simulate complex appliation workflows that invoke Redisson operations without coding
- Measure Redisson latencies and throughputs in a multi-threaded user session environment

The `perf_test` app provides a pair of scripts to ingest and transact mock data for testing Redisson throughputs and latencies. It provides a quick way to run your performance tests by configuring a few properties such as the payload size, the number of objects (entries), and the number of worker threads. Out of the box, these properties have already been pre-configured in `etc/ingestion.properties` and `etc/tx.properties`, which you can modify as needed to meet your test criteria.

The `perf_test` app also includes the `test_group` script that allows you to configure one or more groups of Redisson data structure operations and execute them in sequence and/or in parallel. A group is analogous to a function that makes multiple data structure method calls in the order they are specified in the `etc/group.properties` file. The `etc` directory also contains the `group-put.properties` and `group-get.properties` files for demonstrating complex workflows that invoke 22 put calls and 22 get calls on 22 different maps. There are also several example `group-*.properties` files for each data structure. You can also configure the Near Cache in `etc/redisson-client.yaml` to measure the improved throughput. 

The `perf_test` app can directly *upsert* mock data into your database of choice using Hibernate, which automatically creates tables as needed. This capability allows you to quickly synchrnize Redisson with your database and perform latency tests.

## Transaction Test Cases

All of the transaction test cases are performed on three (3) distributed maps with a simple PBM (Pharmacy Benefit Management) data model that associates the client group number with group members. Simply put, all of the members that belong to a group are co-located in the same Redisson partition. This enables each Redisson member to complete transactions with their local datasets without encountering additional netowork hops.

|Map    | Description | Script |
|------ | ------------| ------ |
|`eligibility` | The `eligibility` map contains `EligKey` and `Blob` entries. `EligKey` contains core member eligibility information and `Blob` contains a byte array as as a payload. The byte array size determines the size of the `Blob` object and it is configurable in `etc/ingestion.properties`.| `bin_sh/test_ingestion` |
| `profile`    | The `profile` map contains `ClientProfileKey` and `Blob` entries. `ClientProfileKey` contains core client information and `Blob` as described above.| `bin_sh/test_ingestion` |
| `summary`    | The `summary` map contains `groupNumber` and `GroupSummary` entries. `GroupSummary` contains the group summary results produced when the `tx` test case is performed by running the `bin_sh/test_tx` script.| `bin_sh/test_tx` |

## Configuration Files

The following table describes a list of preconfigured properties files in the `etc/` directory.

| Properties File | Description |
| --------------- | ----------- |
| `ingestion.properties` | Defines properties for ingesting data into the `eligibility` and `profile` maps. |
| `tx.properties`        | Defines properties for performing transactions. |
| `group.properties`     | Defines properties for performing groups of `IMap` method calls. |
| `group-put.properties` | Defines properties for making 22 put calls on 22 different maps in a single group. |
| `group-get.properties` | Defines properties for making 22 get calls on 22 different maps in a single group. Note that before invoking this file, `group-put.properties` must be invoked first to ingest data. |
| `group-cache.properties` | Defines properties for `ICache` (JCache) operations. Unlike other, data structures, `ICache` requires you to first configure the cluster with the caches that you want to test before running the `test_group` script. |
| `group-queue.properties` |  Defines properties for `IQueue` operations. |
| `group-rmap.properties` | Defines properties for `ReplicatedMap` operations. |
| `group-rtopic.properties` | Defines properties for `ReliableTopic` operations.|
| `group-topic.properties` | Defines properties for `ITopic` operations. |
| `group-factory.properties` | Defines properties for ingesting mock data. |
| `group-factory-er.properties` | Defines properties for ingesting mock data with entity relationships. |

You can introduce your own test criteria by modifying the properties the above files or supply another properties file by specifying the `-prop` option of the scripts described below.

## Scripts

The `bin_sh/` directory contains the following scripts. By default, these scripts simply prints the configuration information obtained from the `etc/perf.properties` file. To run the test cases, you must specify the `-run` option.

| Script | Description |
| ------ | ----------- |
| `build_app` | Downloads the required libraries by running Maven. **You must first run the `build_app` script before you can run other scripts.** If you are behind a firewall then you can manually download `redisson-<version>.jar` from the following MVNRepository and place it in the workspace lib directory: https://mvnrepository.com/artifact/org.redisson/redisson |
| `test_ingestion` | Displays or runs data ingestion test cases (`putall` or `put`) specified in the `etc/ingestion.properties` file. It ingests mock data into the `eligibility` and `profile` maps. |
| `test_tx` | Displays or runs transaction and query test cases specified in the `etc/tx.properties` file. It runs `get`, `getall`, `tx` test cases specified in the `perf.properties` file. |
| `test_group` | Displays or runs group test cases (`set`, `put`, `putall`, `get`, `getall`). A group represents a function that executes one or more Redisson `IMap` operations. |

## Script Usages

### test_ingestion

```bash
./test_ingestion -?
```

Output:

```console
Usage:
   test_ingestion [-run] [-prop <properties-file>] [-?]

   Displays or runs data ingestion test cases specified in the properties file.
   The default properties file is
      ../etc/ingestion.properties

       -run              Run test cases.

       <properties-file> Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/padogrid/workspaces/myrwe/ws-intro/apps/perf_test/results
```

### test_tx

```bash
./test_tx -?
```

Output:

```console
Usage:
   test_tx [-run] [-prop <properties-file>] [-?]

   Displays or runs transaction and query test cases specified in the properties file.
   The default properties file is
      ../etc/tx.properties

       -run              Run test cases.

       <properties-file> Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/padogrid/workspaces/myrwe/ws-intro/apps/perf_test/results
```

### test_group

```bash
./test_group -?
```

Output:

```console
Usage:
   test_group [-run|-list] [-db|-delete] [-prop <properties-file>] [-?]

   Displays or runs group test cases specified in the properties file.
   A group represents a function that executes one or more Redisson IMap
   operations. This program measures average latencies and throughputs
   of group (or function) executions.
   The default properties file is
      ../etc/group.properties

       -run              Runs test cases.

       -list             Lists data structures and their sizes.

       -db               Runs test cases on database instead of Redisson. To use this
                         option, each test case must supply a data object factory class
                         by specifying the 'factory.class' property and Hibernate must
                         be configured by running the 'build_app' command.

       -delete           Deletes (destroys) all the data structures pertaining to the group
                         test cases that were created in the Redisson cluster. If the '-run'
                         option is not specified, then it has the same effect as the '-list'
                         option. It only lists data strcutures and their without deleting them.

       <properties-file> Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/Padogrid/workspaces/myrwe/ws-intro/apps/perf_test/results

Notes:
   ICache requires explicit configuration. It will fail if you run it without first configuring
   the cluster with caches.
```

## Results

Upon successful run, the test results are outputted in the `results/` directory. The following shows an example.

```bash
cat ../results/ingestion-profile-190630-151618_x.txt
```

Output:

```console
******************************************
Data Ingestion Test
******************************************

                   Test Case: putall
                         Map: profile
           PutAll Batch Size: 100
              Test Run Count: 1
   Total Entry Count Per Run: 10000
                Thread Count: 16
        Payload Size (bytes): 10240
                      Prefix: x
      Entry Count per Thread: 625

Start Time: Sun Jun 30 15:16:18 EDT 2019

Actual Total Entry (Put) Count: 10000

Time unit: msec
   Thread 1: 660
   Thread 2: 664
   Thread 3: 664
   Thread 4: 664
   Thread 5: 662
   Thread 6: 658
   Thread 7: 662
   Thread 8: 662
   Thread 9: 660
   Thread 10: 664
   Thread 11: 662
   Thread 12: 656
   Thread 13: 655
   Thread 14: 653
   Thread 15: 653
   Thread 16: 651

        Max time (msec): 664
   Throughput (msg/sec): 15060.240
  *Throughput (KiB/sec): 150602.409
  *Throughput (MiB/sec): 147.072
 Latency per put (msec): 0.066
   **Total Volume (MiB): 100000
   **Total Volume (MiB): 97.656
   **Total Volume (GiB): 0.095
   Payload Size (bytes): 10240

 * Throughput does not take the keys into account.
   The actual rate is higher.
** Total Volume do not take the keys into account.
   The actual volume is higher.

Stop Time: Sun Jun 30 15:16:18 EDT 2019
```

## Inserting and Updating Database Tables

The `group_test -db` command directly loads mock data into database tables without connecting to Redisson. You can use this command to pre-populate the database before testing database synchronization tests in Redisson. This command is also useful for testing the CDC use case in which database changes are automatically ingested into Redisson via a CDC product such as Debezium ansd Striim.

```bash
# Edit setenv.sh to set the correct hibernate configuration file.
vi setenv.sh
```

By default, `setenv.sh` is configured with `hibernate.cfg-mysql.xml`. Change it to another if you are using a different database. Please see the `etc` directory for all the available database configuration files. If your database is not listed, then you can create one by copying one of the `hibernate-*` files and specifying that file name in the `setenv.sh` file.

Make sure to set the correct database user name and password in the Hibernate configuration file.

```bash
# Hibernate
JAVA_OPTS="$JAVA_OPTS -Dhazelcast-addon.hibernate.config=$APP_ETC_DIR/hibernate.cfg-mysql.xml"
```

Run `test_group -db`.

```bash
./test_group -db -run -prop ../etc/group-factory.properties
```

## Generating Entity Relationships (ER)

If you want to add entity relationships to your data, then you can implement [`DataObjectFactory`](https://github.com/padogrid/padogrid/blob/develop/hazelcast-addon-core-5/src/test/java/org/hazelcast/addon/test/perf/data/DataObjectFactory.java) or extend [`AbstractDataObjectFactory`](https://github.com/padogrid/padogrid/blob/develop/hazelcast-addon-core-5/src/test/java/org/hazelcast/demo/nw/impl/AbstractDataObjectFactory.java) and pass the object key to the `createEntry()` method using the `factory.er.operation` property. The `perf_test` app includes an ER example that creates one-to-many ER between `Customer` and `Order` objects by setting `Customer.customerId` to `Order.customerId` while ingesting mock data. Please see [`org.hazelcast.demo.nw.impl.OrderFactoryImpl`](https://github.com/padogrid/padogrid/blob/develop/hazelcast-addon-core-5/src/test/java/org/hazelcast/demo/nw/impl/OrderFactoryImpl.java) for details. You can run the example as follows:

```bash
./test_group -run -prop ../etc/group-factory-er.properties
```

The ER capbility provides you a quick way to ingest co-located data into Redisson and test server-side operations that take advatange of data affinity.
