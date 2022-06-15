# Geode `perf_test` App

The `perf_test` app provides Geode client programs to perform the following:

- Ingest mock data of any size
- Ingest transactional data of any size
- Ingest mock data with entity relationships (ER)
- Ingest mock data directly to databases
- Simulate complex appliation workflows that invoke Geode operations without coding
- Measure Geode latencies and throughputs in a multi-threaded user session environment

The `perf_test` app provides a pair of scripts to ingest and transact mock data for testing Geode throughputs and latencies. It provides a quick way to run your performance tests by configuring a few properties such as the payload size, the number of objects (entries), and the number of worker threads. Out of the box, these properties have already been pre-configured in `etc/ingestion.properties` and `etc/tx.properties`, which you can modify as needed to meet your test criteria.

The `perf_test` app also includes the `test_group` script that allows you to configure one or more groups of `Region` operations and execute them in parallel. A group is analogous to a function that makes multiple `Region` method calls in the order they are specified in the `etc/group.properties` file. The `etc` directory also contains the `group-put.properties` and `group-get.properties` files that have been preconfigured to invoke 22 put calls and 22 get calls on 22 different regions. You can configure the Near Cache in `etc/geode-client.xml` to measure the throughput. There are also several additional example `group-*.properties` files. You can also configure a local or near cache in `etc/client-cache.xml` to measure the improved throughput. 

You can also ingest `Customer` and `Order` domain objects with mock data. These objects have been annotated with Hibernate such that you can synchronize Geode with a database of your choice. See the [CacheWriterLoaderPkDbImpl (Database Integration)](#cachewriterloaderpkdbimpl-database-integration) section for configuration instructions.

## Transaction Test Cases

All of the transaction test cases are performed on three (3) partitioned regions with a simple PBM (Pharmacy Benefit Management) data model that associates the client group number with group members. Simply put, all of the members that belong to a group are co-located in the same Geode partition. This enables each Geode member to complete transactions with their local datasets without encountering additional network hops.

|Region    | Description | Script |
|------ | ------------| ------ |
|`eligibility` | The `eligibility` region contains `EligKey` and `Blob` entries. `EligKey` contains core member eligibility information and `Blob` contains a byte array as as a payload. The byte array size determines the size of the `Blob` object and it is configurable in `etc/ingestion.properties`.| `bin_sh/test_ingestion` |
| `profile`    | The `profile` region contains `ClientProfileKey` and `Blob` entries. `ClientProfileKey` contains core client information and `Blob` as described above.| `bin_sh/test_ingestion` |
| `summary`    | The `summary` region contains `groupNumber` and `GroupSummary` entries. `GroupSummary` contains the group summary results produced when the `tx` test case is performed by running the `bin_sh/test_tx` script.| `bin_sh/test_tx` |

## Configuration Files


There are two configuration files with preconfigured properties as follows:
- `etc/ingestion.properties` - This file defines properties for ingesting data into the `eligibility` and `profile` regions.
- `etc/tx.properties` - This file defines properties for performing transactions.
- `etc/group.properties` - This file defines properties for performing groups of `Region` method calls.
- `etc/group-put.properties` - This file defines properties for making 22 put calls on 22 different regions in a single group.
- `etc/group-get.properties` - This file defines properties for making 22 get calls on 22 different regions in a single group. Note that group-put must be invoked first to ingest data. 

| Properties File | Description |
| --------------- | ----------- |
| `ingestion.properties` | Defines properties for ingesting data into the `eligibility` and `profile` regions. |
| `tx.properties`        | Defines properties for performing transactions. |
| `group.properties`     | Defines properties for performing groups of `Region` method calls. |
| `group-put.properties` | Defines properties for making 22 put calls on 22 different regions in a single group. |
| `group-get.properties` | Defines properties for making 22 get calls on 22 different regions in a single group. Note that before invoking this file, `group-put.properties` must be invoked first to ingest data. |
| `group-factory.properties` | Defines properties for ingesting mock data. |
| `group-factory-er.properties` | Defines properties for ingesting mock data with entity relationships. |

You can introduce your own test criteria by modifying the properties the above files or supply another properties file by specifying the `-prop` option of the scripts described below.

## Scripts

The `bin_sh/` directory contains the following scripts. By default, these scripts simply prints the configuration information obtained from the `etc/perf.properties` file. To run the test cases, you must specify the `-run` option.

| Script | Description |
| ------ | ----------- |
| `test_ingestion` | Displays or runs data ingestion test cases (`putall` or `put`) specified in the `etc/ingestion.properties` file. It ingests mock data into the `eligibility` and `profile` regions. |
| `test_tx` | Displays or runs transaction and query test cases specified in the `etc/tx.properties` file. It runs `get`, `getall`, `tx` test cases specified in the `perf.properties` file. |
| `test_group` | Displays or runs group test cases (`put`, `putall`, `get`, `getall`). A group represents a function that executes one or more Geode `Region` operations. |

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

       -run               Run test cases.
       <properties-file>  Optional properties file path.

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

       -run               Run test cases.
       <properties-file>  Optional properties file path.

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
   test_group [-run|-list] [-db|-clear] [-prop <properties-file>] [-?]

   Displays or runs group test cases specified in the properties file.
   A group represents a function that executes one or more Geode/GemFire Region
   operations. This program measures average latencies and throughputs
   of group (or function) executions.
   The default properties file is
      ../etc/group.properties

       -run              Run test cases.

       -list             Lists data structures and their sizes.

       -db               Runs test cases on database instead of Geode/GemFire. To use this
                         option, each test case must supply a data object factory class
                         by specifying the 'factory.class' property and Hibernate must
                         be configured by running the 'build_app' command.

       -clear            Clears all the data structures pertaining to the group test cases that
                         were created in the Geode/GemFire cluster. If the '-run' option is not
                         ospecified, then it has the same effect as the '-list' option. It only
                         lists data strcutures and their without deleting them.

                         Note that the 'clear' operation is an extension to the Geode/GemFire API.
                         Geode/GemFire does not allow clearing partitioned regions in particuar.
                         To use this operation, you must register the following function found in
                         the 'geode-addon-core' plugin. By default, this function is registered
                         in the Geode cluster's 'etc/cache.xml' file.

                             org.apache.geode.addon.function.ClearFunction

       <properties-file> Optional properties file path.

   To run the the test cases, specify the '-run' option. Upon run completion, the results
   will be outputted in the following directory:
      /Users/dpark/Padogrid/workspaces/rwe-gemfire/ws-spring/apps/perf_test/results
```

### CacheWriterLoaderPkDbImpl (Database Integration)

`geode-addon` includes a generic DB addon, `org.geode.addon.cluster.CacheWriterLoaderPkDbImpl`, that can read/write from/to a database. This primary-key-based DB addon regions your Hibernate entity objects to database tables. To use the plugin, the primary key must be part of the entity object annotated with Hibernate's `@Id`. The following is a snippet of the `Order` object included in `geode-addon`.

```java
@Entity
@Table(name = "orders")
public class Order implements PdxSerializable, Comparable<Order>
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

`perf_test` is equipped with a test harness to experiment the `CacheWriterLoaderPkDbImpl` addon. It comes with MySQL and PostgreSQL Hibernate configuration files. If you have a different database or JDBC driver then you can include it in the `pom.xml` file. 

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

To use `CacheWriterLoaderPkDbImpl `, you must first build the environment by executing the `build_app` script as shown below. This script runs Maven to download the dependency files into the `$PADOGRID_WORKSPACE/lib` directory, which is included in `CLASSPATH` for all the apps and clusters running in the workspace.

:pencil2: If your application does not require Geode locally installed then you can set `GEODE_VERSION` in the `setenv.sh` file. If this environment variable is set then the `build_app` script downloads the specified version of Geode jar files.

```bash
./build_app
```

Upon successful build, you must also configure the cluster in `cache.xml` file as follows:

```bash
# Edit cache.xml
vi $PADOGRID_WORKSPACE/clusters/$CLUSTER/etc/cache.xml
```

Add the following in the `cache.xml` file.

```xml
	<region-attributes id="customerStore">
        <cache-loader>
            <class-name>org.apache.geode.addon.cluster.CacheWriterLoaderPkDbImpl</class-name>
            <parameter name="region.path"><string>/nw/customers</string></parameter>
            <parameter name="entity.class"><string>org.apache.geode.addon.demo.nw.data.Customer</string></parameter>
            <parameter name="entity.load.limit"><string>1000</string></parameter>
            <parameter name="entity.load.batch.size"><string>100</string></parameter>
            <parameter name="entity.db.isRead"><string>true</string></parameter>
            <parameter name="entity.db.isWrite"><string>true</string></parameter>
            <parameter name="entity.db.isDelete"><string>false</string></parameter>
        </cache-loader>
        <cache-writer>
            <class-name>org.apache.geode.addon.cluster.CacheWriterLoaderPkDbImpl</class-name>
            <parameter name="region.path"><string>/nw/customers</string></parameter>
            <parameter name="entity.class"><string>org.apache.geode.addon.demo.nw.data.Customer</string></parameter>
            <parameter name="entity.load.limit"><string>1000</string></parameter>
            <parameter name="entity.load.batch.size"><string>100</string></parameter>
            <parameter name="entity.db.isRead"><string>true</string></parameter>
            <parameter name="entity.db.isWrite"><string>true</string></parameter>
            <parameter name="entity.db.isDelete"><string>false</string></parameter>
        </cache-writer>
    </region-attributes>
    
    <region-attributes id="orderStore">
        <cache-loader>
            <class-name>org.apache.geode.addon.cluster.CacheWriterLoaderPkDbImpl</class-name>
            <parameter name="region.path"><string>/nw/orders</string></parameter>
            <parameter name="entity.class"><string>org.apache.geode.addon.demo.nw.data.Order</string></parameter>
            <parameter name="entity.load.limit"><string>1000</string></parameter>
            <parameter name="entity.load.batch.size"><string>100</string></parameter>
            <parameter name="entity.db.isRead"><string>true</string></parameter>
            <parameter name="entity.db.isWrite"><string>true</string></parameter>
            <parameter name="entity.db.isDelete"><string>false</string></parameter>
        </cache-loader>
        <cache-writer>
            <class-name>org.apache.geode.addon.cluster.CacheWriterLoaderPkDbImpl</class-name>
            <parameter name="region.path"><string>/nw/orders</string></parameter>
            <parameter name="entity.class"><string>org.apache.geode.addon.demo.nw.data.Order</string></parameter>
            <parameter name="entity.load.limit"><string>1000</string></parameter>
            <parameter name="entity.load.batch.size"><string>100</string></parameter>
            <parameter name="entity.db.isRead"><string>true</string></parameter>
            <parameter name="entity.db.isWrite"><string>true</string></parameter>
            <parameter name="entity.db.isDelete"><string>false</string></parameter>
        </cache-writer>
    </region-attributes>
    
    <region name="nw">
		<region name="customers" refid="PARTITION">
			<region-attributes refid="customerStore" />
		</region>
        <region name="orders" refid="PARTITION">
			<region-attributes refid="orderStore" />
		</region>
    </region>
```

The above configures the `/nw/customers` and `/nw/orders` regions to store and load data to/from the database. The database can be configured in the cluster's `hibernate.cfg.xml` file as follows:

```bash
# Edit hibernate.cfg.xml
vi $PADOGRID_WORKSPACE/clusters/<your-cluster>/etc/hibernate.cfg-mysql.xml
vi $PADOGRID_WORKSPACE/clusters/<your-cluster>/etc/hibernate.cfg-postresql.xml
```

The following is the `hibernate.cfg-mysql.xml` file provided by `geode-addon`. Make sure to replace the database information with your database information such as *password*.

```xml
<hibernate-configuration>
	<session-factory>
		<!-- JDBC Database connection settings -->
		<property name="connection.driver_class">com.mysql.cj.jdbc.Driver</property>
		<property name="connection.url">jdbc:mysql://localhost:3306/nw?allowPublicKeyRetrieval=true&amp;useSSL=false</property>
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
		<property name="hibernate.connection.serverTimezone">UTC</property>
		
		<!-- c3p0 connection pool -->
		<property name="hibernate.connection.provider_class">
			org.hibernate.connection.C3P0ConnectionProvider
		</property>
		<property name="hibernate.c3p0.min_size">5</property>
		<property name="hibernate.c3p0.max_size">10</property>
		<property name="hibernate.c3p0.acquire_increment">1</property>
		<property name="hibernate.c3p0.idle_test_period">3000</property>
		<property name="hibernate.c3p0.max_statements">50</property>
		<property name="hibernate.c3p0.timeout">1800</property>
		<property name="hibernate.c3p0.validate">1800</property>
		
		<!-- Disable the second-level cache -->
		<property name="cache.provider_class">org.hibernate.cache.internal.NoCacheProvider</property>
		<mapping class="org.apache.geode.addon.demo.nw.data.Customer" />
		<mapping class="org.apache.geode.addon.demo.nw.data.Order" />
	</session-factory>
</hibernate-configuration>
```

The Hibernate configuration file path must be provided before you start the cluster. Edit the cluster's `setenv.sh` file and include the path as follows.

```bash
vi $PADOGRID_WORKSPACE/clusters/$CLUSTER/bin_sh/setenv.sh
```
In `setenv.sh`, add the following:

```bash
# Set JAVA_OPTS in setenv.sh. Remember that geode-addon uses gfsh to start/stop
# locators and members. For Java options, you must prepend '--J='.
HIBERNATE_CONFIG_FILE="$CLUSTER_DIR/etc/hibernate.cfg-mysql.xml"
if [[ ${OS_NAME} == CYGWIN* ]]; then
   HIBERNATE_CONFIG_FILE="$(cygpath -wp "$HIBERNATE_CONFIG_FILE")"
fi
JAVA_OPTS="$JAVA_OPTS --J=-Dgeode-addon.hibernate.config=$HIBERNATE_CONFIG_FILE"
```

You can now run the cluster.

```bash
# After making the above changes, start the cluster
start_cluster -cluster <cluster-name>
```

Once the cluster is up, you are ready to run the `test_group` script to insert `Customer` and `Order` entity objects in to the cluster and database. Run the script as follows:

```bash
./test_group -prop ../etc/group-factory.properties -run
```

You can reconfigure `group-factory.properties` to add more data, threads, etc.

## Results

Upon successful run, the test results are outputted in the `results/` directory. The following shows an example.

```bash
cat ../results/ingestion-profile-200114-112146_x.txt
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

Start Time: Tue Jan 14 11:21:46 EDT 2020

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

Stop Time: Tue Jan 14 11:21:46 EDT 2020
```

## Inserting and Updating Database Tables

The `group_test -db` command directly loads mock data into database tables without connecting to Geode. You can use this command to pre-populate the database before testing database synchronization tests in Geode. This command is also useful for testing the CDC use case in which database changes are automatically ingested into Geode via a CDC product such as Debezium.

```bash
# Edit setenv.sh to set the correct hibernate configuration file.
vi setenv.sh
```

By default, `setenv.sh` is configured with `hibernate.cfg-mysql.xml`. Change it to another if you are using a different database. Please see the `etc` directory for all the available database configuration files. If your database is not listed, then you can create one by copying one of the `hibernate-*` files and specifying that file name in the `setenv.sh` file.

Make sure to set the correct database user name and password in the Hibernate configuration file.

```bash
# Hibernate
JAVA_OPTS="$JAVA_OPTS -Dgeode-addon.hibernate.config=$APP_ETC_DIR/hibernate.cfg-mysql.xml"
```

Run `test_group -db`.

```bash
./test_group -db -run -prop ../etc/group-factory.properties
```

## Generating Entity Relationships (ER)

If you want to add entity relationships to your data, then you can implement [`DataObjectFactory`](https://github.com/padogrid/padogrid/blob/develop/geode-addon-core-5/src/test/java/org/geode/addon/test/perf/data/DataObjectFactory.java) or extend [`AbstractDataObjectFactory`](https://github.com/padogrid/padogrid/blob/develop/geode-addon-core-5/src/test/java/org/geode/demo/nw/impl/AbstractDataObjectFactory.java) and pass the object key to the `createEntry()` method using the `factory.er.operation` property. The `perf_test` app includes an ER example that creates one-to-many ER between `Customer` and `Order` objects by setting `Customer.customerId` to `Order.customerId` while ingesting mock data. Please see [`org.apache.geode.addon.demo.nw.impl.OrderFactoryImpl`](https://github.com/padogrid/padogrid/blob/develop/geode-addon-core/src/test/java/org/apache/geode/addon/demo/nw/impl/OrderFactoryImpl.java) for details. You can run the example as follows:

```bash
./test_group -run -prop ../etc/group-factory-er.properties
```

The ER capbility provides you a quick way to ingest co-located data into Geode and test server-side operations that take advatange of data affinity.
