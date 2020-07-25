package org.oracle.coherence.addon.test.perf;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.oracle.coherence.addon.test.perf.data.Blob;
import org.oracle.coherence.addon.test.perf.data.DataObjectFactory;

import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

/**
 * GroupTest is a test tool for capturing the throughput and average latency of
 * multiple Geode IMap operations performed as a single operation. This is
 * achieved by allowing you to group one or more operations and invoke them as a
 * single call.
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
 * <td>testCase</td>
 * <td>eligibility: getall | tx, profile: getall | get</td>
 * <td>{@linkplain #DEFAULT_testCase}</td>
 * </tr>
 * </table>
 * 
 * @author dpark
 *
 */
public class GroupTest implements Constants {
	private static int TEST_COUNT;
	private static int TEST_INTERVAL_IN_MSEC;
	private static int PRINT_STATUS_INTERVAL_IN_SEC;
	private static List<Group[]> concurrentGroupList = new ArrayList<Group[]>(4);
	private static HashMap<String, Operation> operationMap = new HashMap<String, Operation>();

	private Session session;

	enum TestCaseEnum {
		put, putall, get, getall;

		static TestCaseEnum getTestCase(String testCaseName) {
			if (put.name().equalsIgnoreCase(testCaseName)) {
				return put;
			} else if (putall.name().equalsIgnoreCase(testCaseName)) {
				return putall;
			} else if (get.name().equalsIgnoreCase(testCaseName)) {
				return get;
			} else {
				return getall;
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
		GroupTestThread[] threads;
	}

	static class Operation {
		String ref;
		String cachePath;
		NamedCache cache;
		TestCaseEnum testCase;
		int totalEntryCount = -1; // for putall and getall
		int payloadSize = -1;
		int batchSize = -1;
		String keyPrefix;
		int startNum = -1;
		DataObjectFactory dataObjectFactory;
		Random random;

		@Override
		public Object clone() {
			Operation op = new Operation();
			op.ref = ref;
			op.cachePath = cachePath;
			op.cache = cache;
			op.testCase = testCase;
			op.totalEntryCount = totalEntryCount;
			op.payloadSize = payloadSize;
			op.batchSize = batchSize;
			op.keyPrefix = keyPrefix;
			op.startNum = startNum;
			op.dataObjectFactory = dataObjectFactory;
			op.random = random;
			return op;
		}
	}

	public GroupTest() throws Exception {
		init();
	}

	private void init() throws Exception {
		session = Session.create();
	}

	private void printTotalInvocations(Group[] groups, int timeElapsedInSec) {
		long totalInvocationCount;
		for (Group group : groups) {
			totalInvocationCount = 0;
			if (group.threads != null) {
				for (GroupTestThread thread : group.threads) {
					if (thread != null) {
						totalInvocationCount += thread.operationCount;
					}
				}
				System.out.println("[" + timeElapsedInSec + " sec] Invocation Count (" + group.name + "): "
						+ totalInvocationCount);
			}
		}
	}

	private void runTest(String concurrentGroupNames, Group group) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd-HHmmss");
		String resultsDirStr = System.getProperty("results.dir", "results");
		File resultsDir = new File(resultsDirStr);
		if (resultsDir.exists() == false) {
			resultsDir.mkdirs();
		}
		Date startTime = new Date();
		File file = new File(resultsDir, "group-" + group.name + "-" + format.format(startTime) + ".txt");

		System.out.println("   " + file.getAbsolutePath());

		int countPerThread = group.totalInvocationCount / group.threadCount;

		PrintWriter writer = new PrintWriter(file);

		writer.println("******************************************");
		writer.println("Group Test");
		writer.println("******************************************");
		writer.println();
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
		GroupTestThread workerThreads[] = null;

		workerThreads = new GroupTestThread[group.threadCount];
		for (int i = 0; i < workerThreads.length; i++) {
			workerThreads[i] = new GroupTestThread(i + 1, threadStartIndex, countPerThread, group);
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
		writer.println("                Max time (msec): " + maxTimeMsec);
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
				operation.cache = session.getCache(operation.cachePath);
				int entryCount = operation.totalEntryCount / group.threadCount;
				keyIndexes[i] = (threadNum - 1) * entryCount;
			}

			long startTime = System.currentTimeMillis();
			for (int i = threadStartIndex; i <= threadStopIndex; i++) {

				for (int j = 0; j < group.operations.length; j++) {
					Operation operation = group.operations[j];
					switch (operation.testCase) {
					case put: {
						int idNum = operation.startNum + i - 1;
						Object value;
						if (operation.dataObjectFactory == null) {
							String key = operation.keyPrefix + idNum;
							Blob blob = new Blob(new byte[operation.payloadSize]);
							operation.cache.put(key, blob);
						} else {
							DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum);
							operation.cache.put(entry.key, entry.value);
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
							value = operation.cache.get(key);
						} else {
							key = operation.dataObjectFactory.getKey(idNum);
							value = operation.cache.get(key);
						}
						if (value == null) {
							System.out.println(threadNum + ". [" + group.name + "." + operation.cachePath + "."
									+ operation.testCase + "] key=" + key + " value=null");
						}
					}
						break;

					case getall: {
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
						Map<Object, Object> map = operation.cache.getAll(keys);
						if (map == null) {
							System.out.println(threadNum + ". [" + group.name + "." + operation.cachePath + "."
									+ operation.testCase + "] returned null");
						} else if (map.size() < keys.size()) {
							System.out.println(threadNum + ". [" + group.name + "." + operation.cachePath + "."
									+ operation.testCase + "] returned " + map.size() + "/" + keys.size());
						}
					}
						break;

					case putall:
					default: {
						int entryCount = operation.totalEntryCount / group.threadCount;
						HashMap<Object, Object> map = new HashMap<Object, Object>(operation.batchSize, 1f);
						int keyIndex = keyIndexes[j];
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
								DataObjectFactory.Entry entry = operation.dataObjectFactory.createEntry(idNum);
								keyIndex++;
								map.put(entry.key, entry.value);
								if (keyIndex >= threadNum * entryCount) {
									keyIndex = (threadNum - 1) * entryCount;
								}
							}
						}
						operation.cache.putAll(map);
						keyIndexes[j] = keyIndex;
					}
						break;
					}
				}
				operationCount++;
			}
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}

	public void close() throws Exception {
		session.close();
	}

	private static boolean threadsComplete[];

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	private static void write(String str) {
		System.out.print(str);
	}

	private static void usage() {
		String executableName = System.getProperty(PROPERTY_executableName, GroupTest.class.getName());
		String resultsDirStr = System.getProperty(PROPERTY_resultsDir, DEFAULT_resultsDir);
		writeLine();
		writeLine("Usage:");
		writeLine("   " + executableName + " [-run] [-prop <properties-file>] [-?]");
		writeLine();
		writeLine("   Displays or runs group test cases specified in the properties file.");
		writeLine("   A group represents a function that executes one or more Coherence NamedCache");
		writeLine("   operations. This program measures average latencies and throughputs");
		writeLine("   of group (or function) executions.");
		writeLine("   The default properties file is");
		writeLine("      " + DEFAULT_groupPropertiesFile);
		writeLine("");
		writeLine("       -run              Run test cases.");
		writeLine("       <properties-file> Optional properties file path.");
		writeLine();
		writeLine("   To run the the test cases, specify the '-run' option. Upon run completion, the results");
		writeLine("   will be outputted in the following directory:");
		writeLine("      " + resultsDirStr);
		writeLine();
	}

	@SuppressWarnings("unchecked")
	private static void parseConfig() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		int defaultThreadCount = (int) (Runtime.getRuntime().availableProcessors() * 1.5);
		int defaultTotalInvocationCount = 10000;
		String groupNamesStr = System.getProperty("groupNames");
		String preGroupNames[] = groupNamesStr.split(",");

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
				Operation[] operations = new Operation[split.length];
				for (int k = 0; k < split.length; k++) {
					String operationName = split[k];
					operationName = operationName.trim();
					Operation operation = operationMap.get(operationName);
					if (operation == null) {
						operation = new Operation();
						operationMap.put(operationName, operation);
						operation.ref = System.getProperty(operationName + ".ref");
						if (operation.ref != null) {
							operation.ref = operation.ref.trim();
						}
						operation.cachePath = System.getProperty(operationName + ".map");
						if (operation.cachePath != null) {
							operation.cachePath = operation.cachePath.trim();
						}
						String testCase = System.getProperty(operationName + ".testCase");
						if (testCase != null) {
							if (testCase != null) {
								testCase = testCase.trim();
							}
							operation.testCase = TestCaseEnum.getTestCase(testCase);
						}
						Integer payloadSize = Integer.getInteger(operationName + ".payloadSize");
						if (payloadSize != null) {
							operation.payloadSize = payloadSize;
						}
						operation.keyPrefix = System.getProperty(operationName + ".key.prefix");
						if (operation.keyPrefix != null) {
							operation.keyPrefix = operation.keyPrefix.trim();
						}
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
							if (factoryClassName != null) {
								factoryClassName = factoryClassName.trim();
							}
							Class<DataObjectFactory> clazz = (Class<DataObjectFactory>) Class.forName(factoryClassName);
							operation.dataObjectFactory = clazz.newInstance();
							Properties factoryProps = getFactoryProps(operationName);
							operation.dataObjectFactory.initialize(factoryProps);
						}
					}
					operations[k] = operation;
				}

				// Set references
				for (Operation operation : operations) {
					if (operation.ref != null) {
						Operation refOperation = operationMap.get(operation.ref);
						if (refOperation != null) {
							if (operation.cachePath == null) {
								operation.cachePath = refOperation.cachePath;
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
						}
					}
				}

				// Set default values if not defined.
				for (Operation operation : operations) {
					if (operation.cachePath == null) {
						operation.cachePath = "map1";
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

				group.operations = operations;
				group.comment = System.getProperty(groupName + ".comment", "");
			}
		}
	}
	
	/**
	 * Returns all properties with the prefix operationName + ".factory"
	 * @param operationName Operation name
	 */
	private static Properties getFactoryProps(String operationName)
	{
		Properties props = new Properties();
		Set<Map.Entry<Object, Object>> entrySet = (Set<Map.Entry<Object, Object>>)System.getProperties().entrySet();
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
		String perfPropertiesFilePath = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.equals("-run")) {
				showConfig = false;
			} else if (arg.equals("-prop")) {
				if (i < args.length - 1) {
					perfPropertiesFilePath = args[++i].trim();
				}
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

		System.out.println();
		System.out.println("***************************************");
		if (showConfig) {
			System.out.println("Group Test Configuration");
		} else {
			System.out.println("Group Test");
		}
		System.out.println("***************************************");
		System.out.println();
		if (file.exists()) {
			System.out.println("Configuration File: " + file.getAbsolutePath());
		} else {
			System.out.println("Configuration File: N/A");
		}

		System.out.println();
		System.out.println("             Test Run Count: " + TEST_COUNT);
		System.out.println("   Test Run Interval (msec): " + TEST_INTERVAL_IN_MSEC);

		for (Group[] groups : concurrentGroupList) {
			String groupNames = getGroupNames(groups);
			System.out.println();
			System.out.println("- Concurrent Group(s): " + groupNames);
			for (int i = 0; i < groups.length; i++) {
				Group group = groups[i];
				int countPerThread = group.totalInvocationCount / group.threadCount;
				System.out.println("                               Group Name: " + group.name);
				System.out.println("                                  Comment: " + group.comment);
				System.out.println("                               Operations: " + group.operationsStr);
				System.out.println("          Total Invocation Count Per Test: " + group.totalInvocationCount);
				System.out.println("                             Thread Count: " + group.threadCount);
				System.out.println("              Invocation Count Per Thread: " + countPerThread);
				System.out.println("   Actual Total Invocation Count Per Test: " + countPerThread * group.threadCount);
				System.out.println("");
			}
		}

		System.out.println();

		if (showConfig) {
			System.out.println("To run the test, specify the option, '-run'.");
			System.out.println();
			return;
		}

		System.out.println("Please wait until done. This may take some time. Status printed in every "
				+ PRINT_STATUS_INTERVAL_IN_SEC + " sec.");
		System.out.println("Results:");

		final GroupTest groupTest = new GroupTest();

		for (Group[] groups : concurrentGroupList) {
			String groupNames = getGroupNames(groups);
			System.out.println();
			System.out.println("Running Group(s): " + groupNames);
			System.out.println();

			threadsComplete = new boolean[groups.length];

			for (int i = 0; i < groups.length; i++) {
				final Group group = groups[i];
				final int index = i;
				new Thread(new Runnable() {
					public void run() {
						try {
							groupTest.runTest(groupNames, group);
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
		groupTest.close();
		System.out.println();
		System.out.println("GroupTest complete");
		System.out.println();
		System.exit(0);
	}
}
