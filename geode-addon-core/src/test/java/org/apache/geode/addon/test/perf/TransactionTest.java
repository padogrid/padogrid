package org.apache.geode.addon.test.perf;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.geode.addon.test.perf.data.Blob;
import org.apache.geode.addon.test.perf.data.ClientProfileKey;
import org.apache.geode.addon.test.perf.data.EligKey;
import org.apache.geode.addon.test.perf.data.GroupSummary;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;

/**
 * TransactionTest is a test tool for conducting transactions and querying data
 * from the {@link RegionNameEnum#eligibility} and {@link RegionNameEnum#profile}
 * maps. It launches multiple threads to concurrently execute transaction and
 * query operations for achieving the highest throughput possible. Before
 * running this test, {@link PdxDataIngestionTest} should be run to populate data
 * in the mentioned maps.
 * 
 * TransactionTest share the same properties described in
 * {@link PdxDataIngestionTest}. The properties described below are TransactionTest
 * specific and must begin with the map name followed by '.', e.g.,
 * eligibility.totalEntryCount.
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
 * <td>The number of objects per getAll() call per thread. For other operations (test cases),
 * this property is ignored.</td>
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
public class TransactionTest implements Constants
{
	private final static String PRODUCT="geode";

	private static int MEMBER_SET_SIZE;
	private static int TEST_COUNT;
	private static int TEST_INTERVAL_IN_MSEC;
	private static int PRINT_STATUS_INTERVAL_IN_SEC;
	
	private ClientCache clientCache;
	
	private AbstractThread[] eligibiilityThreads;
	private AbstractThread[] profileThreads;
	
	private Date baseDate;
	
	
	enum TestCaseEnum { 
		tx, put, putall, get, getall; 
		
		static TestCaseEnum getTestCase(String testCaseName) {
			if (tx.name().equalsIgnoreCase(testCaseName)) {
				return tx;
			} else if (put.name().equalsIgnoreCase(testCaseName)) {
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

	public TransactionTest() throws Exception
	{
		init();
	}
	
	private void init() throws Exception
	{
		clientCache = new ClientCacheFactory().create();

		// baseDate is used to create Eligibility Date objects
		SimpleDateFormat format = new SimpleDateFormat("MMddyyyy HHmmss.SSS");
		baseDate = format.parse("06132019 000000.000");
	}

	private void printTotalGets(int timeElapsedInSec)
	{
		long totalGets;
		if (eligibiilityThreads != null) {
			totalGets = 0;
			for (int i = 0; i < eligibiilityThreads.length; i++) {
				totalGets += eligibiilityThreads[i].operationCount;
			}
			System.out.println("[" + timeElapsedInSec + " sec] Transaction Count (eligibility): " + totalGets);
		}
		if (profileThreads != null) {
			totalGets = 0;
			for (int i = 0; i < profileThreads.length; i++) {
				totalGets += profileThreads[i].operationCount;
			}
			System.out.println("[" + timeElapsedInSec + " sec]     Transaction Count (profile): " + totalGets);
		}
	}

	private void runTest(RegionNameEnum mapNameEnum, int batchSize, int totalEntryCount, int threadCount, String prefix, TestCaseEnum testCaseEnum) throws Exception
	{
		Date startTime = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd-HHmmss");
		String resultsDirStr = System.getProperty("results.dir", "results");
		File resultsDir = new File(resultsDirStr);
		if (resultsDir.exists() == false) {
			resultsDir.mkdirs();
		}
		File file = new File(resultsDir, "tx-" + mapNameEnum.name() + "-" + PRODUCT + "-" + format.format(startTime) + "_" + prefix + ".txt");

		System.out.println("   " + file.getAbsolutePath());

		int countPerThread = totalEntryCount / threadCount;
		
		PrintWriter writer = new PrintWriter(file);

		writer.println("******************************************");
		writer.println("Transaction Test");
		writer.println("******************************************");
		writer.println();
		writer.println("                     Product: " + PRODUCT);
		writer.println("                   Test Case: " + testCaseEnum.name());
		writer.println("                         Map: " + mapNameEnum.name());
		if (testCaseEnum == TestCaseEnum.getall) {
			writer.println("           GetAll Batch Size: " + batchSize);
		}
		writer.println("              Test Run Count: " + TEST_COUNT);
		writer.println("    Test Run Interval (msec): " + TEST_INTERVAL_IN_MSEC);
		writer.println("   Total Entry Count Per Run: " + totalEntryCount);
		writer.println("                Thread Count: " + threadCount);
		writer.println("                      Prefix: " + prefix);
		writer.println("      Entry Count per Thread: " + countPerThread);
		writer.println();

		writer.println("Start Time: " + startTime);

		int threadStartIndex = 1;
		AbstractThread workerThreads[] = null;

		switch (mapNameEnum) {
		case eligibility:
			switch (testCaseEnum) {
			case tx:
				workerThreads = new EligibilityGroupSummaryTestThread[threadCount];
				for (int i = 0; i < workerThreads.length; i++) {
					workerThreads[i] = new EligibilityGroupSummaryTestThread(i + 1, mapNameEnum, threadStartIndex, countPerThread,
							prefix);
					threadStartIndex += countPerThread;
				}
				break;				
			case getall:	
			default:
				workerThreads = new EligibilityGetAllTestThread[threadCount];
				for (int i = 0; i < workerThreads.length; i++) {
					workerThreads[i] = new EligibilityGetAllTestThread(i + 1, mapNameEnum, batchSize, threadStartIndex, countPerThread,
							prefix);
					threadStartIndex += countPerThread;
				}
				break;
			}
			eligibiilityThreads = workerThreads;
			break;
		case profile:
		default:
			switch (testCaseEnum) {
			case get:
				workerThreads = new ProfileGetTestThread[threadCount];
				for (int i = 0; i < workerThreads.length; i++) {
					workerThreads[i] = new ProfileGetTestThread(i + 1, mapNameEnum, threadStartIndex, countPerThread, prefix);
					threadStartIndex += countPerThread;
				}
				break;

			case getall:
			default:
				workerThreads = new ProfileGetAllTestThread[threadCount];
				for (int i = 0; i < workerThreads.length; i++) {
					workerThreads[i] = new ProfileGetAllTestThread(i + 1, mapNameEnum, batchSize, threadStartIndex, countPerThread, prefix);
					threadStartIndex += countPerThread;
				}
				break;
			}
			profileThreads = workerThreads;
			break;
		}

		for (int i = 0; i < workerThreads.length; i++) {
			workerThreads[i].start();
		}

		// Wait till all threads are complete
		int totalOperationCount = 0;
		int totalNullCount = 0;
		for (int i = 0; i < workerThreads.length; i++) {
			try {
				workerThreads[i].join();
				totalOperationCount += workerThreads[i].operationCount;
				totalNullCount += workerThreads[i].nullCount;
			} catch (InterruptedException ignore) {
			}
		}
		
		Date stopTime = new Date();
		
		writer.println();
		writer.println("         Actual Total Number of Transactions: " + totalOperationCount);
		writer.println("Total Number of Transactions on Missing Data: " + totalNullCount);
		writer.println();
		writer.println("Missing Data represents the number of transaction operations that are performed on");
	    writer.println("non-existing co-located data. This may occur if there are not enough ClientProfile");
	    writer.println("objects ingested. To avoid this, increase the number of profile objects by setting");
	    writer.println("'profile.totalEntryCount' in the ingestion properties file and rerun the ingestion test.");
	    writer.println("This value must be greater than or equal to 'eligibility.totalEntryCount'.");

		// Report results
		printReport(writer, workerThreads, totalOperationCount);
		writer.println("Stop Time: " + stopTime);
		writer.println();
		writer.close();
	}

	private void printReport(PrintWriter writer, AbstractThread threads[], int totalCount)
	{
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
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		
		writer.println();
		writer.println("                Max Time (msec): " + maxTimeMsec);
		writer.println("            Throughput (tx/sec): " + df.format(txPerSec));
		writer.println(" Latency per transaction (msec): " + df.format(latencyPerEntry));
		writer.println();
	}

	@SuppressWarnings("rawtypes")
	abstract class AbstractThread extends Thread
	{
		int threadNum;
		RegionNameEnum mapNameEnum;
		int batchSize;
		Region region;
		int payloadSize;
		int threadStartIndex;
		int entryCountPerThread;
		String prefix;

		long operationCount = 0;
		long nullCount = 0;
		long elapsedTimeInMsec;
		long totalElapsedTimeInMsec;

		AbstractThread(int threadNum, RegionNameEnum mapNameEnum, int batchSize, int threadStartIndex, int entryCountPerThread, String prefix)
		{
			this.threadNum = threadNum;
			this.batchSize = batchSize;
			this.mapNameEnum = mapNameEnum;
			this.threadStartIndex = threadStartIndex;
			this.entryCountPerThread = entryCountPerThread;
			region = clientCache.getRegion(mapNameEnum.name());
			this.prefix = prefix;
		}
		
		public synchronized void run()
		{
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
	
	class EligibilityGroupSummaryTestThread extends AbstractThread
	{

		public EligibilityGroupSummaryTestThread(int threadNum, RegionNameEnum mapNameEnum, int threadStartIndex, int entryCountPerThread,
				String prefix)
		{
			super(threadNum, mapNameEnum, 0, threadStartIndex, entryCountPerThread, prefix);
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void __run()
		{
			int threadStopIndex = threadStartIndex + entryCountPerThread - 1;			
			int groupNumber = (threadNum - 1) * entryCountPerThread / MEMBER_SET_SIZE + 1;
			long startTime = System.currentTimeMillis();
			for (int i = threadStartIndex; i <= threadStopIndex; i++) {
				String groupNumberStr = prefix + groupNumber;
				ResultCollector rc = FunctionService.onRegion(region)
						.withFilter(Collections.singleton(groupNumberStr))
						.execute(EligFunction.ID);
				@SuppressWarnings("unused")
				List<GroupSummary> summary = (List<GroupSummary>)rc.getResult();
				groupNumber++;
				operationCount++;
			}
			
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}

	class EligibilityGetAllTestThread extends AbstractThread
	{

		public EligibilityGetAllTestThread(int threadNum, RegionNameEnum mapNameEnum, int batchSize, int threadStartIndex, int entryCountPerThread,
				String prefix)
		{
			super(threadNum, mapNameEnum, batchSize, threadStartIndex, entryCountPerThread, prefix);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void __run()
		{
			HashSet<EligKey> keys = new HashSet<EligKey>(batchSize, 1f);
			int outerLoopCount = entryCountPerThread / batchSize;
			int remainingCount = entryCountPerThread % batchSize;
			int threadStopIndex = threadStartIndex + outerLoopCount - 1;
			int keyIndex = threadStartIndex;
			long effectiveDateTime = baseDate.getTime();
			long termDateTime = baseDate.getTime();
			long timeDelta = 1000; // 1 sec
			long baseTick = (threadNum - 1) * entryCountPerThread * timeDelta;
			effectiveDateTime += baseTick;
			termDateTime += baseTick;
			
			String key = null;
			int groupNumber = (threadNum - 1) * entryCountPerThread / MEMBER_SET_SIZE + 1;
			long startTime = System.currentTimeMillis();
			for (int i = threadStartIndex; i <= threadStopIndex; i++) {
				for (int j = 0; j < batchSize; j+=MEMBER_SET_SIZE) {
					key = prefix + groupNumber;
				
					// Create a set of members that belong to the same group number
					for (int k = 0; k < MEMBER_SET_SIZE; k++) {
						keys.add(new EligKey(key, key, (short) keyIndex, 
									new Date(effectiveDateTime + k), new Date(termDateTime+k), 
									keyIndex, (short)keyIndex, keyIndex, keyIndex));
						keyIndex++;
					}
					groupNumber++;
					
					effectiveDateTime += timeDelta;
					termDateTime += timeDelta;
					
					if (keys.size() >= batchSize) {
						Map<EligKey, Blob> map = region.getAll(keys);
						
						if (keys.size() != map.size()) {
							System.out.println("[" + mapNameEnum + " " + threadNum + "] getAll(): keySet does not match the returned map size: [keySet=" + keys.size() + ", map=" + map.size() + "]");
						}
						operationCount += keys.size();
						keys.clear();
					}
				}
			}
			if (remainingCount > 0) {
				for (int j = 0; j < remainingCount; j+=MEMBER_SET_SIZE) {
					key = prefix + groupNumber;
					for (int k = 0; k < MEMBER_SET_SIZE; k++) {
						keys.add(new EligKey(key, key, (short) keyIndex, new Date(effectiveDateTime + k), new Date(termDateTime+k), keyIndex, (short)keyIndex, keyIndex, keyIndex));
						keyIndex++;
					}
					groupNumber++;
					
					effectiveDateTime += timeDelta;
					termDateTime += timeDelta;
				}
				
				Map<EligKey, Blob> map = region.getAll(keys);
				
				if (keys.size() != map.size()) {
					System.out.println("[" + mapNameEnum + " " + threadNum + "] getAll(): keySet does not match the returned map size: [keySet=" + keys.size() + ", map=" + map.size() + "]");
				}
				operationCount += keys.size();
				keys.clear();
		}
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}
	
	class ProfileGetTestThread extends AbstractThread
	{
		public ProfileGetTestThread(int threadNum, RegionNameEnum mapNameEnum, int threadStartIndex, int entryCountPerThread,
				String prefix)
		{
			super(threadNum, mapNameEnum, 0, threadStartIndex, entryCountPerThread, prefix);
		}
		
		@Override
		public void __run()
		{
			int threadStopIndex = threadStartIndex + entryCountPerThread - 1;
			int keyIndex = threadStartIndex;
			int nullBlobCount = 0;

			long startTime = System.currentTimeMillis();
			for (int i = threadStartIndex; i <= threadStopIndex; i++) {
				String key = prefix + (keyIndex++);
				ClientProfileKey profileKey = new ClientProfileKey(key, key, key);
				Blob blob = (Blob) region.get(profileKey);
				if (blob == null) {
					nullBlobCount++;
					System.out.println(nullBlobCount + ". Profile blob=null");
					System.out.println(nullBlobCount + ". " + profileKey);
				}
				operationCount++;
			}
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}
	
	class ProfileGetAllTestThread extends AbstractThread
	{

		public ProfileGetAllTestThread(int threadNum, RegionNameEnum mapNameEnum, int batchSize, int threadStartIndex, int entryCountPerThread,
				String prefix)
		{
			super(threadNum, mapNameEnum, batchSize, threadStartIndex, entryCountPerThread, prefix);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void __run()
		{
			HashSet<ClientProfileKey> keys = new HashSet<ClientProfileKey>(batchSize, 1f);
			int threadStopIndex = threadStartIndex + entryCountPerThread - 1;
			int keyIndex = threadStartIndex;

			long startTime = System.currentTimeMillis();
			for (int i = threadStartIndex; i <= threadStopIndex; i++) {
				String key = prefix + (keyIndex++);
				ClientProfileKey profileKey = new ClientProfileKey(key, key, key);
				keys.add(profileKey);
				if (keys.size() >= batchSize) {
					Map<ClientProfileKey, Blob> map = region.getAll(keys);
					if (keys.size() != map.size()) {
						System.out.println("[" + mapNameEnum + " " + threadNum + "] getAll(): keySet does not match the returned map size: [keySet=" + keys.size() + ", map=" + map.size() + "]");
					}
					keys.clear();
				}
				operationCount++;
			}
			
			// Get all remaining keys
			if (keys.size() < 0) {
				if (keys.size() >= batchSize) {
					Map<ClientProfileKey, Blob> map = region.getAll(keys);
					if (keys.size() != map.size()) {
						System.out.println("[" + mapNameEnum + " " + threadNum + "] getAll(): keySet does not match the returned map size: [keySet=" + keys.size() + ", map=" + map.size() + "]");
					}
					keys.clear();
				}
			}
			
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}

	public void close()
	{
		clientCache.close();
	}

	private static boolean threadsComplete[];

	private static void writeLine()
	{
		System.out.println();
	}

	private static void writeLine(String line)
	{
		System.out.println(line);
	}

	private static void write(String str)
	{
		System.out.print(str);
	}
	
	private static void usage()
	{
		String executableName = System.getProperty(PROPERTY_executableName, TransactionTest.class.getName());
		String resultsDirStr = System.getProperty(PROPERTY_resultsDir, DEFAULT_resultsDir);
		writeLine();
		writeLine("Usage:");
		writeLine("   " + executableName + " [-run] [-prop <properties-file>] [-?]");
		writeLine();
		writeLine("   Displays or runs transaction and query test cases specified in the properties file.");
		writeLine("   The default properties file is");
		writeLine("      " + DEFAULT_txPropertiesFile);
		writeLine("");
		writeLine("       -run              Run test cases.");
		writeLine("       <properties-file> Optional properties file path.");
		writeLine();
		writeLine("   To run the the test cases, specify the '-run' option. Upon run completion, the results");
		writeLine("   will be outputted in the following directory:");
		writeLine("      " + resultsDirStr);
		writeLine();
	}
	
	public static void main(String args[]) throws Exception
	{
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
			perfPropertiesFilePath = DEFAULT_txPropertiesFile;
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
		MEMBER_SET_SIZE = Integer.getInteger(PROPERTY_memberSetSize, DEFAULT_memberSetSize);
		PRINT_STATUS_INTERVAL_IN_SEC = Integer.getInteger(PROPERTY_printStatusIntervalInSec, DEFAULT_printStatusIntervalInSec);
		TEST_COUNT = Integer.getInteger(PROPERTY_testCount, DEFAULT_testCount);
		TEST_INTERVAL_IN_MSEC = Integer.getInteger(PROPERTY_testIntervalInMsec, DEFAULT_testIntervalInMsec);

		String prefix = System.getProperty(PROPERTY_prefix, DEFAULT_prefix);
		
		String mapNamesStr = System.getProperty(PROPERTY_mapNames,  DEFAULT_mapNames);
		RegionNameEnum[] mapNameEnums = RegionNameEnum.getMapNameEnums(mapNamesStr);

		System.out.println();
		System.out.println("***************************************");
		if (showConfig) {
			System.out.println("Transaction Test Configuration");
		} else {
			System.out.println("Transaction Test");
		}
		System.out.println("***************************************");
		System.out.println();
		if (file.exists()) {
			System.out.println("Configuration File: " + file.getAbsolutePath());
		} else {
			System.out.println("Configuration File: N/A");
		}

		System.out.println();
		System.out.println("                          Product: " + PRODUCT);
		System.out.println("                   Test Run Count: " + TEST_COUNT);
		System.out.println("         Test Run Interval (msec): " + TEST_INTERVAL_IN_MSEC);

		for (int i = 0; i < mapNameEnums.length; i++) {
			final RegionNameEnum mapNameEnum = mapNameEnums[i];
			final String mapName = mapNameEnum.name();
			final int batchSize = Integer.getInteger(mapName + "." + PROPERTY_batchSize, DEFAULT_batchSize);
			final String testCaseName = System.getProperty(mapName + "." + PROPERTY_testCase, DEFAULT_testCase.name());
			final TestCaseEnum testCaseEnum = TestCaseEnum.getTestCase(testCaseName);
			final int totalEntryCount = Integer.getInteger(mapName + "." + PROPERTY_totalEntryCount, DEFAULT_totalEntryCount);
			final int threadCount = Integer.getInteger(mapName + "." + PROPERTY_threadCount, DEFAULT_threadCount);
			final int countPerThread = totalEntryCount / threadCount;

			System.out.println();
			System.out.println("                        Test Case: " + testCaseName);
			System.out.println("                              Map: " + mapName);
			if (testCaseEnum == TestCaseEnum.getall) {
			System.out.println("                GetAll Batch Size: " + batchSize);
			}
			System.out.println("       Total Entry Count Per Test: " + totalEntryCount);
			System.out.println("                     Thread Count: " + threadCount);
			System.out.println("           Entry Count Per Thread: " + countPerThread);
			System.out.println("Actual Total Entry Count Per Test: " + countPerThread * threadCount);
			System.out.println("                           Prefix: " + prefix);
		}

		System.out.println();
		
		if (showConfig) {
			System.out.println("To run the test, specify the option, '-run'.");
			System.out.println();
			return;
		}
		
		System.out.println("Please wait until done. This may take some time. Status printed in every " + PRINT_STATUS_INTERVAL_IN_SEC + " sec.");
		System.out.println("Results:");

		final TransactionTest tx = new TransactionTest();

		threadsComplete = new boolean[mapNameEnums.length];

		for (int i = 0; i < mapNameEnums.length; i++) {
			final RegionNameEnum mapNameEnum = mapNameEnums[i];
			final String mapName = mapNameEnum.name();
			final int batchSize = Integer.getInteger(mapName + "." + PROPERTY_batchSize, DEFAULT_batchSize);
			final String testCaseName = System.getProperty(mapName + "." + PROPERTY_testCase, DEFAULT_testCase.name());
			final TestCaseEnum testCaseEnum = TestCaseEnum.getTestCase(testCaseName);
			final int totalEntryCount = Integer.getInteger(mapName + "." + PROPERTY_totalEntryCount, DEFAULT_totalEntryCount);
			final int threadCount = Integer.getInteger(mapName + "." + PROPERTY_threadCount, DEFAULT_threadCount);
			final int index = i;

			new Thread(new Runnable() {
				public void run()
				{
					try {
						tx.runTest(mapNameEnum, batchSize, totalEntryCount, threadCount, prefix, testCaseEnum);
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
				tx.printTotalGets(loopCount);
				break;
			}
			if (loopCount % PRINT_STATUS_INTERVAL_IN_SEC == 0) {
				tx.printTotalGets(loopCount);
			}
			Thread.sleep(1000);
		}
		tx.close();
		System.out.println();
		System.out.println("Transaction complete");
		System.out.println();
		System.exit(0);
	}	
}
