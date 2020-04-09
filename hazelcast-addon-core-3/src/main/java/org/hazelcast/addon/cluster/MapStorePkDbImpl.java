package org.hazelcast.addon.cluster;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hazelcast.addon.cluster.util.HibernatePool;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;
import com.hazelcast.logging.ILogger;

/**
 * MapStorePkDbImpl is a generic primary-key mapped MapStore implementation for
 * integrating Hazelcast with a relational database. It works only with a single
 * primary key. In other words, the entity object must include the primary key
 * field and the table must contain the primary key column.
 * <p>
 * The following properties are supported.
 * <table border="1">
 * <tr>
 * <td>Property</td>
 * <td>Description</td>
 * <td>Default</td>
 * </tr>
 * <tr>
 * <td>entity.class</td>
 * <td>Entity class with Hibernate annotations</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>entity.load.limit</td>
 * <td>Initial load data size limit. The specified number of rows are loaded
 * into Hazelcast. If <= 0 then all rows are loaded.</td>
 * <td>-1</td>
 * </tr>
 * <td>entity.load.batch.size</td>
 * <td>Batch size of primary keys per query. Instead of executing a single query
 * to get all rows during the initial load time, by setting this property,
 * MapStoreDbImpl executes multiple queries that return less number of rows to
 * prevent the database connection getting timed out.</td>
 * <td>1000</td>
 * <tr>
 * <td>entity.db.isRead</td>
 * <td>true to read from the database if there is a cache miss, false to ignore
 * cache misses</td>
 * <td>true</td>
 * </tr>
 * <tr>
 * <td>entity.db.isWrite</td>
 * <td>true to write the database</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>entity.db.isDelete</td>
 * <td>true to delete from the database</td>
 * <td>false</td>
 * </tr>
 * </table>
 * <p>
 * <b>Limitations:</b>
 * <ul>
 * <li>MapStoreDbImpl can be connected to only a single database system due to
 * its reliance of {@link HibernatePool} which can connect to only one database
 * system.</li>
 * </ul>
 * 
 * <b>Example:</b> The following shows a configuration example set in the
 * <b>hazecast.xml</b> file.
 * 
 * <pre>
 * &lt;map name="nw/customers"&gt;
 *    &lt;map-store enabled="true" initial-mode="EAGER"&gt;
 *       &lt;class-name&gt;org.hazelcast.addon.cluster.MapStorePkDbImpl&lt;/class-name&gt;
 *       &lt;properties&gt;
 *           &lt;property name="entity.class"&gt;org.hazelcast.demo.nw.data.Order&lt;/property&gt;
 *           &lt;property name="entity.load.limit"&gt;1000&lt;/property&gt;
 *           &lt;property name="entity.load.batch.size"&gt;100&lt;/property&gt;
 *           &lt;property name="entity.db.isRead"&gt;true&lt;/property&gt;
 *           &lt;property name="entity.db.isWrite"&gt;false&lt;/property&gt;
 *           &lt;property name="entity.db.isDelete"&gt;false&lt;/property&gt;
 *       &lt;/properties&gt;
 *       &lt;write-batch-size&gt;100&lt;/write-batch-size&gt;
 *       &lt;write-coalescing&gt;true&lt;/write-coalescing&gt;
 *       &lt;write-delay-seconds&gt;1&lt;/write-delay-seconds&gt;
 *   &lt;/map-store&gt;
 * &lt;map&gt;
 * </pre>
 * 
 * In addition to hazelcast.xml, the Customer entity class must be registered in
 * the <b>hibernate.cfg.xml</b> file. See the class attributes in the example below.
 * 
 * <pre>
 * &lt;hibernate-configuration&gt;
 *    &lt;session-factory&gt;
 *       &lt;!-- JDBC Database connection settings --&gt;
 *       &lt;property name="connection.driver_class"&gt;com.mysql.cj.jdbc.Driver&lt;/property&gt;
 *       &lt;property name="connection.url"&gt;jdbc:mysql://localhost:3306/nw?useSSL=false&lt;/property&gt;
 *       &lt;property name="connection.username"&gt;root&lt;/property&gt;
 *       &lt;property name="connection.password"&gt;password&lt;/property&gt;
 *       &lt;!-- JDBC connection pool settings ... using built-in test pool --&gt;
 *       &lt;property name="connection.pool_size"&gt;10&lt;/property&gt;
 *       &lt;!-- Select our SQL dialect --&gt;
 *       &lt;property name="dialect"&gt;org.hibernate.dialect.MySQL5Dialect&lt;/property&gt;
 *       &lt;!-- Echo the SQL to stdout --&gt;
 *       &lt;property name="show_sql"&gt;true&lt;/property&gt;
 *       &lt;!-- Set the current session context --&gt;
 *       &lt;property name="current_session_context_class"&gt;thread&lt;/property&gt;
 *       &lt;!-- Update the database schema on startup --&gt;
 *       &lt;property name="hbm2ddl.auto"&gt;update&lt;/property&gt;
 *       &lt;!-- dbcp connection pool configuration --&gt;
 *       &lt;property name="hibernate.dbcp.initialSize"&gt;5&lt;/property&gt;
 *       &lt;property name="hibernate.dbcp.maxTotal"&gt;20&lt;/property&gt;
 *       &lt;property name="hibernate.dbcp.maxIdle"&gt;10&lt;/property&gt;
 *       &lt;property name="hibernate.dbcp.minIdle"&gt;5&lt;/property&gt;
 *       &lt;property name="hibernate.dbcp.maxWaitMillis"&gt;-1&lt;/property&gt;
 *       &lt;property name="hibernate.connection.serverTimezone"&gt;UTC&lt;/property&gt;
 *       &lt;mapping class="org.hazelcast.demo.nw.data.Customer" /&gt;
 *       &lt;mapping class="org.hazelcast.demo.nw.data.Order" /&gt;
 *    &lt;/session-factory&gt;
 * &lt;/hibernate-configuration&gt;
 * </pre>
 * 
 * @author dpark
 *
 * @param <K> Key
 * @param <V> Value or entity class
 */
public class MapStorePkDbImpl<K, V> implements MapStore<K, V>, MapLoaderLifecycleSupport {

	private final String PROPERTY_ENTITY_CLASS = "entity.class";
	private final String PROPERTY_LIMIT = "entity.load.limit";
	private final String PROPERTY_LOAD_BATCH_SIZE = "entity.load.batch.size";
	private final String PROPERTY_IS_READ = "entity.db.isRead";
	private final String PROPERTY_IS_WRITE = "entity.db.isWrite";
	private final String PROPERTY_IS_DELETE = "entity.db.isDelete";

	private HazelcastInstance hzInstance;
	private String mapName;
	private Class<?> entityClass;
	private int initialKeyLimit = 1000;
	private int loadBatchSize = 1000;
	private boolean isDbRead = true;
	private boolean isDbWrite = false;
	private boolean isDbDelete = false;

	private ILogger logger;

	@Override
	public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
		String className = properties.getProperty(PROPERTY_ENTITY_CLASS);
		if (className == null) {
			throw new HazelcastException("entity.class undefined.");
		}
		try {
			entityClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new HazelcastException(PROPERTY_ENTITY_CLASS + "=" + className, e);
		}
		try {
			initialKeyLimit = Integer.valueOf(properties.getProperty(PROPERTY_LIMIT, "1000"));
		} catch (NumberFormatException ex) {
			initialKeyLimit = 1000;
		}
		try {
			loadBatchSize = Integer.valueOf(properties.getProperty(PROPERTY_LOAD_BATCH_SIZE, "1000"));
		} catch (NumberFormatException ex) {
			loadBatchSize = 1000;
		}
		isDbRead = Boolean.valueOf(properties.getProperty(PROPERTY_IS_READ, "true"));
		isDbWrite = Boolean.valueOf(properties.getProperty(PROPERTY_IS_WRITE, "false"));
		isDbDelete = Boolean.valueOf(properties.getProperty(PROPERTY_IS_DELETE, "false"));

		this.hzInstance = hazelcastInstance;
		this.mapName = mapName;
		logger = hzInstance.getLoggingService().getLogger(this.getClass());

		logger.info("Configured MapStore " + this.getClass().getName() + " for the map " + this.mapName);
	}

	@Override
	public void destroy() {
		logger.info("Shutting down Hibernate [" + this.getClass().getName() + "]");
		HibernatePool.getHibernatePool().shutdown();
	}

	@SuppressWarnings("unchecked")
	@Override
	public V load(K key) {
		if (isDbRead) {
			try {
				Session session = HibernatePool.getHibernatePool().takeSession();
				V value = (V) session.find(entityClass, key);
				HibernatePool.getHibernatePool().offerSession(session);
				return value;
			} catch (InterruptedException ex) {
				logger.severe(this.getClass().getSimpleName() + ".load() DB interrupted", ex);
			} catch (Exception ex) {
				logger.severe(ex);
			}
		}
		return null;
	}

	private List<String> getPrimaryKeys(Class<?> clazz, int limit) {
		try {
			Session session = HibernatePool.getHibernatePool().takeSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<String> cr = cb.createQuery(String.class);
			Root<?> root = cr.from(clazz);
			String pk = root.getModel().getId(String.class).getName();
			cr.select(root.get(pk));
			Query<String> query = session.createQuery(cr);
			if (limit > 0) {
				query.setMaxResults(limit);
			}
			List<String> pkList = query.getResultList();
			HibernatePool.getHibernatePool().offerSession(session);
			return pkList;
		} catch (InterruptedException ex) {
			logger.severe(this.getClass().getSimpleName() + ".getPrimaryKeys() DB session interrupted", ex);
		} catch (Exception ex) {
			logger.severe(ex);
		}
		return new ArrayList<String>(0);
	}

	private String getGetter(String fieldName) {
		char c = fieldName.charAt(0);
		if (Character.isAlphabetic(c)) {
			fieldName = Character.toUpperCase(c) + fieldName.substring(1);
		}
		return "get" + fieldName;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<K, V> loadAll(Collection<K> keys) {
		Map<K, V> result = new HashMap<K, V>();
		String getterMethodName = null;
		if (isDbRead) {
			Session session = null;
			try {
				session = HibernatePool.getHibernatePool().takeSession();
				CriteriaBuilder cb = session.getCriteriaBuilder();
				CriteriaQuery<?> cr = cb.createQuery(entityClass);
				Root root = cr.from(entityClass);
				String pk = root.getModel().getId(String.class).getName();
				getterMethodName = getGetter(pk);
				Method method = entityClass.getMethod(getterMethodName);

				// Query the DB with a batch of primary keys at a time to
				// reduce the client query time
				Iterator iterator = keys.iterator();
				int size = keys.size();
				int i = 1;
				while (i <= size) {
					In<String> inClause = cb.in(root.get(pk));
					while (iterator.hasNext() && i % loadBatchSize > 0) {
						Object key = iterator.next();
						inClause.value(key.toString());
						cr.select(root).where(inClause);
						i++;
					}
					if (iterator.hasNext()) {
						Object key = iterator.next();
						inClause.value(key.toString());
						cr.select(root).where(inClause);
						i++;
					}
					Query<?> query = session.createQuery(cr);
					List<?> valueList = query.getResultList();
					for (Object value : valueList) {
						K key = (K) method.invoke(value);
						result.put(key, (V) value);
					}
				}
			} catch (InterruptedException ex) {
				logger.severe(this.getClass().getSimpleName() + ".loadAll() DB session interrupted", ex);
			} catch (NoSuchMethodException ex) {
				logger.severe(this.getClass().getSimpleName()
						+ ".loadAll() The getter method for primary key not found in the entity class: "
						+ entityClass.getCanonicalName() + "." + getterMethodName + "()", ex);
			} catch (SecurityException ex) {
				logger.severe(this.getClass().getSimpleName() + ".loadAll() SecurityException", ex);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				logger.severe(this.getClass().getSimpleName()
						+ ".loadAll() - Getter method invokation exception for the entity class: "
						+ entityClass.getCanonicalName() + "." + getterMethodName + "()", ex);
			} catch (Exception ex) {
				logger.severe(ex);
			}
			if (session != null) {
				HibernatePool.getHibernatePool().offerSession(session);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<K> loadAllKeys() {
		List<String> pkList = getPrimaryKeys(entityClass, initialKeyLimit);
		return (Iterable<K>) pkList;
	}

	@Override
	public void store(K key, V value) {
		if (isDbWrite) {
			try {
				Session session = HibernatePool.getHibernatePool().takeSession();
				Transaction transaction = session.beginTransaction();
				session.saveOrUpdate(value);
				transaction.commit();
				HibernatePool.getHibernatePool().offerSession(session);
			} catch (InterruptedException ex) {
				logger.severe(this.getClass().getSimpleName() + ".store() DB session interrupted", ex);
			} catch (Exception ex) {
				logger.severe(ex);
			}
		}
	}

	@Override
	public void storeAll(Map<K, V> map) {
		if (isDbWrite) {
			try {
				Session session = HibernatePool.getHibernatePool().takeSession();
				Transaction transaction = session.beginTransaction();
				map.forEach((id, value) -> session.saveOrUpdate(value));
				transaction.commit();
				HibernatePool.getHibernatePool().offerSession(session);
			} catch (InterruptedException ex) {
				logger.severe(this.getClass().getSimpleName() + ".storeAll() DB session interrupted", ex);
			} catch (Exception ex) {
				logger.severe(ex);
			}
		}
	}

	@Override
	public void delete(K key) {
		if (isDbDelete) {
			try {
				Session session = HibernatePool.getHibernatePool().takeSession();
				Transaction transaction = session.beginTransaction();
				session.delete(key);
				transaction.commit();
				HibernatePool.getHibernatePool().offerSession(session);
			} catch (InterruptedException ex) {
				logger.severe(this.getClass().getSimpleName() + ".delete() DB session interrupted", ex);
			} catch (Exception ex) {
				logger.severe(ex);
			}
		}
	}

	@Override
	public void deleteAll(Collection<K> keys) {
		if (isDbDelete) {
			try {
				Session session = HibernatePool.getHibernatePool().takeSession();
				Transaction transaction = session.beginTransaction();
				keys.forEach((key) -> session.delete(key));
				transaction.commit();
				HibernatePool.getHibernatePool().offerSession(session);
			} catch (InterruptedException ex) {
				logger.severe(this.getClass().getSimpleName() + ".deleteAll() DB session interrupted", ex);
			} catch (Exception ex) {
				logger.severe(ex);
			}
		}
	}
}
