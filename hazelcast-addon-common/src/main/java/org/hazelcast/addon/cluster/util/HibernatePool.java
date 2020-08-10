package org.hazelcast.addon.cluster.util;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

/**
 * HibernatePool is a Hibernate session pool for a general purpose use. The
 * session pool size is configurable by setting the Hibernate property,
 * "connection.pool_size". The default pool size is 1.
 * <p>
 * HibernatePool enforces the following honor system.
 * <ul>
 * <li>Invoke takeSession() to get an available session.</li>
 * <li>Upon completion, return the session by invoking offerSession().</li>
 * <li>Once offerSession() is invoked, the caller must not use the session
 * again. The returned session maybe made available to another caller and
 * therefore the caller may experience conflicts if the returned session is
 * reused. If the caller requires a session, then it must invoke takeSession()
 * again to get the next available session.</li>
 * <li>The caller must not close the session, i.e., do not invoke
 * session.close().</li>
 * </ul>
 * <p>
 * <b>Example:</b> Note that takeSession() and offerSession() are always paired.
 * When you are done with the session make sure to invoke offerSession() to
 * return it to the pool for others to use.
 * 
 * <pre>
 * Session session = HibernatePool.getHibernatePool().takeSession();
 * CriteriaBuilder cb = session.getCriteriaBuilder();
 * CriteriaQuery<Customer> cr = cb.createQuery(Customer.class);
 * Root<Customer> root = cr.from(Customer.class);
 * cr.select(root);
 * Query<Customer> query = session.createQuery(cr);
 * List<Customer> customers = query.getResultList();
 * HibernatePool.getHibernatePool().offerSession(session);
 * customers.forEach(c -> System.out.println(c));
 * </pre>
 * 
 * @author dpark
 *
 */
public class HibernatePool {
	private static final String PROPERTY_HIBERNATE_CONFIG_FILE = "hazelcast-addon.hibernate.config";
	private static StandardServiceRegistry registry;
	private static SessionFactory sessionFactory;
	private static int poolSize = 1;
	private static ILogger logger = Logger.getLogger(HibernatePool.class);

	// Session is not thread-safe. This map holds Session objects create for
	// individual threads
	private static ArrayBlockingQueue<Session> sessionPoolQueue;

	private final static HibernatePool hibernatePool = new HibernatePool();

	private HibernatePool() {
		init();
	}

	public static HibernatePool getHibernatePool() {
		return hibernatePool;
	}

	/**
	 * Returns the session factory that can be used to create additional sessions as
	 * needed. If you create new sessions, then you have an option to offer (add)
	 * them to HibernatePool by invoking the {@linkplain #offerSession(Session)}.
	 * The offered sessions immediately become available to other callers.
	 */
	public SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			try {
				// Create registry
				String filePath = System.getProperty(PROPERTY_HIBERNATE_CONFIG_FILE);
				if (filePath != null) {
					File file = new File(filePath);
					registry = new StandardServiceRegistryBuilder().configure(file).build();
				} else {
					registry = new StandardServiceRegistryBuilder().configure().build();
				}
				// Create MetadataSources
				MetadataSources sources = new MetadataSources(registry);

				// Create Metadata
				Metadata metadata = sources.getMetadataBuilder().build();

				// Create SessionFactory
				sessionFactory = metadata.getSessionFactoryBuilder().build();
				poolSize = Integer
						.valueOf(sessionFactory.getProperties().getOrDefault("connection.pool_size", "1").toString());
			} catch (Exception e) {
				
				// Flush the error. The logger has no fluch capability.
				System.err.println("Hibernate initialization error.");
				e.printStackTrace(System.err);
				System.err.flush();
				
				logger.severe("Hibernate initialization error.", e);
				if (registry != null) {
					StandardServiceRegistryBuilder.destroy(registry);
				}
			}
		}
		return sessionFactory;
	}

	private synchronized void init() {
		if (sessionPoolQueue != null) {
			return;
		}
		SessionFactory sessionFactory = getSessionFactory();
		sessionPoolQueue = new ArrayBlockingQueue<Session>(poolSize);
		for (int i = 0; i < poolSize; i++) {
			Session session = sessionFactory.openSession();
			sessionPoolQueue.add(session);
		}
		String connectionUrl = (String) sessionFactory.getProperties().get("connection.url");
		logger.info("Hibernate successfully initialized. [connection.url=" + connectionUrl + ", connection.pool_size="
				+ poolSize + "]");
	}

	/**
	 * Takes the next available Hibernate session. This call blocks if there are no
	 * sessions available.
	 * 
	 * @throws InterruptedException
	 */
	public Session takeSession() throws InterruptedException {
		Session session = sessionPoolQueue.take();
		return session;
	}

	/**
	 * Offers (or returns) the specified session to the pool. Always invoke this
	 * method when you are done with the session taken by invoking
	 * {@linkplain #takeSession()}. It clears the session in order to prevent 
	 * multiple sessions having the same values. If more than one session have
	 * the same value then Hibernate throws an exception any may not work properly
	 * thereafter. By clearing the session, we prevent the exception and also clear
	 * the Hibernate cache.
	 * 
	 * @param session Hibernate session
	 */
	public void offerSession(Session session) {
		if (session == null) {
			return;
		}
		session.clear();
		sessionPoolQueue.offer(session);
	}

	/**
	 * Shuts down Hibernate by destroying the {@link StandardServiceRegistry}
	 * created by HibernatePool.
	 */
	public void shutdown() {
		if (registry != null) {
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}
}