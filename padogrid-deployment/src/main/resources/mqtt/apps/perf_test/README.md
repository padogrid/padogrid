# MQTT `perf_test` App

The `perf_test` app provides MQTT client programs using the [PadoGrid's MQTT HA API, `HaMqttClient`](https://github.com/padogrid/padogrid/blob/develop/padogrid-mqtt/README.md), to perform the following:

- Ingest mock data of any size
- Ingest mock data with entity relationships (ER)
- Simulate complex application workflows that invoke MQTT operations without coding
- Measure MQTT latencies and throughputs in a multi-threaded user session environment

The `perf_test` app provides a pair of scripts to ingest and transact mock data for testing MQTT throughputs and latencies. It provides a quick way to run your performance tests by configuring a few properties such as the payload size, the number of objects (entries), and the number of worker threads. Out of the box, these properties have already been pre-configured in `etc/group-*.properties`, which you can modify as needed to meet your test criteria.

The `perf_test` app includes the `test_group` script that allows you to configure one or more groups of MQTT data structure operations and execute them in sequence and/or in parallel. A group is analogous to a function that makes multiple data structure method calls in the order they are specified in the `etc/group.properties` file. The `etc` directory also contains the `group-publish.properties` file for demonstrating complex workflows that invoke 22 publish calls on 22 different topics. There are also several example `group-*.properties` files. You can also configure the MQTT publisher in `etc/mqtt-publisher.properties` to measure the improved throughput. 

## Configuration Files

The following table describes a list of preconfigured properties files in the `etc/` directory.

| Properties File | Description |
| --------------- | ----------- |
| `group.properties`      | Defines properties for performing groups of `MqttClient.publish()` method calls. |
| `group-publish.properties` | Defines properties for making 22 publish calls on 22 different topics in a single group. |
| `group-publish-sleep.properties` | Defines properties for testing the sleep operation. |
| `group-topic.properties` | Defines properties for MqttClient.publish() operations. |

You can introduce your own test criteria by modifying the properties the above files or supply another properties file by specifying the `-prop` option of the scripts described below.

## Scripts

The `bin_sh/` directory contains the following scripts. By default, these scripts simply prints the configuration information obtained from the `etc/<perf>.properties` file. To run the test cases, you must specify the `-run` option.

| Script | Default Config | Description |
| ------ | -------------- | ----------- |
| `test_group` | etc/group.properties | Displays or runs group test cases (`publish`, `sleep`). A group represents a function that executes one or more MQTT client operations. |

## Script Usages

### test_group

```bash
./test_group -?
```

Output:

```console
Usage:
   test_group [-run] [-prop <properties-file>] [-?]

   Displays or runs group test cases specified in the properties file.
   A group represents a function that executes one or more MQTT data structure
   operations. This program measures average latencies and throughputs of
   group (or function) executions.

   The default properties file is
      ../etc/group.properties

       -run              Runs test cases.

       <properties-file> Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/Padogrid/workspaces/rwe-products/myws/apps/perf_test_mosquitto/results
```

## Results

Upon successful run, the test results are outputted in the `results/` directory. The following shows an example.

```bash
cat ../results/group-g1-mqtt-230421-145958.txt
```

Output:

```console
******************************************
Group Test
******************************************

                       Product: mqtt
                         Group: g1
           Concurrent Group(s): g1
                       Comment: MqttClient.publish() test (1 KiB payload in topic1)
                    Operations: publish1
                Test Run Count: 1
      Test Run Interval (msec): 0
Total Invocation Count per Run: 100000
                  Thread Count: 8
   Invocation Count per Thread: 12500

Start Time: Fri Apr 21 14:59:58 EDT 2023

Actual Total Number of Invocations: 100000

Time unit: msec
   Thread 1: 2314
   Thread 2: 2314
   Thread 3: 2313
   Thread 4: 2313
   Thread 5: 2316
   Thread 6: 2293
   Thread 7: 2318
   Thread 8: 2312

                Max Time (msec): 2318
            Elapsed Time (msec): 2321
         Total Invocation Count: 100000
 M Throughput (invocations/sec): 43140.6385
M Latency per invocation (msec): 0.0232
 E Throughput (invocations/sec): 43084.8772
E Latency per invocation (msec): 0.0232

Stop Time: Fri Apr 21 15:00:00 EDT 2023
```
