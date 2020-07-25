# Hazelcast `perf_test` App

The `perf_test` app provides a pair of scripts to ingest and transact mock data for testing Hazelcast throughputs and latencies. It provides a quick way to run your performance tests by configuring a few properties such as the payload size, the number of objects (entries), and the number of worker threads. Out of the box, these properties have already been pre-configured in `etc/ingestion.properties` and `etc/tx.properties`, which you can modify as needed to meet your test criteria.

The `perf_test` app also includes the `test_group` script that allows you to configure one or more groups of IMap operations and execute them in parallel. A group is analogous to a function that makes multiple IMap method calls in the order they are specified in the `etc/group.properties` file. The `etc` directory also contains the `group-put.properties` and `group-get.properties` files that have been preconfigured to invoke 22 put calls and 22 get calls on 22 different maps. You can configure the Near Cache in `etc/hazelcast-client.xml` to measure the throughput. 

## Maps

All of the test cases are performed on three (3) distributed maps with a simple PBM (Pharmacy Benefit Management) data model that associates the client group number with group members. Simply put, all of the members that belong to a group are co-located in the same Hazelcast partition. This enables each Hazelcast member to complete transactions with their local datasets without encountering additional netowork hops.

|Map    | Description | Script |
|------ | ------------| ------ |
|`eligibility` | The `eligibility` map contains `EligKey` and `Blob` entries. `EligKey` contains core member eligibility information and `Blob` contains a byte array as as a payload. The byte array size determines the size of the `Blob` object and it is configurable in `etc/ingestion.properties`.| `bin_sh/test_ingestion` |
| `profile`    | The `profile` map contains `ClientProfileKey` and `Blob` entries. `ClientProfileKey` contains core client information and `Blob` as described above.| `bin_sh/test_ingestion` |
| `summary`    | The `summary` map contains `groupNumber` and `GroupSummary` entries. `GroupSummary` contains the group summary results produced when the `tx` test case is performed by running the `bin_sh/test_tx` script.| `bin_sh/test_tx` |

## Configuration Files

There are two configuration files with pref-configured properties as follows:
- `etc/ingestion.properties` - This file defines properties for ingesting data into the `eligibility` and `profile` maps.
- `etc/tx.properties` - This file defines properties for performing transactions.
- `etc/group.properties` - This file defines properties for performing groups of IMap method calls.
- `etc/group-put.properties` - This file defines properties for making 22 put calls on 22 different maps in a single group.
- `etc/group-get.properties` - This file defines properties for making 22 get calls on 22 different maps in a single group. Note that group-put must be invoked first to ingest data. 

You can introduce your own test criteria by modifying the properties the above files or supply another properties file by specifying the `-prop` option of the scripts described below.

## Scripts

The `bin_sh/` directory contains the following scripts. By default, these scripts simply prints the configuration information obtained from the `etc/perf.properties` file. To run the test cases, you must specify the `-run` option.

| Script | Description |
| ------ | ----------- |
| `test_ingestion` | Displays or runs data ingestion test cases (`putall` or `put`) specified in the `etc/ingestion.properties` file. It ingests mock data into the `eligibility` and `profile` maps. |
| `test_tx` | Displays or runs transaction and query test cases specified in the `etc/tx.properties` file. It runs `get`, `getall`, `tx` test cases specified in the `perf.properties` file. |
| `test_group` | Displays or runs group test cases (`set`, `put`, `putall`, `get`, `getall`). A group represents a function that executes one or more Hazelcast IMap operations. |

## Script Usages

### test_ingestion

```console
./test_ingestion -?

Usage:
   test_ingestion [-run] [-prop <properties-file>] [-?]

   Displays or runs data ingestion test cases specified in the properties file.
   The default properties file is
      ../etc/ingestion.properties

       -run              Run test cases.
       -failover         Configure failover client using the following config file:
                            ../etc/hazelcast-client-failover.xml
       <properties-file> Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/padogrid/workspaces/myrwe/ws-intro/apps/perf_test/results
```

### test_tx

```console
Usage:
   test_tx [-run] [-failover] [-prop <properties-file>] [-?]

   Displays or runs transaction and query test cases specified in the properties file.
   The default properties file is
      ../etc/tx.properties

       -run              Run test cases.
       -failover         Configure failover client using the following config file:
                            ../etc/hazelcast-client-failover.xml
       <properties-file> Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/padogrid/workspaces/myrwe/ws-intro/apps/perf_test/results
```

### test_group

```console
Usage:
   test_group [-run] [-prop <properties-file>] [-?]

   Displays or runs group test cases specified in the properties file.
   A group represents a function that executes one or more Hazelcast IMap
   operations. This program measures average latencies and throughputs
   of group (or function) executions.
   The default properties file is
      ../etc/group.properties

       -run              Run test cases.
       -db               Run test cases on database instead of Hazelcast. To use this
                         option, each test case must supply a data object factory class
                         by specifying the 'factory.class' property and Hibernate must
                         be configured by running the 'build_app' command.
       -failover         Configure failover client using the following config file:
                           ../etc/hazelcast-client-failover.xml
       <properties-file> Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/padogrid/workspaces/myrwe/ws-intro/apps/perf_test/results
```

### MapStorePkDbImpl (Database Integration)

`padogrid` includes a generic DB addon, `org.hazelcast.addon.cluster.MapStorePkDbImpl`, that can read/write from/to a database. This primary-key-based DB addon maps your Hibernate entity objects to database tables. To use the plugin, the primary key must be part of the entity object annotated with Hibernate's `@Id`. The following is a snippet of the `Order` object included in `padogrid`.

```java
@Entity
@Table(name = "orders")
public class Order implements VersionedPortable, Comparable<Order>
{
	@Id
    @Column(length = 20)
	private String orderId;
	@Column(length = 20)
	private String customerId;
	@Column(length = 20)
	private String employeeId;
	@Column
	private Date orderDate;
    ...
}
```

`perf_test` is equipped with a test harness to experiment the `MapStorePkDbImpl` addon. It comes with MySQL and PostgreSQL Hibernate configuration files. If you have a different database or JDBC driver then you can include it in the `pom.xml` file. 

**pom.xml**

```xml
<dependencies>
        <dependency>
                <groupId>mysql</groupId>
                <artifactId>mysql-connector-java</artifactId>
                <version>8.0.13</version>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.2.8</version>
        </dependency>
        <dependency>
                <groupId>org.hibernate</groupId>
                <artifactId>hibernate-core</artifactId>
                <version>5.3.7.Final</version>
        </dependency>
        ...
```

To use `MapStorePkDbImpl`, you must first build the environment by executing the `build_app` script as shown below. This script runs Maven to download the dependency files into the `$PADOGRID_WORKSPACE/lib` directory, which is included in `CLASSPATH` for all the apps and clusters running in the workspace.

```console
./build_app
```

Upon successful build, you must also configure the cluster in `hazelcast.xml` file as follows:

```console
# Edit hazelcast.xml
vi $PADOGRID_WORKSPACE/clusters/<your-cluster>/etc/hazelcast.xml
```

Add the following in the `hazelcast.xml` file. Make sure to add `org.hazelcast.demo.nw.data.PortableFactoryImpl` for serialization as it is needed by `MapStorePkDbImpl` in the cluster to deserialize the `Portable` objects, `Customer` and `Order`.

```xml
	<serialization>
		<portable-version>1</portable-version>
		<portable-factories>
			<portable-factory factory-id="10000">
                org.hazelcast.addon.hql.impl.PortableFactoryImpl
            </portable-factory>
            <portable-factory factory-id="1">
                org.hazelcast.demo.nw.data.PortableFactoryImpl
            </portable-factory>
		</portable-factories>
	</serialization>
	<map name="nw/customers">
		<map-store enabled="true" initial-mode="EAGER">
			<class-name>org.hazelcast.addon.cluster.MapStorePkDbImpl</class-name>
			<properties>
				<property name="entity.class">org.hazelcast.demo.nw.data.Customer</property>
				<property name="entity.load.limit">1000</property>
				<property name="entity.load.batch.size">100</property>
				<property name="entity.db.isRead">true</property>
				<property name="entity.db.isWrite">true</property>
				<property name="entity.db.isDelete">false</property>
			</properties>
		</map-store>
	</map>
	<map name="nw/orders">
		<map-store enabled="true" initial-mode="EAGER">
			<class-name>org.hazelcast.addon.cluster.MapStorePkDbImpl</class-name>
			<properties>
				<property name="entity.class">org.hazelcast.demo.nw.data.Order</property>
				<property name="entity.load.limit">1000</property>
				<property name="entity.load.batch.size">100</property>
				<property name="entity.db.isRead">true</property>
				<property name="entity.db.isWrite">true</property>
				<property name="entity.db.isDelete">false</property>
			</properties>
			<write-batch-size>100</write-batch-size>
			<write-coalescing>true</write-coalescing>
			<write-delay-seconds>1</write-delay-seconds>
		</map-store>
	</map>
```

The above configures the `nw/customers` and `nw/orders` maps to store and load data to/from the database. The database can be configured in the cluster's `hibernate.cfg.xml` file as follows:

```console
# Edit hibernate.cfg.xml
vi $PADOGRID_WORKSPACE/clusters/<your-cluster>/etc/hibernate.cfg-mysql.xml
vi $PADOGRID_WORKSPACE/clusters/<your-cluster>/etc/hibernate.cfg-postresql.xml
```

The following is the `hibernate.cfg-mysql.xml` file provided by `padogrid`. Make sure to replace the database information with your database information.

```xml
<hibernate-configuration>
	<session-factory>
		<!-- JDBC Database connection settings -->
		<property name="connection.driver_class">com.mysql.cj.jdbc.Driver</property>
		<property name="connection.url">jdbc:mysql://localhost:3306/nw?useSSL=false</property>
		<property name="connection.username">root</property>
		<property name="connection.password">password</property>
		<!-- JDBC connection pool settings ... using built-in test pool -->
		<property name="connection.pool_size">10</property>
		<!-- Select our SQL dialect -->
		<property name="dialect">org.hibernate.dialect.MySQL5Dialect</property>
		<!-- Echo the SQL to stdout -->
		<property name="show_sql">true</property>
		<!-- Set the current session context -->
		<property name="current_session_context_class">thread</property>
		<!-- Update the database schema on startup -->
		<property name="hbm2ddl.auto">update</property>
		<!-- dbcp connection pool configuration -->
		<property name="hibernate.dbcp.initialSize">5</property>
		<property name="hibernate.dbcp.maxTotal">20</property>
		<property name="hibernate.dbcp.maxIdle">10</property>
		<property name="hibernate.dbcp.minIdle">5</property>
		<property name="hibernate.dbcp.maxWaitMillis">-1</property>
		<property name="hibernate.connection.serverTimezone">UTC</property>
        <!-- Disable the second-level cache -->
		<property name="cache.provider_class">org.hibernate.cache.internal.NoCacheProvider</property>
		<mapping class="org.hazelcast.demo.nw.data.Customer" />
		<mapping class="org.hazelcast.demo.nw.data.Order" />
	</session-factory>
</hibernate-configuration>
```

The Hibernate configuration file path must be provided before you start the cluster. Edit the cluster's `setenv.sh` file and include the path as follows:

```
vi $PADOGRID_WORKSPACE/clusters/<your-cluster>/bin_sh/setenv.sh

# Set JAVA_OPTS in setenv.sh
JAVA_OPTS="$JAVA_OPTS -Dpadogrid.hibernate.config=$CLUSTER_DIR/etc/hibernate.cfg-mysql.xml"
```

You can now run the cluster.

```
# After making the above changes, start the cluster
start_cluster -cluster <cluster-name>
```

Once the cluster is up, you are ready to run the `test_group` script to insert `Customer` and `Order` entity objects in to the cluster and database. Run the script as follows:

```console
./test_group -prop ../etc/group-factory.properties -run
```

You can reconfigure `group-factory.properties` to add more data, threads, etc.

## Results

Upon successful run, the test results are outputted in the `results/` directory. The following shows an example.

```
cat ../results/ingestion-profile-190630-151618_x.txt
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

The `group_test -db` command directly loads mock data into database tables without connecting to Hazelcast. You can use this command to pre-populate the database before testing database synchronization tests in Hazelcast. This command is also useful for testing the CDC use case in which database changes are automacally ingested into Hazelcast via a CDC product such as Debezium ansd Striim.

```console
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

```
./test_group -db -run -prop ../etc/group-factory.properties
```
