# Hazelcast Addon Library

This package contains an open source Hazelcast addon API that supplements the Hazelcast API.

## Compiling and installing hazelcast-addon

```
mvn install
```


## Deploying hazelcast-addon

Place the `hazelcast-addon_\<version\>.jar` file in both client and server class paths. Upon successful build, the jar file can be found in the target directory as follows:

```
target/hazelcast-addon-<version>.jar
```

## Configuring hazelcast-addon
Add the PortableFactory class in hazelcast.xml and hazelcast-client.xml as follows:

```xml
<serialization>
    <portable-factories>
        <portable-factory factory-id="10000">
            org.hazelcast.addon.hql.impl.PortableFactoryImpl
        </portable-factory>
    </portable-factories>
</serialization>
```

By default, hazelcast-addon sets the factory ID to 10000 and the class ID begins from 10000. The class IDs increment by 1 as more of them become available. If there are ID conflicts then you can change them using their system properies as follows:

```java
-Dorg.hazelcast.addon.hql.impl.PortableFactoryImpl.factoryId=10000
-Dorg.hazelcast.addon.hql.impl.PortableFactoryImpl.firstClassId=10000
```

## Addon Features

### HqlQuery

HqlQuery wraps Hazelcast's `SqlPredicate` and `PagingPredicate` to provide support for ad-hoc queries and result set pagination. 

**HQL Syntax**

```sql
select * <from-clause> [<where-clause>] [<order-by-clause>] [;]

    <from-clause>: from <map-name>[.keys|.values|.entries] [<alias>] 
   <where-clause>: where [<alias>-name>][.value.]<field-name>...
<order-by-clause>: order by [<alias>[.key.|.value.]]<field-name> [asc|desc]
```

**HQL Examples:**

```sql
-- Query values (if keys or entries are not specified in the map name then
-- it defaults to values):
select *
from nw/orders
where customerId='ANATR' or freigt>10 and freight<100
order by customerId asc, orderId desc;
 
-- Query entries (keys and values) alias:
select * 
from nw/orders.entries e
where e.value.customerId='ANATR' or e.value.freight>10 and e.value.freight<100 
order by e.value.customerId asc, e.value.orderId desc;
 
-- Query keys (returns keys only):
--    Note that at the time of writing, Hazelcast does not support query
--    executions on composite keys (Hazelcast version 3.12)
select *
from nw/orders.keys k
where k>1000;
 
-- Query keys sorted by objects themselves in ascending order.
-- The key objects must implement Comparable.
select *
from nw/orders.keys k order by k desc;
 
-- Query values sorted by objects themselves in descending order. 
-- The value objects must implement Comparable.
select *
from nw/orders v order by v desc;
```

**HqlQuery API Example:**

```java 
import org.hazelcast.addon.hql.HqlQuery;
import org.hazelcast.addon.hql.IPageResults;
...

HazelcastInstance hz = Hazelcast.newHazelcastInstance();
HqlQuery<Map.Entry<String, Order>> hql = HqlQuery.newHqlQueryInstance(hz);
String query = "select * "
			+ "from nw/orders.entries e "
			+ "order by e.value.shipCountry asc, e.value.customerId desc, e.key desc";
IPageResults<Map.Entry<String, Order>> results = hql.execute(query, 100);
Collection<Map.Entry<String, Order>> col;
int i = 0;
do {
	col = results.getResults();
	System.out.println("Page " + results.getPage());
	System.out.println("-------");
	for (Map.Entry<String, Order> entry : col) {
		String key = entry.getKey();
		Order order = entry.getValue();
		System.out.println(i++ + ". " + order.getShipCountry() + ", " + order.getCustomerId() + " key=" + key);
	}
} while (results.nextPage());
```