package org.hazelcast.addon.test.perf;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hazelcast.addon.cluster.util.HibernatePool;
import org.hazelcast.addon.test.perf.data.Blob;
import org.hazelcast.addon.test.perf.data.DataObjectFactory;
import org.hazelcast.addon.test.perf.query.IPredicate;
import org.hazelcast.addon.test.perf.query.ISql;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.hazelcast.cache.ICache;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import com.hazelcast.replicatedmap.ReplicatedMap;
import com.hazelcast.topic.ITopic;

/**
 * GroupTest is a test tool for capturing the throughput and average latency of
 * multiple Hazelcast data structure operations performed as a single atomic
 * operation. This is achieved by allowing you to group one or more operations
 * and invoke them as a single call.
 * <p>
 * <table>
 * <thead>
 * <tr>
 * <th>Property</th>
 * <th>Description</th>
 * <th>Default</th>
 * </tr>
 * </thead>
 * <tr>
 * <td>totalEntryCount</td>
 * <td>The total number of entries per test case.</td>
 * <td>{@linkplain #DEFAULT_totalEntryCount}</td>
 * </tr>
 * <tr>
 * <td>batchSize</td>
 * <td>The number of objects per getAll() call per thread. For other operations
 * (test cases), this property is ignored.</td>
 * <td>{@linkplain #DEFAULT_batchSize}</td>
 * </tr>
 * <tr>
 * <td>threadCount</td>
 * <td>The number of threads to concurrently execute getAll().</td>
 * <td>{@linkplain #DEFAULT_threadCount}</td>
 * </tr>
 * <tr>
 * <td>Data Structures</td>
 * <td>map | rmap | cache | topic | rtopic | queue</td>
 * <td>{@linkplain #DEFAULT_testCase}</td>
 * </tr>
 * <tr>
 * <td>testCase</td>
 * <td>set | put | putall | get | getall | publish | publishall | offer | poll |
 * peek | take</td>
 * <td>{@linkplain #DEFAULT_testCase}</td>
 * </tr>
 * </table>
 * 
 * @author dpark
 *
 */
public class GroupTest implements Constants 
{
	private final static String PRODUCT="hazelcast";

	private static int TEST_COUNT;
	private static int TEST_INTERVAL_IN_MSEC;
	private static int PRINT_STATUS_INTERVAL_IN_SEC;
	private static boolean IS_FAILOVER_CLIENT = false;
	private static List<Group[]> concurrentGroupList = new ArrayList<Group[]>(4);
	private static HashMap<String, Operation> operationMap = new HashMap<String, Operation>();

	private HazelcastInstance hazelcastInstance;

	enum DataStructureEnum {
		map, rmap, cache, topic, rtopic, queue, sleep
	}

	enum TestCaseEnum {
		set, put, putall, get, getall, asyncput, asyncget, predicate, sql, publish, publishall, offer, poll, peek, take;

		static TestCaseEnum getTestCase(String testCaseName) {
			if (set.name().equalsIgnoreCase(testCaseName)) {
				return set;
			} else if (put.name().equalsIgnoreCase(testCaseName)) {
				return put;
			} else if (putall.name().equalsIgnoreCase(testCaseName)) {
				return putall;
			} else if (get.name().equalsIgnoreCase(testCaseName)) {
				return get;
			} else if (asyncput.name().equalsIgnoreCase(testCaseName)) {
				return asyncput;
			} else if (asyncget.name().equalsIgnoreCase(testCaseName)) {
				return asyncget;
			} else if (getall.name().equalsIgnoreCase(testCaseName)) {
				return getall;
			} else if (predicate.name().equalsIgnoreCase(testCaseName)) {
				return predicate;
			} else if (sql.name().equalsIgnoreCase(testCaseName)) {
				return sql;
			} else if (publish.name().equalsIgnoreCase(testCaseName)) {
				return publish;
			} else if (publishall.name().equalsIgnoreCase(testCaseName)) {
				return publishall;
			} else if (offer.name().equalsIgnoreCase(testCaseName)) {
				return offer;
			} else if (poll.name().equalsIgnoreCase(testCaseName)) {
				return poll;
			} else if (peek.name().equalsIgnoreCase(testCaseName)) {
				return peek;
			} else if (take.name().equalsIgnoreCase(testCaseName)) {
				return take;
			} else {
				return putall;
			}
		}
	}

	static class Group {
		String name;
		int threadCount;
		int totalInvocationCount;
		Operation[] operations;
		String operationsStr;
		String comment;
		AbstractThread[] threads;
	}

	static class Operation {
		String name;
		String ref;
		String dsName;
		int sleep;
		@SuppressWarnings("rawtypes")
		IMap imap;
		@SuppressWarnings("rawtypes")
		ICache icache;
		@SuppressWarnings("rawtypes")
		ReplicatedMap rmap;
		@SuppressWarnings("rawtypes")
		IQueue iqueue;
		@SuppressWarnings("rawtypes")
		ITopic itopic;
		DataStructureEnum ds;
		TestCaseEnum testCase;
		int totalEntryCount = -1; // for putall and getall
		int payloadSize = -1;
		int batchSize = -1;
		String keyPrefix;
		int startNum = -1;
		DataObjectFactory dataObjectFactory;
		Random random;
		String predicate;
		String sql;
		@SuppressWarnings("rawtypes")
		IPredicate predicateObj;
		ISql sqlObj;
		String sqlArg;

		@Override
		public Object clone() {
			Operation op = new Operation();
			op.name = name;
			op.ref = ref;
			op.dsName = dsName;
			op.sleep = sleep;
			op.imap = imap;
			op.icache = icache;
			op.rmap = rmap;
			op.iqueue = iqueue;
			op.itopic = itopic;
			op.ds = ds;
			op.testCase = testCase;
			op.totalEntryCount = totalEntryCount;
			op.payloadSize = payloadSize;
			op.batchSize = batchSize;
			op.keyPrefix = keyPrefix;
			op.startNum = startNum;
			op.dataObjectFactory = dataObjectFactory;
			op.random = random;
			op.predicate = predicate;
			op.sql = sql;
			op.predicateObj= predicateObj;
			op.sqlObj = sqlObj;
			op.sqlArg = sqlArg;
			return op;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Operation other = (Operation) obj;
			return Objects.equals(name, other.name);
		}
	}

	public GroupTest(boolean runDb) throws Exception {
		init(runDb);
	}

	private void init(boolean runDb) throws Exception {
		if (runDb == false) {
			if (IS_FAILOVER_CLIENT) {
				hazelcastInstance = HazelcastClient.newHazelcastFailoverClient();
			} else {
				hazelcastInstance = HazelcastClient.newHazelcastClient();
			}

			// Get data structures
			for (Operation operation : operationMap.values()) {
				switch (operation.ds) {
				case rmap:
					operation.rmap = hazelcastInstance.getReplicatedMap(operation.dsName);
					break;
				case cache:
					operation.icache = hazelcastInstance.getCacheManager().getCache(operation.dsName);
					break;
				case queue:
					operation.iqueue = hazelcastInstance.getQueue(operation.dsName);
					break;
				case topic:
					operation.itopic = hazelcastInstance.getTopic(operation.dsName);
					break;
				case rtopic:
					operation.itopic = hazelcastInstance.getReliableTopic(operation.dsName);
					break;
				case map:
				default:
					// dsName maybe null if sleep operation
					if (operation.dsName != null) {
						operation.imap = hazelcastInstance.getMap(operation.dsName);
						if (operation.predicateObj != null) {
							operation.predicateObj.init(operation.imap);
						} else if (operation.sqlObj != null) {
							operation.sqlObj.init(operation.sqlArg);
						}
					}
					break;
				}
			}
		}
	}

	private void printTotalInvocations(Group[] groups, int timeElapsedInSec) {
		long totalInvocationCount;
		for (Group group : groups) {
			totalInvocationCount = 0;
			if (group.threads != null) {
				for (AbstractThread thread : group.threads) {
					if (thread != null) {
						totalInvocationCount += thread.operationCount;
					}
				}
				writeLine("[" + timeElapsedInSec + " sec] Invocation Count (" + group.name + "): "
						+ totalInvocationCount);
			}
		}
	}

	private void runTest(String concurrentGroupNames, Group group, boolean runDb) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd-HHmmss");
		String resultsDirStr = System.getProperty("results.dir", "results");
		File resultsDir = new File(resultsDirStr);
		if (resultsDir.exists() == false) {
			resultsDir.mkdirs();
		}
		Date startTime = new Date();
		File file = new File(resultsDir, "group-" + group.name + "-" + PRODUCT + "-" + format.format(startTime) + ".txt");

		writeLine("   " + file.getAbsolutePath());

		int countPerThread = group.totalInvocationCount / group.threadCount;

		PrintWriter writer = new PrintWriter(file);

		String dbHeader = "";
		if (runDb) {
			dbHeader = " (Dababase)";
		}
		writer.println("******************************************");
		writer.println("Group Test" + dbHeader);
		writer.println("******************************************");
		writer.println();
		writer.println("                       Product: " + PRODUCT);
		writer.println("                         Group: " + group.name);
		writer.println("           Concurrent Group(s): " + concurrentGroupNames);
		writer.println("                       Comment: " + group.comment);
		writer.println("                    Operations: " + group.operationsStr);
		writer.println("                Test Run Count: " + TEST_COUNT);
		writer.println("      Test Run Interval (msec): " + TEST_INTERVAL_IN_MSEC);
		writer.println("Total Invocation Count per Run: " + group.totalInvocationCount);
		writer.println("                  Thread Count: " + group.threadCount);
		writer.println("   Invocation Count per Thread: " + countPerThread);
		writer.println();

		int threadStartIndex = 1;
		AbstractThread workerThreads[] = null;

		workerThreads = new AbstractThread[group.threadCount];
		for (int i = 0; i < workerThreads.length; i++) {
			if (runDb) {
				workerThreads[i] = new GroupDbTestThread(i + 1, threadStartIndex, countPerThread, group);
			} else {
				workerThreads[i] = new GroupTestThread(i + 1, threadStartIndex, countPerThread, group);
			}
			threadStartIndex += countPerThread;
		}

		group.threads = workerThreads;

		startTime = new Date();
		writer.println("Start Time: " + startTime);
		writer.flush();

		for (int i = 0; i < workerThreads.length; i++) {
			workerThreads[i].start();
		}

		// Wait till all threads are complete
		int totalInvocationCount = 0;
		for (int i = 0; i < workerThreads.length; i++) {
			try {
				workerThreads[i].join();
				totalInvocationCount += workerThreads[i].operationCount;
			} catch (InterruptedException ignore) {
			}
		}

		Date stopTime = new Date();

		writer.println();
		writer.println("Actual Total Number of Invocations: " + totalInvocationCount);

		// Report results
		long timeElapsedInMsec = stopTime.getTime() - startTime.getTime();
		printReport(writer, workerThreads, totalInvocationCount, timeElapsedInMsec);
		writer.println("Stop Time: " + stopTime);
		writer.println();
		writer.close();
	}

	private void printReport(PrintWriter writer, AbstractThread threads[], int totalCount, long elapsedTimeInMsec) {
		writer.println();
		writer.println("Time unit: msec");

		long maxTimeMsec = Long.MIN_VALUE;
		for (int i = 0; i < threads.length; i++) {
			writer.println("   Thread " + (i + 1) + ": " + (threads[i].totalElapsedTimeInMsec));
			if (maxTimeMsec < threads[i].totalElapsedTimeInMsec) {
				maxTimeMsec = threads[i].totalElapsedTimeInMsec;
			}
		}

		double txPerMsec = (double) totalCount / (double) maxTimeMsec;
		double txPerSec = txPerMsec * 1000;
		double latencyPerEntry = (double) maxTimeMsec / (double) totalCount;
		double eTxPerMSec = (double) totalCount / (double) elapsedTimeInMsec;
		double eTxPerSec = eTxPerMSec * 1000;
		double eLatencyPerEntry = (double) elapsedTimeInMsec / (double) totalCount;
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.HALF_UP);

		writer.println();
		writer.println("                Max Time (msec): " + maxTimeMsec);
		writer.println("            Elapsed Time (msec): " + elapsedTimeInMsec);
		writer.println("         Total Invocation Count: " + totalCount);
		writer.println(" M Throughput (invocations/sec): " + df.format(txPerSec));
		writer.println("M Latency per invocation (msec): " + df.format(latencyPerEntry));
		writer.println(" E Throughput (invocations/sec): " + df.format(eTxPerSec));
		writer.println("E Latency per invocation (msec): " + df.format(eLatencyPerEntry));
		writer.println();
	}

	abstract class AbstractThread extends Thread {
		int threadNum;
		int threadStartIndex;
		int invocationCountPerThread;
		Group group;

		long operationCount = 0;
		long nullCount = 0;
		long elapsedTimeInMsec;
		long totalElapsedTimeInMsec;

		AbstractThread(int threadNum, int threadStartIndex, int invocationCountPerThread, Group group) {
			this.threadNum = threadNum;
			this.threadStartIndex = threadStartIndex;
			this.invocationCountPerThread = invocationCountPerThread;
			this.group = group;
		}

		public synchronized void run() {
			for (int i = 0; i < TEST_COUNT; i++) {
				__run();
				totalElapsedTimeInMsec += elapsedTimeInMsec;
				if (TEST_INTERVAL_IN_MSEC > 0) {
					try {
						wait(TEST_INTERVAL_IN_MSEC);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
		}

		public abstract void __run();
	}

	class GroupTestThread extends AbstractThread {
		public GroupTestThread(int threadNum, int threadStartIndex, int entryCountPerThread, Group group) {
			super(threadNum, threadStartIndex, entryCountPerThread, group);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void __run() {
			int threadStopIndex = threadStartIndex + invocationCountPerThread - 1;
			int keyIndexes[] = new int[group.operations.length];
			for (int i = 0; i < keyIndexes.length; i++) {
				Operation operation = group.operations[i];
				int entryCount = operation.totalEntryCount / group.threadCount;
				keyIndexes[i] = (threadNum - 1) * entryCount;
			}

			long startTime = System.currentTimeMillis();
			try {
				for (int i = threadStartIndex; i <= threadStopIndex; i++) {

					for (int j = 0; j < group.operations.length; j++) {
						Operation operation = group.operations[j];

						switch (operation.ds) {

						case rmap:
							switch (operation.testCase) {

							case put: {
								int idNum = operation.startNum + i - 1;
								if (operation.dataObjectFactory == null) {
									String key = operation.keyPrefix + idNum;
									Blob blob = new Blob(new byte[operation.payloadSize]);
									operation.rmap.put(key, blob);
								} else {
									DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum,
											null);
									operation.rmap.put(entry.key, entry.value);
									// ER objects
									writeEr(operation, entry, i, threadStopIndex, threadStopIndex);
								}
							}
								break;

							case get: {
								int val = operation.random.nextInt(operation.totalEntryCount);
								int idNum = operation.startNum + val;
								Object key;
								Object value;
								if (operation.dataObjectFactory == null) {
									key = operation.keyPrefix + idNum;
									value = operation.rmap.get(key);
								} else {
									key = operation.dataObjectFactory.getKey(idNum);
									value = operation.rmap.get(key);
								}
								if (value == null) {
									writeLine(threadNum + ". [" + group.name + "." + operation.dsName + "."
											+ operation.testCase + "] key=" + key + " value=null");
								}
							}
								break;

							case putall:
							default: {
								HashMap<Object, Object> map = new HashMap<Object, Object>(operation.batchSize, 1f);
								keyIndexes[j] = createPutAllMap(map, operation, keyIndexes[j], threadNum,
										group.threadCount);
								operation.rmap.putAll(map);
							}
								break;
							}
							break;

						case cache:
							switch (operation.testCase) {
							case put: {
								int idNum = operation.startNum + i - 1;
								if (operation.dataObjectFactory == null) {
									String key = operation.keyPrefix + idNum;
									Blob blob = new Blob(new byte[operation.payloadSize]);
									operation.icache.put(key, blob);
								} else {
									DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum,
											null);
									operation.icache.put(entry.key, entry.value);
									// ER objects
									writeEr(operation, entry, i, threadStopIndex, threadStopIndex);
								}
							}
								break;

							case get: {
								int val = operation.random.nextInt(operation.totalEntryCount);
								int idNum = operation.startNum + val;
								Object key;
								Object value;
								if (operation.dataObjectFactory == null) {
									key = operation.keyPrefix + idNum;
									value = operation.icache.get(key);
								} else {
									key = operation.dataObjectFactory.getKey(idNum);
									value = operation.icache.get(key);
								}
								if (value == null) {
									writeLine(threadNum + ". [" + group.name + "." + operation.dsName + "."
											+ operation.testCase + "] key=" + key + " value=null");
								}
							}
								break;

							case getall: {
								HashSet<Object> keys = createGetAllKeySet(operation);
								Map<Object, Object> map = operation.icache.getAll(keys);
								if (map == null) {
									writeLine(threadNum + ". [" + group.name + "." + operation.dsName + "."
											+ operation.testCase + "] returned null");
								} else if (map.size() < keys.size()) {
									writeLine(threadNum + ". [" + group.name + "." + operation.dsName + "."
											+ operation.testCase + "] returned " + map.size() + "/" + keys.size());
								}
							}
								break;

							case putall:
							default: {
								HashMap<Object, Object> map = new HashMap<Object, Object>(operation.batchSize, 1f);
								keyIndexes[j] = createPutAllMap(map, operation, keyIndexes[j], threadNum,
										group.threadCount);
								operation.icache.putAll(map);
							}
								break;
							}
							break;

						case queue:
							switch (operation.testCase) {

							case poll: {
								operation.iqueue.poll();
							}
								break;

							case peek: {
								operation.iqueue.peek();
							}
								break;

							case take: {
								try {
									operation.iqueue.take();
								} catch (InterruptedException e) {
									int idNum = operation.startNum + i - 1;
									writeLine(e.getClass().getSimpleName() + ": IQueue.take() interrupted [" + idNum
											+ ". " + operation.name + ", " + operation.dsName + "].");
								}
							}
								break;

							case offer:
							default: {
								int idNum = operation.startNum + i - 1;
								if (operation.dataObjectFactory == null) {
									Blob blob = new Blob(new byte[operation.payloadSize]);
									operation.iqueue.offer(blob);
								} else {
									DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum,
											null);
									operation.iqueue.offer(entry.value);
									// ER objects
									writeEr(operation, entry, i, threadStopIndex, threadStopIndex);
								}
							}
								break;
							}
							break;

						case topic:
						case rtopic:
							switch (operation.testCase) {

							case publish: {
								int idNum = operation.startNum + i - 1;
								if (operation.dataObjectFactory == null) {
									Blob blob = new Blob(new byte[operation.payloadSize]);
									operation.itopic.publish(blob);
								} else {
									DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum,
											null);
									operation.itopic.publish(entry.value);
									// ER objects
									writeEr(operation, entry, i, threadStopIndex, threadStopIndex);
								}
							}
								break;

							case publishall:
							default: {
								int entryCount = operation.totalEntryCount / group.threadCount;
								List<Object> list = new ArrayList<Object>(operation.batchSize);
								int keyIndex = keyIndexes[j];
								if (operation.dataObjectFactory == null) {
									for (int k = 0; k < operation.batchSize; k++) {
										list.add(new Blob(new byte[operation.payloadSize]));
									}
								} else {
									for (int k = 0; k < operation.batchSize; k++) {
										int idNum = operation.startNum + keyIndex;
										DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum,
												null);
										keyIndex++;
										list.add(entry.value);
										if (keyIndex >= threadNum * entryCount) {
											keyIndex = (threadNum - 1) * entryCount;
										}
									}
								}
								try {
									operation.itopic.publishAll(list);
								} catch (Exception e) {
									writeLine(e.getClass().getSimpleName() + ": ITopic.publishAll() error ["
											+ keyIndexes[j] + ". " + operation.name + ", " + operation.dsName + "].");

								}
								keyIndexes[j] = keyIndex;
							}
								break;
							}
							break;

						case sleep:
							Thread.sleep(operation.sleep);
							break;

						case map:
						default:
							switch (operation.testCase) {
							case set: {
								int idNum = operation.startNum + i - 1;
								if (operation.dataObjectFactory == null) {
									String key = operation.keyPrefix + idNum;
									Blob blob = new Blob(new byte[operation.payloadSize]);
									operation.imap.set(key, blob);
								} else {
									DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum,
											null);
									operation.imap.set(entry.key, entry.value);
									// ER objects
									writeEr(operation, entry, i, threadStopIndex, threadStopIndex);
								}
							}
								break;

							case put: {
								int idNum = operation.startNum + i - 1;
								if (operation.dataObjectFactory == null) {
									String key = operation.keyPrefix + idNum;
									Blob blob = new Blob(new byte[operation.payloadSize]);
									operation.imap.put(key, blob);
								} else {
									DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum,
											null);
									operation.imap.put(entry.key, entry.value);
									// ER objects
									writeEr(operation, entry, i, threadStopIndex, threadStopIndex);
								}
							}
								break;

							case get: {
								int val = operation.random.nextInt(operation.totalEntryCount);
								int idNum = operation.startNum + val;
								Object key;
								Object value;
								if (operation.dataObjectFactory == null) {
									key = operation.keyPrefix + idNum;
									value = operation.imap.get(key);
								} else {
									key = operation.dataObjectFactory.getKey(idNum);
									value = operation.imap.get(key);
								}
								if (value == null) {
									writeLine(threadNum + ". [" + group.name + "." + operation.dsName + "."
											+ operation.testCase + "] key=" + key + " value=null");
								}
							}
								break;

							case getall: {
								HashSet<Object> keys = createGetAllKeySet(operation);
								Map<Object, Object> map = operation.imap.getAll(keys);
								if (map == null) {
									writeLine(threadNum + ". [" + group.name + "." + operation.dsName + "."
											+ operation.testCase + "] returned null");
								} else if (map.size() < keys.size()) {
									writeLine(threadNum + ". [" + group.name + "." + operation.dsName + "."
											+ operation.testCase + "] returned " + map.size() + "/" + keys.size());
								}
							}
								break;
								
							case predicate:
								if (operation.predicate != null) {
									operation.imap.values(Predicates.sql(operation.predicate));
								} else if (operation.predicateObj != null) {
									operation.imap.values(operation.predicateObj.getPredicate());
								}
								break;
								
							case sql:
								if (operation.sql != null) {
									hazelcastInstance.getSql().execute(operation.sql);
								} else if (operation.sqlObj != null) {
									hazelcastInstance.getSql().execute(operation.sqlObj.getSql());
								}
								break;
								
							case asyncput:
								break;

							case putall:
							default: {
								HashMap<Object, Object> map = new HashMap<Object, Object>(operation.batchSize, 1f);
								keyIndexes[j] = createPutAllMap(map, operation, keyIndexes[j], threadNum,
										group.threadCount);
								operation.imap.putAll(map);
							}
								break;
							}
							break;
						}
					}
					operationCount++;
				}
			} catch (InterruptedException e) {
				// ignore
			}
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}

	/**
	 * Recursively writes to the the specified operation's ER data structures.
	 * @param operation 
	 * @param entry
	 * @param index
	 * @param threadStartIndex
	 * @param threadStopIndex
	 * @throws InterruptedException 
	 */
	@SuppressWarnings("unchecked")
	private void writeEr(Operation operation, DataObjectFactory.Entry entry, int index, int threadStartIndex,
			int threadStopIndex) throws InterruptedException {
		// Child objects
		if (operation.dataObjectFactory.isEr()) {
			int maxErKeys = operation.dataObjectFactory.getMaxErKeys();
			Operation childOperation = operationMap.get(operation.dataObjectFactory.getErOperationName());
			int maxErKeysPerThread = maxErKeys * (threadStopIndex - threadStartIndex + 1);
			int startErKeyIndex = (threadStartIndex - 1) * maxErKeysPerThread + 1;
			startErKeyIndex = index * maxErKeys + 1;
			if (childOperation != null) {
				boolean isErMaxRandom = operation.dataObjectFactory.isErMaxRandom();
				if (isErMaxRandom) {
					maxErKeys = operation.random.nextInt(maxErKeys) + 1;
				}
				for (int k = 0; k < maxErKeys; k++) {
					int childIdNum = startErKeyIndex + k;
					DataObjectFactory.Entry childEntry = childOperation.dataObjectFactory.createEntry(childIdNum,
							entry.key);
					switch (childOperation.ds) {
					case sleep:
						Thread.sleep(childOperation.sleep);
						break;
						
					case rmap:
						if (childOperation.rmap != null) {
							childOperation.rmap.put(childEntry.key, childEntry.value);
						}
						break;
					case cache:
						if (childOperation.icache != null) {
							childOperation.icache.put(childEntry.key, childEntry.value);
						}
						break;
					case queue:
						if (childOperation.iqueue != null) {
							childOperation.iqueue.offer(childEntry.value);
						}
						break;
					case topic:
					case rtopic:
						if (childOperation.itopic != null) {
							childOperation.itopic.publish(entry.value);
						}
						break;
					case map:
					default:
						if (childOperation.imap != null) {
							switch (childOperation.testCase) {
							case set:
								childOperation.imap.set(childEntry.key, childEntry.value);
								break;
							case put:
							default:
								childOperation.imap.put(childEntry.key, childEntry.value);
								break;
							}
						}
					}
					writeEr(childOperation, entry, index, threadStartIndex, threadStopIndex);
				}
			}
		}
	}

	private int createPutAllMap(HashMap<Object, Object> map, Operation operation, int keyIndex, int threadNum,
			int threadCount) {
		int entryCount = operation.totalEntryCount / threadCount;
		if (operation.dataObjectFactory == null) {
			for (int k = 0; k < operation.batchSize; k++) {
				String key = operation.keyPrefix + (operation.startNum + keyIndex);
				keyIndex++;
				map.put(key, new Blob(new byte[operation.payloadSize]));
				if (keyIndex >= threadNum * entryCount) {
					keyIndex = (threadNum - 1) * entryCount;
				}
			}
		} else {
			for (int k = 0; k < operation.batchSize; k++) {
				int idNum = operation.startNum + keyIndex;
				DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum, null);
				keyIndex++;
				map.put(entry.key, entry.value);
				if (keyIndex >= threadNum * entryCount) {
					keyIndex = (threadNum - 1) * entryCount;
				}
			}
		}

		return keyIndex;
	}

	private HashSet<Object> createGetAllKeySet(Operation operation) {
		HashSet<Object> keys = new HashSet<Object>(operation.batchSize, 1f);
		if (operation.dataObjectFactory == null) {
			for (int k = 0; k < operation.batchSize; k++) {
				int keyIndex = operation.random.nextInt(operation.totalEntryCount);
				String key = operation.keyPrefix + (operation.startNum + keyIndex);
				keys.add(key);
			}
		} else {
			for (int k = 0; k < operation.batchSize; k++) {
				int keyIndex = operation.random.nextInt(operation.totalEntryCount);
				Object key = operation.dataObjectFactory.getKey(keyIndex);
				keys.add(key);
			}
		}
		return keys;
	}

	/**
	 * GroupDbTestThread applies group tasks to the DB configured by Hibernate.
	 * 
	 * @author dpark
	 *
	 */
	class GroupDbTestThread extends AbstractThread {
		public GroupDbTestThread(int threadNum, int threadStartIndex, int invocationCountPerThread, Group group) {
			super(threadNum, threadStartIndex, invocationCountPerThread, group);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void __run() {
			int threadStopIndex = threadStartIndex + invocationCountPerThread - 1;
			int keyIndexes[] = new int[group.operations.length];
			for (int i = 0; i < keyIndexes.length; i++) {
				Operation operation = group.operations[i];
				int entryCount = operation.totalEntryCount / group.threadCount;
				keyIndexes[i] = (threadNum - 1) * entryCount;
			}

			final Session session;
			try {
				session = HibernatePool.getHibernatePool().takeSession();
			} catch (Exception e) {
				throw new RuntimeException("HibernatePool interrupted. GroupDbTestThread Aborted.", e);
			}
			if (session == null) {
				throw new RuntimeException("Unable to get a HibernatePool session. GroupDbTestThread Aborted.");
			}

			long startTime = System.currentTimeMillis();
			for (int i = threadStartIndex; i <= threadStopIndex; i++) {

				for (int j = 0; j < group.operations.length; j++) {
					Operation operation = group.operations[j];
					switch (operation.testCase) {
					case set:
					case put: {
						int idNum = operation.startNum + i - 1;
						DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum, null);
						Transaction transaction = session.beginTransaction();
						session.saveOrUpdate(entry.value);
						transaction.commit();

						// Child objects
						if (operation.dataObjectFactory.isEr()) {
							int maxErKeys = operation.dataObjectFactory.getMaxErKeys();
							Operation childOperation = operationMap
									.get(operation.dataObjectFactory.getErOperationName());
							int maxErKeysPerThread = maxErKeys * (threadStopIndex - threadStartIndex + 1);
							int startErKeyIndex = (threadStartIndex - 1) * maxErKeysPerThread + 1;
							startErKeyIndex = i * maxErKeys + 1;
							if (childOperation != null) {
								boolean isErMaxRandom = operation.dataObjectFactory.isErMaxRandom();
								if (isErMaxRandom) {
									maxErKeys = operation.random.nextInt(maxErKeys) + 1;
								}
								for (int k = 0; k < maxErKeys; k++) {
									int childIdNum = startErKeyIndex + k;
									DataObjectFactory.Entry childEntry = childOperation.dataObjectFactory
											.createEntry(childIdNum, entry.key);
									Transaction childTransaction = session.beginTransaction();
									session.saveOrUpdate(childEntry.value);
									childTransaction.commit();
								}
							}
						}
					}
						break;

					case get: {
						int val = operation.random.nextInt(operation.totalEntryCount);
						int idNum = operation.startNum + val;
						Class<?> entityClass = operation.dataObjectFactory.getDataObjectClass();
						Object key = operation.dataObjectFactory.getKey(idNum);
						Object value = session.find(entityClass, key);
						if (value == null) {
							writeLine(threadNum + ". [" + group.name + "." + operation.dsName + "." + operation.testCase
									+ "] key=" + key + " value=null");
						}
					}
						break;

					case getall: {
						HashSet<Object> keys = new HashSet<Object>(operation.batchSize, 1f);

						for (int k = 0; k < operation.batchSize; k++) {
							int keyIndex = operation.random.nextInt(operation.totalEntryCount);
							Object key = operation.dataObjectFactory.getKey(keyIndex);
							keys.add(key);
						}
						Class<?> entityClass = operation.dataObjectFactory.getDataObjectClass();
						CriteriaBuilder cb = session.getCriteriaBuilder();
						CriteriaQuery<?> cr = cb.createQuery(entityClass);
						Root root = cr.from(entityClass);
						String pk = root.getModel().getId(String.class).getName();
						String getterMethodName = getGetter(pk);
						Method method = null;
						try {
							method = entityClass.getMethod(getterMethodName);
						} catch (NoSuchMethodException | SecurityException e1) {
							throw new RuntimeException("Getter method retrieval failed. GroupDbTestThread Aborted.",
									e1);
						}
						if (method == null) {
							throw new RuntimeException(
									"Unable to retrieve the primary key getter method. GroupDbTestThread Aborted.");
						}

						// Query the DB with a batch of primary keys at a time to
						// reduce the client query time
						Iterator<?> iterator = keys.iterator();
						int size = keys.size();
						int k = 1;
						Map<Object, Object> map = new HashMap<Object, Object>();
						while (k <= size) {
							In<String> inClause = cb.in(root.get(pk));
							while (iterator.hasNext() && k % operation.batchSize > 0) {
								Object key = iterator.next();
								inClause.value(key.toString());
								cr.select(root).where(inClause);
								k++;
							}
							if (iterator.hasNext()) {
								Object key = iterator.next();
								inClause.value(key.toString());
								cr.select(root).where(inClause);
								k++;
							}
							Query<?> query = session.createQuery(cr);
							List<?> valueList = query.getResultList();
							try {
								for (Object value : valueList) {
									Object key = method.invoke(value);
									map.put(key, value);
								}
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								throw new RuntimeException(
										"Getter method invocation failed. GroupDbTestThread Aborted.", e);
							}
						}
						if (map.size() < keys.size()) {
							writeLine(threadNum + ". [" + group.name + "." + operation.dsName + "." + operation.testCase
									+ "] returned " + map.size() + "/" + keys.size());
						}
					}
						break;

					case putall:
					default: {
						int entryCount = operation.totalEntryCount / group.threadCount;
						HashMap<Object, Object> map = new HashMap<Object, Object>(operation.batchSize, 1f);
						int keyIndex = keyIndexes[j];
						for (int k = 0; k < operation.batchSize; k++) {
							int idNum = operation.startNum + keyIndex;
							DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum, null);
							keyIndex++;
							map.put(entry.key, entry.value);
							if (keyIndex >= threadNum * entryCount) {
								keyIndex = (threadNum - 1) * entryCount;
							}
						}
						Transaction transaction = session.beginTransaction();
						map.forEach((id, value) -> session.saveOrUpdate(value));
						transaction.commit();
						keyIndexes[j] = keyIndex;
					}
						break;
					}
				}
				operationCount++;
			}
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;

			HibernatePool.getHibernatePool().offerSession(session);
		}

		private String getGetter(String fieldName) {
			char c = fieldName.charAt(0);
			if (Character.isAlphabetic(c)) {
				fieldName = Character.toUpperCase(c) + fieldName.substring(1);
			}
			return "get" + fieldName;
		}
	}

	/**
	 * Recursive deletes data structures including child operations.
	 * 
	 * @param operation Parent operation
	 * @param delete    true to delete, false to print size info only.
	 */
	private void deleteOperation(Operation operation, boolean delete) {
		switch (operation.ds) {
		case rmap:
			operation.rmap = hazelcastInstance.getReplicatedMap(operation.dsName);
			int size = operation.rmap.size();
			if (delete) {
				operation.rmap.destroy();
			}
			writeLine("  - name: " + operation.dsName);
			writeLine("    data: ReplicatedMap");
			writeLine("    size: " + size);
			writeLine("    deleted: " + delete);
			break;
		case cache:
			operation.icache = hazelcastInstance.getCacheManager().getCache(operation.dsName);
			size = operation.icache.size();
			if (delete) {
				operation.icache.destroy();
			}
			writeLine("  - name: " + operation.dsName);
			writeLine("    data: ICache");
			writeLine("    size: " + size);
			writeLine("    deleted: " + operation.icache.isDestroyed());
			break;
		case queue:
			operation.iqueue = hazelcastInstance.getQueue(operation.dsName);
			size = operation.iqueue.size();
			if (delete) {
				operation.iqueue.destroy();
			}
			writeLine("  - name: " + operation.dsName);
			writeLine("    data: IQueue");
			writeLine("    size: " + size);
			writeLine("    deleted: " + delete);
			break;
		case topic:
			operation.itopic = hazelcastInstance.getTopic(operation.dsName);
			if (delete) {
				operation.itopic.destroy();
			}
			writeLine("  - name: " + operation.dsName);
			writeLine("    data: ITopic");
			writeLine("    deleted: " + delete);
			break;
		case rtopic:
			operation.itopic = hazelcastInstance.getReliableTopic(operation.dsName);
			if (delete) {
				operation.itopic.destroy();
			}
			writeLine("  - name: " + operation.dsName);
			writeLine("    data: ReliableTopic");
			writeLine("    deleted: " + delete);
			break;
		case sleep:
			break;
		case map:
		default:
			operation.imap = hazelcastInstance.getMap(operation.dsName);
			size = operation.imap.size();
			if (delete) {
				operation.imap.destroy();
			}
			writeLine("  - name: " + operation.dsName);
			writeLine("    data: IMap");
			writeLine("    size: " + size);
			writeLine("    deleted: " + delete);
			break;
		}

		if (operation.dataObjectFactory != null && operation.dataObjectFactory.getErOperationName() != null) {
			Operation childOperation = operationMap.get(operation.dataObjectFactory.getErOperationName());
			deleteOperation(childOperation, delete);
		}
	}

	/**
	 * Deletes all data structures in all groups
	 * 
	 * @param delete If true, deletes; otherwise, prints each data structure size
	 *               info.
	 */
	private void deleteDataStructures(boolean delete) {
		for (Group[] groups : concurrentGroupList) {
			String groupNames = getGroupNames(groups);
			writeLine();
			writeLine("Running Group(s): " + groupNames);
			writeLine();
			for (Group group : groups) {
				writeLine("group: " + group.name);
				for (Operation operation : group.operations) {
					deleteOperation(operation, delete);
				}
			}
		}
	}

	public void close() {
		HazelcastClient.shutdownAll();
	}

	private static boolean threadsComplete[];

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	@SuppressWarnings("unused")
	private static void write(String str) {
		System.out.print(str);
	}

	private static void usage() {
		String executableName = System.getProperty(PROPERTY_executableName, GroupTest.class.getName());
		String resultsDirStr = System.getProperty(PROPERTY_resultsDir, DEFAULT_resultsDir);
		writeLine();
		writeLine("Usage:");
		writeLine("   " + executableName + " [-run|-list] [-db|-delete] [-prop <properties-file>] [-?]");
		writeLine();
		writeLine("   Displays or runs group test cases specified in the properties file.");
		writeLine("   A group represents a function that executes one or more Hazelcast data");
		writeLine("   structure operations. This program measures average latencies and throughputs");
		writeLine("   of group (or function) executions.");
		writeLine("   The default properties file is");
		writeLine("      " + DEFAULT_groupPropertiesFile);
		writeLine();
		writeLine("       -run              Runs test cases.");
		writeLine();
		writeLine("       -list             Lists data structures and their sizes.");
		writeLine();
		writeLine("       -db               Runs test cases on database instead of Hazelcast. To use this");
		writeLine("                         option, each test case must supply a data object factory class");
		writeLine("                         by specifying the 'factory.class' property and Hibernate must");
		writeLine("                         be configured by running the 'build_app' command.");
		writeLine();
		writeLine("       -delete           Deletes (destroys) all the data structures pertaining to the group");
		writeLine("                         test cases that were created in the Hazelcast cluster. If the '-run'");
		writeLine("                         option is not specified, then it has the same effect as the '-list'");
		writeLine("                         option. It only lists data strcutures and their without deleting them.");
		writeLine();
		writeLine("       -failover         Configure failover client using the following config file:");
		writeLine("                           ../etc/hazelcast-client-failover.xml");
		writeLine();
		writeLine("       <properties-file> Optional properties file path.");
		writeLine();
		writeLine("   To run the the test cases, specify the '-run' option. Upon run completion, the results");
		writeLine("   will be outputted in the following directory:");
		writeLine("      " + resultsDirStr);
		writeLine();
		writeLine("Notes:");
		writeLine("   ICache requires explicit configuration. It will fail if you run it without first configuring");
		writeLine("   the cluster with caches.");
		writeLine();
	}

	@SuppressWarnings("unchecked")
	private static Operation parseOperation(String operationName, HashSet<String> erOperationNamesSet)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		String dsName = null;
		DataStructureEnum ds = null;
		for (DataStructureEnum ds2 : DataStructureEnum.values()) {
			dsName = System.getProperty(operationName + "." + ds2.name());
			if (dsName != null) {
				ds = ds2;
				break;
			}
		}
		Operation operation = operationMap.get(operationName);
		if (operation == null) {
			operation = new Operation();
			operation.name = operationName;
			operationMap.put(operationName, operation);
			operation.ds = ds;
			if (ds == DataStructureEnum.sleep) {
				try {
					operation.sleep = Integer.parseInt(dsName);
					if (operation.sleep <= 0) {
						operation = null;
					}
				} catch (Exception ex) {
					throw new RuntimeException("Parsing error: " + operation.name + ".sleep=" + dsName, ex);
				}
			} else {
				operation.ref = System.getProperty(operationName + ".ref");
				operation.dsName = dsName;

				String testCase = System.getProperty(operationName + ".testCase");
				if (testCase != null) {
					operation.testCase = TestCaseEnum.getTestCase(testCase);
				}
				Integer payloadSize = Integer.getInteger(operationName + ".payloadSize");
				if (payloadSize != null) {
					operation.payloadSize = payloadSize;
				}
				operation.keyPrefix = System.getProperty(operationName + ".key.prefix");
				Integer startNum = Integer.getInteger(operationName + ".key.startNum");
				if (startNum != null) {
					operation.startNum = startNum;
				}
				Integer totalEntryCount = Integer.getInteger(operationName + ".totalEntryCount");
				if (totalEntryCount != null) {
					operation.totalEntryCount = totalEntryCount;
				}
				Integer batchSize = Integer.getInteger(operationName + ".batchSize");
				if (batchSize != null) {
					operation.batchSize = batchSize;
				}
				Long randomSeed = Long.getLong(operationName + ".randomSeed");
				if (randomSeed != null) {
					operation.random = new Random(randomSeed);
				}
				String factoryClassName = System.getProperty(operationName + ".factory.class");
				if (factoryClassName != null) {
					Class<DataObjectFactory> clazz = (Class<DataObjectFactory>) Class.forName(factoryClassName);
					operation.dataObjectFactory = clazz.newInstance();
					Properties factoryProps = getFactoryProps(operationName);
					operation.dataObjectFactory.initialize(factoryProps);
					String factoryErOperationName = factoryProps.getProperty("factory.er.operation");
					if (factoryErOperationName != null) {
						erOperationNamesSet.add(factoryErOperationName);
					}
				}
				operation.predicate = System.getProperty(operationName + ".predicate");
				operation.sql = System.getProperty(operationName + ".sql");
				
				String predicateClass = System.getProperty(operationName + ".predicate.class");
				if (predicateClass != null) {
					Class<IPredicate> clazz = (Class<IPredicate>) Class.forName(predicateClass);
					operation.predicateObj = clazz.newInstance();
				}
				String sqlClass = System.getProperty(operationName + ".sql.class");
				if (sqlClass != null) {
					Class<ISql> clazz = (Class<ISql>) Class.forName(sqlClass);
					operation.sqlObj = clazz.newInstance();
				}
				operation.sqlArg = System.getProperty(operationName + ".sql.arg");
			}
		}
		return operation;
	}

	private static void parseConfig() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		int defaultThreadCount = (int) (Runtime.getRuntime().availableProcessors() * 1.5);
		int defaultTotalInvocationCount = 10000;
		String groupNamesStr = System.getProperty("groupNames");
		String preGroupNames[] = groupNamesStr.split(",");
		HashSet<String> erOperationNamesSet = new HashSet<String>(10);

		for (int i = 0; i < preGroupNames.length; i++) {
			String preGroupName = preGroupNames[i];
			String[] groupNames = preGroupName.split("&");
			Group[] groups = new Group[groupNames.length];
			concurrentGroupList.add(groups);
			for (int j = 0; j < groupNames.length; j++) {
				String groupName = groupNames[j];
				groupName = groupName.trim();
				Group group = new Group();
				groups[j] = group;
				group.name = groupName;
				group.threadCount = Integer.getInteger(groupName + ".threadCount", defaultThreadCount);
				group.totalInvocationCount = Integer.getInteger(groupName + ".totalInvocationCount",
						defaultTotalInvocationCount);
				String operationsStr = System.getProperty(groupName + ".operations", "putall");
				group.operationsStr = operationsStr;
				String[] split = operationsStr.split(",");

				HashSet<Operation> groupOperationSet = new HashSet<Operation>(split.length);
				ArrayList<Operation> groupOperationList = new ArrayList<Operation>(split.length);
				for (int k = 0; k < split.length; k++) {
					String operationName = split[k];
					operationName = operationName.trim();
					Operation operation = parseOperation(operationName, erOperationNamesSet);
					if (operation != null) {
						groupOperationSet.add(operation);
						groupOperationList.add(operation);
					}
				}

				// ER
				HashSet<Operation> erOperationSet = new HashSet<Operation>(split.length);
				for (String eRperationName : erOperationNamesSet) {
					Operation operation = parseOperation(eRperationName, erOperationNamesSet);
					if (operation != null) {
						erOperationSet.add(operation);
					}
				}

				// Combined
				HashSet<Operation> allOperationSet = new HashSet<Operation>(groupOperationSet);
				allOperationSet.addAll(erOperationSet);

				// Set references
				for (Operation operation : allOperationSet) {
					if (operation.ref != null) {
						Operation refOperation = operationMap.get(operation.ref);
						if (refOperation != null) {
							if (operation.dsName == null) {
								operation.dsName = refOperation.dsName;
							}
							if (operation.ds == null) {
								operation.ds = refOperation.ds;
							}
							if (operation.testCase == null) {
								operation.testCase = refOperation.testCase;
							}
							if (operation.payloadSize == -1) {
								operation.payloadSize = refOperation.payloadSize;
							}
							if (operation.keyPrefix == null) {
								operation.keyPrefix = refOperation.keyPrefix;
							}
							if (operation.startNum == -1) {
								operation.startNum = refOperation.startNum;
							}
							if (operation.totalEntryCount == -1) {
								operation.totalEntryCount = refOperation.totalEntryCount;
							}
							if (operation.batchSize == -1) {
								operation.batchSize = refOperation.batchSize;
							}
							if (operation.random == null) {
								operation.random = refOperation.random;
							}
							if (operation.predicate == null) {
								operation.predicate = refOperation.predicate;
							}
							if (operation.predicateObj == null) {
								operation.predicateObj = refOperation.predicateObj;
							}
							if (operation.sql == null) {
								operation.sql = refOperation.sql;
							}
							if (operation.sqlObj == null) {
								operation.sqlObj = refOperation.sqlObj;
							}
							if (operation.sqlArg == null) {
								operation.sqlArg = refOperation.sqlArg;
							}
						}
					}
					if (operation.ds == null) {
						operation.ds = DataStructureEnum.map;
					}
				}

				// Set default values if not defined.
				for (Operation operation : allOperationSet) {
					if (operation.ds == DataStructureEnum.sleep) {
						continue;
					}
					if (operation.dsName == null) {
						operation.dsName = "map1";
					}
					if (operation.testCase == null) {
						operation.testCase = TestCaseEnum.putall;
					}
					if (operation.payloadSize == -1) {
						operation.payloadSize = 1024;
					}
					if (operation.keyPrefix == null) {
						operation.keyPrefix = "k";
					}
					if (operation.startNum == -1) {
						operation.startNum = 1;
					}
					if (operation.totalEntryCount == -1) {
						operation.totalEntryCount = 10000;
					}
					if (operation.batchSize == -1) {
						operation.batchSize = 100;
					}
					if (operation.random == null) {
						operation.random = new Random(1);
					}
				}

				// groupOperationList contains all operations including repeating ones
				// in the order they were defined.
				group.operations = groupOperationList.toArray(new Operation[0]);
				group.comment = System.getProperty(groupName + ".comment", "");
			}
		}
	}

	/**
	 * Returns all properties with the prefix operationName + ".factory"
	 * 
	 * @param operationName Operation name
	 */
	private static Properties getFactoryProps(String operationName) {
		Properties props = new Properties();
		Set<Map.Entry<Object, Object>> entrySet = (Set<Map.Entry<Object, Object>>) System.getProperties().entrySet();
		String keyPrefix = operationName + ".key.";
		String prefix = operationName + ".factory.";
		String replaceStr = operationName + ".";
		for (Map.Entry<Object, Object> entry : entrySet) {
			String key = entry.getKey().toString();
			if (key.startsWith(prefix) || key.startsWith(keyPrefix)) {
				props.put(key.replaceFirst(replaceStr, ""), entry.getValue());
			}
		}
		return props;
	}

	private static String getGroupNames(Group[] groups) {
		String groupNames = "";
		for (int i = 0; i < groups.length; i++) {
			if (i == 0) {
				groupNames = groups[i].name;
			} else {
				groupNames += " & " + groups[i].name;
			}
		}
		return groupNames;
	}

	public static void main(String args[]) throws Exception {
		boolean showConfig = true;
		boolean runDb = false;
		boolean delete = false;
		boolean list = false;
		String perfPropertiesFilePath = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.equals("-run")) {
				showConfig = false;
			} else if (arg.equals("-list")) {
				list = true;
			} else if (arg.equals("-db")) {
				runDb = true;
			} else if (arg.equals("-delete")) {
				delete = true;
			} else if (arg.equals("-failover")) {
				IS_FAILOVER_CLIENT = true;
			} else if (arg.equals("-prop")) {
				if (i < args.length - 1) {
					perfPropertiesFilePath = args[++i].trim();
				}
			}
		}

		// Exit if more than one run option specified
		if (runDb && delete) {
			if (!showConfig || delete) {
				System.err.println("ERROR: Must specify only one of -db or -delete.");
				System.err.println("       Command aborted.");
				System.exit(1);
			}
		}

		if (perfPropertiesFilePath == null) {
			perfPropertiesFilePath = DEFAULT_groupPropertiesFile;
		}

		File file = new File(perfPropertiesFilePath);
		Properties perfProperties = new Properties();
		if (file.exists() == false) {
			System.err.println("Perf properties file does not exist: ");
			System.err.println("   " + file.getAbsolutePath());
			System.err.println("Command aborted.");
			System.exit(1);
		} else {
			FileReader reader = new FileReader(file);
			perfProperties.load(reader);
			reader.close();
			System.getProperties().putAll(perfProperties);
		}
		PRINT_STATUS_INTERVAL_IN_SEC = Integer.getInteger(PROPERTY_printStatusIntervalInSec,
				DEFAULT_printStatusIntervalInSec);
		TEST_COUNT = Integer.getInteger(PROPERTY_testCount, DEFAULT_testCount);
		TEST_INTERVAL_IN_MSEC = Integer.getInteger(PROPERTY_testIntervalInMsec, DEFAULT_testIntervalInMsec);

		parseConfig();

		String dbHeader = "";
		if (runDb) {
			dbHeader = " (Database)";
			for (Group[] groups : concurrentGroupList) {
				for (Group group : groups) {
					for (Operation operation : group.operations) {
						if (operation.dataObjectFactory == null) {
							System.err.println("ERROR: data object factory not set for group " + group.name
									+ ", operation " + operation.name + ".");
							System.err.println("       Set '" + operation.name
									+ ".factory.class' in the propertie file," + perfPropertiesFilePath + ".");
							System.err.println("       Command aborted.");
							System.exit(1);
						}
					}
				}
			}
		}
		writeLine();
		writeLine("***************************************");
		if (showConfig) {
			writeLine("Group Test Configuration" + dbHeader);
		} else {
			writeLine("Group Test" + dbHeader);
		}
		writeLine("***************************************");
		writeLine();
		if (file.exists()) {
			writeLine("Configuration File: " + file.getAbsolutePath());
		} else {
			writeLine("Configuration File: N/A");
		}

		if (!delete) {
			writeLine();
			writeLine("                    Product: " + PRODUCT);
			writeLine("             Test Run Count: " + TEST_COUNT);
			writeLine("   Test Run Interval (msec): " + TEST_INTERVAL_IN_MSEC);

			for (Group[] groups : concurrentGroupList) {
				String groupNames = getGroupNames(groups);
				writeLine();
				writeLine("- Concurrent Group(s): " + groupNames);
				for (int i = 0; i < groups.length; i++) {
					Group group = groups[i];
					int countPerThread = group.totalInvocationCount / group.threadCount;
					writeLine("                               Group Name: " + group.name);
					writeLine("                                  Comment: " + group.comment);
					writeLine("                               Operations: " + group.operationsStr);
					writeLine("          Total Invocation Count Per Test: " + group.totalInvocationCount);
					writeLine("                             Thread Count: " + group.threadCount);
					writeLine("              Invocation Count Per Thread: " + countPerThread);
					writeLine("   Actual Total Invocation Count Per Test: " + countPerThread * group.threadCount);
					writeLine("");
				}
			}
		}

		writeLine();

		if (showConfig) {
			if (delete || list) {
				// Show data structures only
				GroupTest groupTest = new GroupTest(false);
				groupTest.deleteDataStructures(false);
				groupTest.close();
				writeLine();
			}
			writeLine("To run the test, specify the option, '-run'.");
			writeLine();
			return;
		}

		writeLine("Please wait until done. This may take some time. Status printed in every "
				+ PRINT_STATUS_INTERVAL_IN_SEC + " sec.");
		writeLine("Results:");

		final GroupTest groupTest = new GroupTest(runDb);

		if (delete) {
			groupTest.deleteDataStructures(true);
		} else {

			final boolean __runDb = runDb;

			for (Group[] groups : concurrentGroupList) {
				String groupNames = getGroupNames(groups);
				writeLine();
				writeLine("Running Group(s): " + groupNames);
				writeLine();

				threadsComplete = new boolean[groups.length];

				for (int i = 0; i < groups.length; i++) {
					final Group group = groups[i];
					final int index = i;
					new Thread(new Runnable() {
						public void run() {
							try {
								groupTest.runTest(groupNames, group, __runDb);
								threadsComplete[index] = true;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}

				int loopCount = 0;
				while (true) {
					int threadsCompleteCount = 0;
					loopCount++;
					for (int i = 0; i < threadsComplete.length; i++) {
						if (threadsComplete[i]) {
							threadsCompleteCount++;
						}
					}
					if (threadsCompleteCount == threadsComplete.length) {
						groupTest.printTotalInvocations(groups, loopCount);
						break;
					}
					if (loopCount % PRINT_STATUS_INTERVAL_IN_SEC == 0) {
						groupTest.printTotalInvocations(groups, loopCount);
					}
					Thread.sleep(1000);
				}
			}
		}
		groupTest.close();
		writeLine();
		writeLine("GroupTest complete");
		writeLine();
		System.exit(0);
	}
}
