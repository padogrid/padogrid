# Hazelcast Desktop App

The Hazelcast Desktop app sits on top of [Netcrest Pado](https://github.com/netcrest/pado), which includes a run-time screen layout manager for dynamically laying out the screen with Swing components. This app is maintained by Netcrest in the following GitHub repo:

Repo: [https://github.com/netcrest/hazelcast-desktop](https://github.com/netcrest/hazelcast-desktop)

You can install the Hazelcast Deskop app by running `install_padogrid` or optionally build it by running `bin_sh/build_app`.

## Installing Hazelcast Desktop

```bash
# 1. Install hazelcast-desktop
install_padogrid -product hazelcast-desktop
update_products -product hazelcast-desktop

# 2. Create desktop app
create_app -app desktop

# 3. Copy any client jar files that contain domain classes, etc. into the plugins directory.
cd_app desktop
cp <your-domain-class-jars> plugins/
```

## Optional: Building Hazelcast Desktop (Dreprecated)

You can optionally build Hazelcast Desktop by running the `build_app` script found in the `bin_sh` directory.

:exclamation: *Note that `build_app` script has been deprecated as of PadoGrid v0.9.15.*

```bash
# 1. Create desktop app
create_app -app desktop

# 2. Build hazelcast-desktop
cd_app desktop/bin_sh
./build_app

# 3. Copy any client jar files that contain domain classes, etc. into the plugins directory.
cd ../hazelcast-desktop-<version>
cp <your-domain-class-jars> plugins/
```

## Running Hazelcast Desktop

```bash
# 1.1. If you have installed hazelcast-desktop using install_padogrid/update_products:
cd_app desktop

# 1.2. If you have built hazelcast-desktop:
cd_app desktop/hazelcast-desktop-<version>

# 2. Define ProtableFactory and/or DataSerializable factory classes in the etc/pado.properties file.
vi etc/pado.properties

# ProtableFactory
hazelcast.client.config.serialization.portable.factories=1:org.hazelcast.demo.nw.data.PortableFactoryImpl,\
10000:org.hazelcast.addon.hql.impl.PortableFactoryImpl

# DataSerializable
hazelcast.client.config.serialization.dataSerializable.factories=

# 3. Run desktop
cd bin_sh/
./desktop
```

### Configuring Hazelcast Client

If you prefer to configure Hazelcast client settings in `etc/hazelcast-client.xml` instead of `pado.properties`, then set `hazelcast.client.config.file.enabled=true` in the `etc/pado.properties` file.

## WSL Users

If you are running PadoGrid in WSL, then you will need to run X Server on Windows in order to run the desktop app as shown in the previous section. 

You can also run the desktop app without X Server by executing the `bin_win/desktop.bat` as follows.

```bash
# 1.1. If you have installed hazelcast-desktop using install_padogrid/update_products:
cd_app desktop/bin_win

# 1.2. If you have built hazelcast-desktop:
cd_app desktop/hazelcast-desktop-<version>/bin_win
```

Find the `JAVA_HOME` line in the `setenv.bat` file and set it to the Windows Java home path. If yo already have `JAVA_HOME` globally set in Windows then you can skip this step.

```dos
vi setenv.bat

REM Set Windows JAVA_HOME path.
@set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_212"
```

Run the desktop app as follows.

```bash
cmd.exe /c desktop.bat
```

## Quick Start with perf_test

You can use the `perf_test` app to ingest mock data into a Hazelcast cluster and test the desktop app as shown below.

1. Copy domain class serialization configuration from the desktop hazecast-client.xml

```bash
# 1.1. If you have installed hazelcast-desktop using install_padogrid/update_products:
cd_app desktop/etc
cat hazelcast-client.xml

# 1.2. If you have built hazelcast-desktop:
cd_app desktop/hazelcast-desktop-<version>/etc
cat hazelcast-client.xml
```

**Output:**

```xml
...
            <portable-factory factory-id="1">
                org.hazelcast.demo.nw.data.PortableFactoryImpl
            </portable-factory>
...
```

2. Copy and paste the above output in the cluster's `hazelcast.xml` file.

```bash
cd_cluster
vi etc/hazelcast.xml
```

3. Run cluster

```bash
start_cluster
```

4. Ingest data

```bash
create_app
cd_app perf_test; cd bin_sh
./test_group -prop ../etc/group-factory.properties -run
```

5. Run desktop

```bash
cd_app desktop; cd bin_sh
./desktop
```

## Configuring Hazelcast Client

The `hazelcast-client.xml` file for the desktop is located in the `etc/` directory as follows:

```bash
# 1.1. If you have installed hazelcast-desktop using install_padogrid/update_products:
cat $PADOGRID_WORKSPACE/apps/desktop/etc/hazelcast-client.xml

# 1.2. If you have built hazelcast-desktop:
cat $PADOGRID_WORKSPACE/apps/desktop/<hazelcast-desktop-<version>/etc/hazelcast-client.xml
```

## HqlQuery

The desktop app uses the `org.hazelcast.addon.hql.HqlQuery` class to execute queries in the Hazelcast cluster. HqlQuery (or HQL) supports the syntax shown below.

### HQL Syntax

```sql
select * <from-clause> [<where-clause>] [<order-by-clause>] [;]

    <from-clause>: from <map-name>[.keys|.values|.entries] [<alias>] 
   <where-clause>: where [<alias>-name>][.value.]<field-name>...
<order-by-clause>: order by [<alias>[.key.|.value.]]<field-name> [asc|desc]
```

### HQL Examples

```sql
-- Query both keys and values
select * from nw/orders.entries e;

-- Query values (if keys or entries are not specified in the map name then
-- it defaults to values):
select *
from nw/orders
where customerId='002132-6756' or freigt>10 and freight<100
order by customerId asc, orderId desc;
 
-- Query entries (keys and values) alias:
select * 
from nw/orders.entries e
where e.value.customerId='002132-6756' or e.value.freight>10 and e.value.freight<100 
order by e.value.customerId asc, e.value.orderId desc;
 
-- Query keys (returns keys only):
--    Note that at the time of writing, Hazelcast does not support query
--    executions on composite keys (Hazelcast version 3.12)
select *
from nw/orders.keys k
where k like '%8' order by k desc;

-- Query values sorted by objects themselves in descending order. 
-- The value objects must implement Comparable.
select *
from nw/orders v order by v desc;

-- LIKE example: List orders with orderId ends with 8
select * from nw/orders where orderId like '%8' order by orderId;

-- IN example
select * from nw/orders.entries e 
where e.value.customerId in  ('004232+3790', '006513-2682', '007524-5127') 
order by e.value.customerId;

-- REGEX example
select * from nw/orders
where shipCity regex 'West.*'
order by customerId;

-- BETWEEN example (inclusive)
select * from nw/orders 
where freight between .4 and .8
order by freight;
```

## Screenshot

![Desktop Screenshot](/images/desktop-screenshot.png)

## Running Hazelcast Desktop in Docker Container

If you have X Server running in your host machine then you can run the desktop app as follows.

### macOS

1. Install XQuarts: https://www.xquartz.org/

2. Open XQuarts and activate **Allow connections from network clients** under **Preferences > Security**.

3. Reboot macOS (This is required.)

4. Start XQuartz upon macOS reboot.

5. Open **Terminal* from the XQuartz menu.

6. From the terminal (xterm), run `xhost` to allow client connection.

```bash
# Disable access control to allow clients to connect from any host
xhost +
```

7. Run PadoGrid container as follows:

```bash
docker run -it -e DISPLAY=<macOS host IP>:0 -v /tmp/.X11-unix:/tmp/.X11-unix padogrid/padogrid bash
```

8. Install Hazelcast Desktop by following the instructions in the section, [Installing Hazelcast Desktop](#installing-hazelcast-desktop).