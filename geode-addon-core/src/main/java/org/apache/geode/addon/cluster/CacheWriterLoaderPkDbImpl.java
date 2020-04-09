package org.apache.geode.addon.cluster;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.geode.addon.cluster.util.HibernatePool;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.CacheWriter;
import org.apache.geode.cache.CacheWriterException;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.RegionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * CacheWriterLoaderPkDbImpl is a generic primary-key mapped CacheWrtier/Loader implementation for
 * integrating Geode with a relational database. It works only with a single
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
 * <td>region.path</td>
 * <td>Optional region path. Used for logging only.</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>entity.class</td>
 * <td>Entity class with Hibernate annotations</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>entity.load.limit</td>
 * <td>Initial load data size limit. The specified number of rows are loaded
 * into Geode. If <= 0 then all rows are loaded.</td>
 * <td>-1</td>
 * </tr>
 * <td>entity.load.batch.size</td>
 * <td>Batch size of primary keys per query. Instead of executing a single query
 * to get all rows during the initial load time, by setting this property,
 * CacheWrtierLoaderDbImpl executes multiple queries that return less number of rows to
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
 * <li>CacheWrtierLoaderDbImpl can be connected to only a single database system due to
 * its reliance of {@link HibernatePool} which can connect to only one database
 * system.</li>
 * </ul>
 * 
 * <b>Example:</b> The following shows a configuration example set in the
 * <b>cache.xml</b> file.
 * 
 * <pre>
 * &lt;region-attributes id="customerStore"&gt;
 *    &lt;cache-loader&gt;
 *       &lt;class-name&gt;org.apache.geode.addon.cluster.CacheWriterLoaderPkDbImpl&lt;/class-name&gt;
 *       &lt;parameter name="region.path"&gt;&lt;string&gt;nw/customers&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.class"&gt;&lt;string&gt;org.apache.geode.demo.nw.data.Customer&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.load.limit"&gt;&lt;string&gt;1000&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.load.batch.size"&gt;&lt;string&gt;100&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.db.isRead"&gt;&lt;string&gt;true&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.db.isWrite"&gt;&lt;string&gt;true&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.db.isDelete"&gt;&lt;string&gt;false&lt;/string&gt;&lt;/parameter&gt;
 *    &lt;/cache-loader&gt;
 *    &lt;cache-writer&gt;
 *       &lt;class-name&gt;org.apache.geode.addon.cluster.CacheWriterLoaderPkDbImpl&lt;/class-name&gt;
 *       &lt;parameter name="region.path"&gt;&lt;string&gt;nw/customers&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.class"&gt;&lt;string&gt;org.apache.geode.demo.nw.data.Customer&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.load.limit"&gt;&lt;string&gt;1000&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.load.batch.size"&gt;&lt;string&gt;100&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.db.isRead"&gt;&lt;string&gt;true&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.db.isWrite"&gt;&lt;string&gt;true&lt;/string&gt;&lt;/parameter&gt;
 *       &lt;parameter name="entity.db.isDelete"&gt;&lt;string&gt;false&lt;/string&gt;&lt;/parameter&gt;
 *    &lt;/cache-writer&gt;
 * &lt;/region-attributes&gt;
 * &lt;region name="nw"&gt;
 *    &lt;region name="customers" refid="PARTITION"&gt;
 *       &lt;region-attributes refid="customerStore" /&gt;
 *    &lt;/region&gt;
 * &lt;/region&gt;
 * </pre>
 * 
 * In addition to cache.xml, the Customer entity class must be registered in
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
 *       &lt;mapping class="org.apache.geode.demo.nw.data.Customer" /&gt;
 *       &lt;mapping class="org.apache.geode.demo.nw.data.Order" /&gt;
 *    &lt;/session-factory&gt;
 * &lt;/hibernate-configuration&gt;
 * </pre>
 * 
 * @author dpark
 *
 * @param <K> Key
 * @param <V> Value or entity class
 */
public class CacheWriterLoaderPkDbImpl<K, V> implements CacheWriter<K, V>, CacheLoader<K, V>, Declarable {

	private final String PROPERTY_REGION_PATH = "region.path";
	private final String PROPERTY_ENTITY_CLASS = "entity.class";
	private final String PROPERTY_LIMIT = "entity.load.limit";
	private final String PROPERTY_LOAD_BATCH_SIZE = "entity.load.batch.size";
	private final String PROPERTY_IS_READ = "entity.db.isRead";
	private final String PROPERTY_IS_WRITE = "entity.db.isWrite";
	private final String PROPERTY_IS_DELETE = "entity.db.isDelete";

	private Cache cache;
	private String regionPath;
	private Class<?> entityClass;
	private int initialKeyLimit = 1000;
	private int loadBatchSize = 1000;
	private boolean isDbRead = true;
	private boolean isDbWrite = false;
	private boolean isDbDelete = false;

	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	public void initialize(Cache cache, Properties properties) {
		this.cache = cache;
		String className = properties.getProperty(PROPERTY_ENTITY_CLASS);
		if (className == null) {
			throw new RuntimeException("entity.class undefined.");
		}
		try {
			entityClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(PROPERTY_ENTITY_CLASS + "=" + className, e);
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

		this.regionPath = properties.getProperty(PROPERTY_REGION_PATH);

		logger.info("Configured CacheWriter/Loader " + this.getClass().getName() + " for the region " + this.regionPath);
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
			logger.fatal(this.getClass().getSimpleName() + ".getPrimaryKeys() DB session interrupted", ex);
		} catch (Exception ex) {
			logger.fatal(ex);
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
	private Map<K, V> loadAll(Collection<K> keys) {
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
				logger.fatal(this.getClass().getSimpleName() + ".loadAll() DB session interrupted", ex);
			} catch (NoSuchMethodException ex) {
				logger.fatal(this.getClass().getSimpleName()
						+ ".loadAll() The getter method for primary key not found in the entity class: "
						+ entityClass.getCanonicalName() + "." + getterMethodName + "()", ex);
			} catch (SecurityException ex) {
				logger.fatal(this.getClass().getSimpleName() + ".loadAll() SecurityException", ex);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				logger.fatal(this.getClass().getSimpleName()
						+ ".loadAll() - Getter method invokation exception for the entity class: "
						+ entityClass.getCanonicalName() + "." + getterMethodName + "()", ex);
			} catch (Exception ex) {
				logger.fatal(ex);
			}
			if (session != null) {
				HibernatePool.getHibernatePool().offerSession(session);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private Iterable<K> loadAllKeys() {
		List<String> pkList = getPrimaryKeys(entityClass, initialKeyLimit);
		return (Iterable<K>) pkList;
	}

	@Override
	public void beforeUpdate(EntryEvent<K, V> event) throws CacheWriterException {
		if (isDbWrite) {
			try {
				Session session = HibernatePool.getHibernatePool().takeSession();
				Transaction transaction = session.beginTransaction();
				session.saveOrUpdate(event.getNewValue());
				transaction.commit();
				HibernatePool.getHibernatePool().offerSession(session);
			} catch (InterruptedException ex) {
				logger.fatal(this.getClass().getSimpleName() + ".beforeUpdate() DB session interrupted", ex);
			} catch (Exception ex) {
				logger.fatal(ex);
			}
		}
	}

	@Override
	public void beforeCreate(EntryEvent<K, V> event) throws CacheWriterException {
		beforeUpdate(event);
	}

	@Override
	public void beforeDestroy(EntryEvent<K, V> event) throws CacheWriterException {
		if (isDbDelete) {
			try {
				Session session = HibernatePool.getHibernatePool().takeSession();
				Transaction transaction = session.beginTransaction();
				session.delete(event.getKey());
				transaction.commit();
				HibernatePool.getHibernatePool().offerSession(session);
			} catch (InterruptedException ex) {
				logger.fatal(this.getClass().getSimpleName() + ".beforeDestroy() DB session interrupted", ex);
			} catch (Exception ex) {
				logger.fatal(ex);
			}
		}
	}

	@Override
	public void beforeRegionDestroy(RegionEvent<K, V> event) throws CacheWriterException {
		// Ignore
	}

	@Override
	public void beforeRegionClear(RegionEvent<K, V> event) throws CacheWriterException {
		if (isDbDelete) {
			try {
				Set<K> set = event.getRegion().keySet();
				Session session = HibernatePool.getHibernatePool().takeSession();
				Transaction transaction = session.beginTransaction();
				set.forEach((key) -> session.delete(key));
				transaction.commit();
				HibernatePool.getHibernatePool().offerSession(session);
			} catch (InterruptedException ex) {
				logger.fatal(this.getClass().getSimpleName() + ".beforeRegionClear() DB session interrupted", ex);
			} catch (Exception ex) {
				logger.fatal(ex);
			}
		}
	}

	@Override
	public V load(LoaderHelper<K, V> helper) throws CacheLoaderException {
		if (isDbRead) {
			try {
				Session session = HibernatePool.getHibernatePool().takeSession();
				V value = (V) session.find(entityClass, helper.getKey());
				HibernatePool.getHibernatePool().offerSession(session);
				return value;
			} catch (InterruptedException ex) {
				logger.fatal(this.getClass().getSimpleName() + ".load() DB interrupted", ex);
			} catch (Exception ex) {
				logger.fatal(ex);
			}
		}
		return null;
	}
}
