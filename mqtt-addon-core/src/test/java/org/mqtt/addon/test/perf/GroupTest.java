/*
 * Copyright (c) 2023 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mqtt.addon.test.perf;

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
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttPersistenceException;
import org.mqtt.addon.client.cluster.HaClusters;
import org.mqtt.addon.client.cluster.HaMqttClient;
import org.mqtt.addon.client.cluster.IClusterConfig;
import org.mqtt.addon.test.perf.data.DataObjectFactory;

/**
 * GroupTest is a test tool for capturing the throughput and average latency of
 * multiple MQTT data structure operations performed as a single atomic
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
 * <td>The number of objects per send() or poll() call per thread. For other
 * operations (test cases), this property is ignored.</td>
 * <td>{@linkplain #DEFAULT_batchSize}</td>
 * </tr>
 * <tr>
 * <td>threadCount</td>
 * <td>The number of threads to concurrently execute send() or poll().</td>
 * <td>{@linkplain #DEFAULT_threadCount}</td>
 * </tr>
 * <tr>
 * <td>Data Structures</td>
 * <td>topic | sleep</td>
 * <td>{@linkplain #DEFAULT_testCase}</td>
 * </tr>
 * <tr>
 * <td>testCase</td>
 * <td>send | sendbatch | poll | pollbatch</td>
 * <td>{@linkplain #DEFAULT_testCase}</td>
 * </tr>
 * </table>
 * 
 * @author dpark
 *
 */
public class GroupTest implements Constants {
	private final static String PRODUCT = "mqtt";

	private static int TEST_COUNT;
	private static int TEST_INTERVAL_IN_MSEC;
	private static int PRINT_STATUS_INTERVAL_IN_SEC;
	private static List<Group[]> concurrentGroupList = new ArrayList<Group[]>(4);
	private static HashMap<String, Operation> operationMap = new HashMap<String, Operation>();
	
	private static String clusterName = System.getProperty("cluster.name", IClusterConfig.DEFAULT_CLUSTER_NAME);

	enum DataStructureEnum {
		topic, sleep
	}

	enum TestCaseEnum {
		publish;

		static TestCaseEnum getTestCase(String testCaseName) {
			return publish;
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
		int qos;
		boolean retain;
		int sleep;
		IMqttClient client;
		DataStructureEnum ds;
		TestCaseEnum testCase;
		int totalEntryCount = -1;
		int payloadSize = -1;
		int startNum = -1;
		DataObjectFactory dataObjectFactory;
		Random random;

		@Override
		public Object clone() {
			Operation op = new Operation();
			op.name = name;
			op.ref = ref;
			op.dsName = dsName;
			op.qos = qos;
			op.retain = retain;
			op.sleep = sleep;
			op.ds = ds;
			op.testCase = testCase;
			op.totalEntryCount = totalEntryCount;
			op.payloadSize = payloadSize;
			op.startNum = startNum;
			op.dataObjectFactory = dataObjectFactory;
			op.random = random;
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

	public GroupTest(boolean runDb, boolean publisher) throws Exception {
		init(runDb, publisher);
	}

	private void init(boolean runDb, boolean publisher) throws Exception {
		if (runDb == false) {
			// Get data structures
			for (Operation operation : operationMap.values()) {
				switch (operation.ds) {
				case topic:
				default:
					// dsName maybe null if sleep operation
					if (operation.dsName != null) {
						switch (operation.testCase) {
						case publish:
						default:
							HaMqttClient haclient = HaClusters.getOrCreateHaMqttClient(clusterName);
							if (haclient.isConnected() == false) {
								haclient.connect();
							}
							if (publisher) {
								operation.client = haclient.getPublisher();
							} else {
								operation.client = haclient;
							}
							break;
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

	private void runTest(String concurrentGroupNames, Group group, boolean publisher) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd-HHmmss");
		String resultsDirStr = System.getProperty("results.dir", "results");
		File resultsDir = new File(resultsDirStr);
		if (resultsDir.exists() == false) {
			resultsDir.mkdirs();
		}
		Date startTime = new Date();
		File file = new File(resultsDir,
				"group-" + group.name + "-" + PRODUCT + "-" + format.format(startTime) + ".txt");

		writeLine("   " + file.getAbsolutePath());

		int countPerThread = group.totalInvocationCount / group.threadCount;

		PrintWriter writer = new PrintWriter(file);
		writer.println("******************************************");
		writer.println("Group Test");
		writer.println("******************************************");
		writer.println();
		writer.println("                       Product: " + PRODUCT);
		writer.println("                     Publisher: " + publisher);
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

						case sleep:
							Thread.sleep(operation.sleep);
							break;

						case topic:
						default:
							switch (operation.testCase) {
							case publish:
							default: {
								int idNum = operation.startNum + i - 1;
								byte[] payload;
								if (operation.dataObjectFactory == null) {
									payload= new byte[operation.payloadSize];
								} else {
									payload = operation.dataObjectFactory.createPayload(idNum);
								}
								try {
									operation.client.publish(operation.dsName, payload, operation.qos,
											operation.retain);
								} catch (MqttPersistenceException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (MqttException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
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

	public void close() {
		for (Group[] groups : concurrentGroupList) {
			for (Group group : groups) {
				for (Operation operation : group.operations) {
					if (operation.client != null) {
						try {
							operation.client.close();
						} catch (MqttException e) {
							// ignore
						}
					}
				}
			}
		}
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
		writeLine("   " + executableName + " [-run] [-prop <properties-file>] [-?]");
		writeLine();
		writeLine("   Displays or runs group test cases specified in the properties file.");
		writeLine("   A group represents a function that executes one or more MQTT data structure");
		writeLine("   operations. This program measures average latencies and throughputs of");
		writeLine("   group (or function) executions.");
		writeLine();
		writeLine("   The default properties file is");
		writeLine("      " + DEFAULT_groupPropertiesFile);
		writeLine();
		writeLine("       -run              Runs test cases.");
		writeLine();
		writeLine("       -publisher        Bypasses the HaMqttClient API and instead uses the publisher");
		writeLine("                         instance to run the test cases. This means only one MqttClient");
		writeLine("                         instance is used for all test cases, similar to the STICKY");
		writeLine("                         publisher type except that it directly applies the MqttClient");
		writeLine("                         instance.");
		writeLine();
		writeLine("       <properties-file> Optional properties file path.");
		writeLine();
		writeLine("   To run the the test cases, specify the '-run' option. Upon run completion, the results");
		writeLine("   will be outputted in the following directory:");
		writeLine("      " + resultsDirStr);
		writeLine();
	}

	@SuppressWarnings("unchecked")
	private static Operation parseOperation(String operationName, HashSet<String> erOperationNamesSet)
			throws Exception {

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

				String qosStr = System.getProperty(operationName + ".qos");
				if (qosStr != null) {
					operation.qos = Integer.valueOf(qosStr);
					if (operation.qos < 0 || operation.qos > 2) {
						throw new RuntimeException("ERROR: Invalid qos [" + operationName + ".qos=" + operation.qos
								+ "]. Must be 0, 1, or 2.");
					}
				}
				String retainStr = System.getProperty(operationName + ".retain");
				if (retainStr != null) {
					operation.retain = Boolean.valueOf(retainStr);
				}
				String testCase = System.getProperty(operationName + ".testCase");
				if (testCase != null) {
					operation.testCase = TestCaseEnum.getTestCase(testCase);
				}
				Integer payloadSize = Integer.getInteger(operationName + ".payloadSize");
				if (payloadSize != null) {
					operation.payloadSize = payloadSize;
				}
				Integer startNum = Integer.getInteger(operationName + ".key.startNum");
				if (startNum != null) {
					operation.startNum = startNum;
				}
				Integer totalEntryCount = Integer.getInteger(operationName + ".totalEntryCount");
				if (totalEntryCount != null) {
					operation.totalEntryCount = totalEntryCount;
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
			}
		}
		return operation;
	}

	private static void parseConfig() throws Exception {
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
				String operationsStr = System.getProperty(groupName + ".operations", "sendbatch");
				group.operationsStr = operationsStr;
				String[] split = operationsStr.split(",");

				HashSet<Operation> groupOperationSet = new HashSet<Operation>(split.length);
				for (int k = 0; k < split.length; k++) {
					String operationName = split[k];
					operationName = operationName.trim();
					Operation operation = parseOperation(operationName, erOperationNamesSet);
					if (operation != null) {
						groupOperationSet.add(operation);
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
							if (operation.startNum == -1) {
								operation.startNum = refOperation.startNum;
							}
							if (operation.totalEntryCount == -1) {
								operation.totalEntryCount = refOperation.totalEntryCount;
							}
							if (operation.random == null) {
								operation.random = refOperation.random;
							}
						}
					}
					if (operation.ds == null) {
						operation.ds = DataStructureEnum.topic;
					}
				}

				// Set default values if not defined.
				for (Operation operation : allOperationSet) {
					if (operation.ds == DataStructureEnum.sleep) {
						continue;
					}
					if (operation.dsName == null) {
						operation.dsName = "mytopic";
					}
					if (operation.qos < 0 || operation.qos > 2) {
						operation.qos = 0;
					}
					if (operation.testCase == null) {
						operation.testCase = TestCaseEnum.publish;
					}
					if (operation.payloadSize == -1) {
						operation.payloadSize = 1024;
					}
					if (operation.startNum == -1) {
						operation.startNum = 1;
					}
					if (operation.totalEntryCount == -1) {
						operation.totalEntryCount = 10000;
					}
					if (operation.random == null) {
						operation.random = new Random(1);
					}
				}

				group.operations = groupOperationSet.toArray(new Operation[0]);
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
		String prefix = operationName + ".factory.";
		String replaceStr = operationName + ".";
		for (Map.Entry<Object, Object> entry : entrySet) {
			String key = entry.getKey().toString();
			if (key.startsWith(prefix)) {
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
		boolean publisher = false;
		boolean runDb = false;
		String perfPropertiesFilePath = null;
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				System.exit(0);
			} else if (arg.equals("-run")) {
				showConfig = false;
			} else if (arg.equals("-publisher")) {
				publisher = true;
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

		writeLine();
		writeLine("                    Product: " + PRODUCT);
		writeLine("                  Publisher: " + publisher);
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

		writeLine();

		if (showConfig) {
			writeLine("To run the test, specify the option, '-run'.");
			writeLine();
			return;
		}

		writeLine("Please wait until done. This may take some time. Status printed in every "
				+ PRINT_STATUS_INTERVAL_IN_SEC + " sec.");
		writeLine("Results:");

		final GroupTest groupTest = new GroupTest(runDb, publisher);
		final boolean isPublisher = publisher;

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
							groupTest.runTest(groupNames, group, isPublisher);
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
		writeLine();
		writeLine("GroupTest complete");
		writeLine();
		System.exit(0);
	}
}
