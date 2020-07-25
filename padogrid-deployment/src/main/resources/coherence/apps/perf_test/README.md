# Coherence `perf_test` App

The `perf_test` app provides scripts to ingest and transact mock data for testing Coherence throughputs and latencies. It provides a quick way to run your performance tests by configuring a few properties such as the payload size, the number of objects (entries), and the number of worker threads. Out of the box, these properties have already been pre-configured in `etc/ingestion.properties` and `etc/tx.properties`, which you can modify as needed to meet your test criteria.

The `perf_test` app also includes the `test_group` script that allows you to configure one or more groups of `NamedCache` operations and execute them in parallel. A group is analogous to a function that makes multiple `NamedCache` method calls in the order they are specified in the `etc/group.properties` file. The `etc` directory also contains the `group-put.properties` and `group-get.properties` files that have been preconfigured to invoke 22 put calls and 22 get calls on 22 different caches. You can configure the Near Cache in `etc/client-config.xml` to measure the throughput.

You can also ingest `Customer` and `Order` domain objects with mock data. These objects have been annotated with Hibernate such that you can synchronize Coherence with a database of your choice. See the [CacheWriterLoaderPkDbImpl (Database Integration)](#cachewriterloaderpkdbimpl-database-ntegration) section for configuration instructions.

## NamedCaches

All the test cases are performed on three (3) partitioned caches with a simple PBM (Pharmacy Benefit Management) data model that associates the client group number with group members. Simply put, all of the members that belong to a group are co-located in the same Coherence partition. This enables each Coherence member to complete transactions with their local datasets without encountering additional network hops.

|NamedCache    | Description | Script |
|------ | ------------| ------ |
|`eligibility` | The `eligibility` cache contains `EligKey` and `Blob` entries. `EligKey` contains core member eligibility information and `Blob` contains a byte array as as a payload. The byte array size determines the size of the `Blob` object and it is configurable in `etc/ingestion.properties`.| `bin_sh/test_ingestion` |
| `profile`    | The `profile` cache contains `ClientProfileKey` and `Blob` entries. `ClientProfileKey` contains core client information and `Blob` as described above.| `bin_sh/test_ingestion` |
| `summary`    | The `summary` cache contains `groupNumber` and `GroupSummary` entries. `GroupSummary` contains the group summary results produced when the `tx` test case is performed by running the `bin_sh/test_tx` script.| `bin_sh/test_tx` |

## Configuration Files

There are two configuration files with preconfigured properties as follows:
- `etc/ingestion.properties` - This file defines properties for ingesting data into the `eligibility` and `profile` caches.
- `etc/tx.properties` - This file defines properties for performing transactions.
- `etc/group.properties` - This file defines properties for performing groups of `NamedCache` method calls.
- `etc/group-put.properties` - This file defines properties for making 22 put calls on 22 different caches in a single group.
- `etc/group-get.properties` - This file defines properties for making 22 get calls on 22 different caches in a single group. Note that group-put must be invoked first to ingest data. 

You can introduce your own test criteria by modifying the properties the above files or supply another properties file by specifying the `-prop` option of the scripts described below.

## Scripts

The `bin_sh/` directory contains the following scripts. By default, these scripts simply prints the configuration information obtained from the `etc/perf.properties` file. To run the test cases, you must specify the `-run` option.

| Script | Description |
| ------ | ----------- |
| `test_ingestion` | Displays or runs data ingestion test cases (`putall` or `put`) specified in the `etc/ingestion.properties` file. It ingests mock data into the `eligibility` and `profile` caches. |
| `test_tx` | Displays or runs transaction and query test cases specified in the `etc/tx.properties` file. It runs `get`, `getall`, `tx` test cases specified in the `perf.properties` file. |
| `test_group` | Displays or runs group test cases (`put`, `putall`, `get`, `getall`). A group represents a function that executes one or more Coherence NamedCache operations. |

## Script Usages

### test_ingestion

```console
./test_ingestion -?

Usage:
   test_ingestion [-run] [-prop <properties-file>] [-?]

   Displays or runs data ingestion test cases specified in the properties file.
   The default properties file is
      ../etc/ingestion.properties

       -run               Run test cases.
       <properties-file>  Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/padogrid/workspaces/myrwe/ws-intro/apps/perf_test/results
```

### test_tx

```console
./test_tx -?

Usage:
   test_tx [-run] [-prop <properties-file>] [-?]

   Displays or runs transaction and query test cases specified in the properties file.
   The default properties file is
      ../etc/tx.properties

       -run               Run test cases.
       <properties-file>  Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/padogrid/workspaces/myrwe/ws-intro/apps/perf_test/results
```

### test_group

```console
Usage:
   test_group [-run] [-prop <properties-file>] [-?]

   Displays or runs group test cases specified in the properties file.
   A group represents a function that executes one or more Coherence NamedCache
   operations. This program measures average latencies and throughputs
   of group (or function) executions.
   The default properties file is
      ../etc/group.properties

       -run                Run test cases
        <properties-file>  Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/padogrid/workspaces/myrwe/ws-intro/apps/perf_test/results
```

## Running `test_group`

The `test_group` script requires you to first build the environment by running `build_app` which downloads the required libraries.

```bash
./build_app
```

The `test_group` script inserts `Customer` and `Order` entity objects in to the cluster.

```console
./test_group -prop ../etc/group-factory.properties -run
```

You can reconfigure `group-factory.properties` to add more data, threads, etc.

## Results

Upon successful run, the test results are outputted in the `results/` directory. The following shows an example.

```bash
cat ../results/ingestion-profile-200531-153845_x.txt
```

**Output:**

```console
******************************************
Data Ingestion Test
******************************************

                   Test Case: putall
                         Map: profile
           PutAll Batch Size: 100
              Test Run Count: 1
    Test Run Interval (msec): 0
   Total Entry Count Per Run: 10000
                Thread Count: 16
        Payload Size (bytes): 10240
                      Prefix: x
      Entry Count per Thread: 625

Start Time: Sun May 31 15:38:45 EDT 2020

Actual Total Entry (Put) Count: 10000

Time unit: msec
   Thread 1: 1067
   Thread 2: 1066
   Thread 3: 1110
   Thread 4: 1110
   Thread 5: 1095
   Thread 6: 1117
   Thread 7: 1109
   Thread 8: 1094
   Thread 9: 1112
   Thread 10: 1098
   Thread 11: 1108
   Thread 12: 1060
   Thread 13: 1069
   Thread 14: 1108
   Thread 15: 1107
   Thread 16: 1102

        Max time (msec): 1117
   Throughput (msg/sec): 8952.551
  *Throughput (KiB/sec): 89525.515
  *Throughput (MiB/sec): 87.427
 Latency per put (msec): 0.112
   **Total Volume (MiB): 100000
   **Total Volume (MiB): 97.656
   **Total Volume (GiB): 0.095
   Payload Size (bytes): 10240

 * Throughput does not take the keys into account.
   The actual rate is higher.
** Total Volume do not take the keys into account.
   The actual volume is higher.

Stop Time: Sun May 31 15:38:47 EDT 2020
```
