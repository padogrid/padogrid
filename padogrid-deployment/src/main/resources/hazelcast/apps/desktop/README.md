# Hazelcast Desktop App

The Desktop app is an external app that contains the `build_app` installation script for building `hazlecast-desktop` in the `padogrid` environment. 


## `hazelcast-desktop` Repo

The `hazecast-desktop` app sits on top of [Netcrest Pado](https://github.com/netcrest/pado), which includes a run-time screen layout manager for dynamically laying out the screen with Swing components. This app is maintained by Netcrest in the following GitHub repo:

Repo: [https://github.com/netcrest/hazelcast-desktop](https://github.com/netcrest/hazelcast-desktop)

## Installing and Running `hazlecast-desktop`

```console
# 1. Create desktop app
create_app -app desktop

# 2. Build hazelcast-desktop
cd_app desktop
cd bin_sh
./build_app

# 3. Copy any client jar files that contain domain classes, etc. into the plugins directory.
cd ../hazelcast-desktop-<version>
cp <your-domain-class-jars> plugins/

# 4. Configure hazelcast-client.xml to include your domain class
#    serialization information.
vi etc/hazelcast-client.xml

# 5. Run desktop
cd bin_sh/
./desktop
```

## Quick Start with perf_test

You can use the `perf_test` app to ingest mock data into a Hazelcast cluster and test the desktop app as shown below.

```console
# 1. Create desktop app
create_app -app desktop

# 2. Build hazelcast-desktop
cd_app desktop
cd bin_sh
./build_app

# 3. Copy domain class serialization configuration from the desktop hazecast-client.xml
cd ../hazelcast-desktop-<version>/etc
cat hazelcast-client.xml

            <portable-factory factory-id="1">
                org.hazelcast.demo.nw.data.PortableFactoryImpl
            </portable-factory>

# 4. Copy and paste the above output in hazelcast.xml
cd_cluster
vi etc/hazelcast.xml

# 5. Run cluster
start_cluster

# 6. Ingest data
create_app
cd_app perf_test
cd bin_sh/
./test_group -prop ../etc/group-factory.properties -run

# 7. Run desktop
cd_app desktop
cd bin_sh/
./desktop

```

## Configuring Hazelcast Client

The `hazelcast-client.xml` file for the desktop is located in the `etc/` directory as follows:

```console
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
