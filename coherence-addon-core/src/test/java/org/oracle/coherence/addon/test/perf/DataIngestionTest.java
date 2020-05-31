package org.oracle.coherence.addon.test.perf;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.oracle.coherence.addon.test.perf.TransactionTest.TestCaseEnum;
import org.oracle.coherence.addon.test.perf.data.Blob;
import org.oracle.coherence.addon.test.perf.data.ClientProfileKey;
import org.oracle.coherence.addon.test.perf.data.EligKey;

import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

/**
 * DataIngestionTest is a test tool for ingesting Eligibility and ClientProfile
 * mock data into the {@link CacheNameEnum#eligibility} and
 * {@link CacheNameEnum#profile} maps. It launches multiple threads to
 * concurrently ingest data for achieving the highest throughput possible. It
 * also co-locates the maps by the "groupNumber" field so that the
 * {@link TransactionTest} test cases can be performed.
 * <p>
 * DataIngestionTest can be tuned by the following properties set in
 * etc/perf.properties:
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
 * <td>memberSetSize</td>
 * <td>The number of common members for EligKey objects.</td>
 * <td>10</td>
 * </tr>
 * <tr>
 * <td>testCount</td>
 * <td>The number of test runs. Each group has the member size set by the
 * 'memberSetSize' property. Use this property to increase the number of transactions.</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>testIntervalInMsec</td>
 * <td>The amount of time interval (delay) in between test runs in msec. Use this property
 * * to throttle the ingestion rate.</td>
 * <td>0</td>
 * </tr>
 * <tr>
 * <td>mapNames</td>
 * <td>Map names separated by comma. The valid values are defined in
 * {@link CacheNameEnum}.</td>
 * <td>eligibility</td>
 * </tr>
 * <tr>
 * <td>printStatusIntervalInSec</td>
 * <td>Print status interval in sec. The application prints status in this
 * interval to the console.</td>
 * <td>10</td>
 * </tr>
 * <tr>
 * <td>prefix</td>
 * <td>The key prefix prepended to their attributes.</td>
 * <td>x</td>
 * </tr>
 * </table>
 * 
 * <p>
 * The properties described below are DataIngestionTest specific
 * and must begin with the map name followed by '.', e.g.,
 * eligibility.totalEntryCount.
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
 * <td>payloadSize</td>
 * <td>The value object size in bytes. The value object is a blob
 * object containing the specified payload size in the form of
 * a byte array. Note that the key objects are composite objects
 * containing several attributes. This property is used for
 * put and putall test cases only.</td>
 * <td>{@linkplain #DEFAULT_PAYLOAD_SIZE}</td>
 * </tr>
 * <tr>
 * <td>batchSize</td>
 * <td>The number of objects per putAll() call per thread. For put(), this property is ignored.</td>
 * <td>{@linkplain #DEFAULT_batchSize}</td>
 * </tr>
 * <tr>
 * <td>threadCount</td>
 * <td>The number of threads to concurrently execute putAll().</td>
 * <td>{@linkplain #DEFAULT_threadCount}</td>
 * </tr>
 * <tr>
 * <td>testCase</td>
 * <td>putall | put</td>
 * <td>putall</td>
 * </tr>
 * </table>
 * 
 * @author dpark
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DataIngestionTest implements Constants
{
	private static int MEMBER_SET_SIZE;
	private static int TEST_COUNT;
	private static int TEST_INTERVAL_IN_MSEC;
	private static int PRINT_STATUS_INTERVAL_IN_SEC;
	
	private Session session;
	
	AbstractThread[] eligibilityThread;
	AbstractThread[] profileThreads;

	private Date baseDate;
	
	public DataIngestionTest() throws Exception
	{
		init();
		
		// baseDate is used to create Eligibility Date objects
		SimpleDateFormat format = new SimpleDateFormat("MMddyyyy HHmmss.SSS");
		baseDate = format.parse("06132019 000000.000");
	}

	private void init()
	{
		session = Session.create();
	}

	private void printTotalPuts(int timeElapsedInSec)
	{
		long totalPuts;
		if (eligibilityThread != null) {
			totalPuts = 0;
			for (int i = 0; i < eligibilityThread.length; i++) {
				totalPuts += eligibilityThread[i].putCount;
			}
			System.out.println("[" + timeElapsedInSec + " sec] Entry Count (eligibility): " + totalPuts);
		}
		if (profileThreads != null) {
			totalPuts = 0;
			for (int i = 0; i < profileThreads.length; i++) {
				totalPuts += profileThreads[i].putCount;
			}
			System.out.println("[" + timeElapsedInSec + " sec]     Entry Count (profile): " + totalPuts);
		}
	}
	
	private void runTest(CacheNameEnum mapNameEnum, int batchSize, int totalEntryCount, int threadCount, int payloadSize, String prefix, TestCaseEnum testCaseEnum) throws Exception
	{
		Date startTime = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd-HHmmss");
		String resultsDirStr = System.getProperty("results.dir", "results");
		File resultsDir = new File(resultsDirStr);
		if (resultsDir.exists() == false) {
			resultsDir.mkdirs();
		}
		File file = new File(resultsDir, "ingestion-" + mapNameEnum.name() + "-" + format.format(startTime) + "_" + prefix + ".txt");
		
		System.out.println("   " + file.getAbsolutePath());
		
		int countPerThread = totalEntryCount / threadCount;
		
		PrintWriter writer = new PrintWriter(file);
		
		writer.println("******************************************");
		writer.println("Data Ingestion Test");
		writer.println("******************************************");
		writer.println();
		writer.println("                   Test Case: " + testCaseEnum.name());
		writer.println("                         Map: " + mapNameEnum.name());
		if (testCaseEnum == TestCaseEnum.putall) {
			writer.println("           PutAll Batch Size: " + batchSize);
		}
		writer.println("              Test Run Count: " + TEST_COUNT);
		writer.println("    Test Run Interval (msec): " + TEST_INTERVAL_IN_MSEC);
		writer.println("   Total Entry Count Per Run: " + totalEntryCount);
		writer.println("                Thread Count: " + threadCount);
		writer.println("        Payload Size (bytes): " + payloadSize);
		writer.println("                      Prefix: " + prefix);
		writer.println("      Entry Count per Thread: " + countPerThread);
		writer.println();
		writer.println("Start Time: " + startTime);
		
		int threadStartIndex = 1;
		AbstractThread workerThreads[] = null;
		
		switch (mapNameEnum) {
		case eligibility:
			switch (testCaseEnum) {
			case put:
				workerThreads = new EligibilityPutThread[threadCount];
				for (int i = 0; i < workerThreads.length; i++) {
					workerThreads[i] = new EligibilityPutThread(i + 1, mapNameEnum, batchSize, threadStartIndex,
							countPerThread, payloadSize, prefix);
					threadStartIndex += countPerThread;
				}
				break;
			case putall:
			default:
				workerThreads = new EligibilityPutAllThread[threadCount];
				for (int i = 0; i < workerThreads.length; i++) {
					workerThreads[i] = new EligibilityPutAllThread(i + 1, mapNameEnum, batchSize, threadStartIndex,
							countPerThread, payloadSize, prefix);
					threadStartIndex += countPerThread;
				}
				break;
			}
			eligibilityThread = workerThreads;
			break;
			
		case profile:
			switch (testCaseEnum) {
			case put:
				workerThreads = new ProfilePutThread[threadCount];
				for (int i = 0; i < workerThreads.length; i++) {
					workerThreads[i] = new ProfilePutThread(i + 1, mapNameEnum, batchSize, threadStartIndex, countPerThread,
							payloadSize, prefix);
					threadStartIndex += countPerThread;
				}
				break;
			case putall:
			default:
				workerThreads = new ProfilePutAllThread[threadCount];
				for (int i = 0; i < workerThreads.length; i++) {
					workerThreads[i] = new ProfilePutAllThread(i + 1, mapNameEnum, batchSize, threadStartIndex, countPerThread,
							payloadSize, prefix);
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
		int totalPuts = 0;
		for (int i = 0; i < workerThreads.length; i++) {
			try {
				workerThreads[i].join();
				totalPuts += workerThreads[i].putCount;
			} catch (InterruptedException ignore) {
			}
		}
		
		Date stopTime = new Date();
		
		writer.println();
		writer.println("Actual Total Entry (Put) Count: " + totalPuts);
		
		// Report results
		printReport(writer, workerThreads, totalPuts, payloadSize);
		writer.println("Stop Time: " + stopTime);
		writer.println();
		writer.close();
	}
	
	private void printReport(PrintWriter writer, AbstractThread threads[], int totalPuts, int payloadSize)
	{
		writer.println();
		writer.println("Time unit: msec");
		
		long maxTimeMsec = Long.MIN_VALUE;
		for (int i = 0; i < threads.length; i++) {
			writer.println("   Thread " + (i+1) + ": " + (threads[i].totalElapsedTimeInMsec));
			if (maxTimeMsec < threads[i].totalElapsedTimeInMsec) {
				maxTimeMsec = threads[i].totalElapsedTimeInMsec;
			}
		}
		
		double msgPerMsec = (double)totalPuts / (double)maxTimeMsec;
		double msgPerSec = msgPerMsec * 1000;
		
		double bytesPerMSec = ((double)payloadSize*(double)totalPuts) / (double)maxTimeMsec;
		double bytesPerSec = bytesPerMSec * 1000;
		double kiBytesPerSec = bytesPerSec / 1024;
		double miBytesPerSec = kiBytesPerSec / 1024;
		double latencyPerEntry = (double)maxTimeMsec / (double)totalPuts;
		double totalVolumeInKiB = ((double)payloadSize*(double)totalPuts)/(double)(1024);
		double totalVolumeInMiB = ((double)payloadSize*(double)totalPuts)/(double)(1024*1024);
		double totalVolumeInGiB = ((double)payloadSize*(double)totalPuts)/(double)(1024*1024*1024);
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		
		writer.println();
		writer.println("        Max time (msec): " + maxTimeMsec);
		writer.println("   Throughput (msg/sec): " + df.format(msgPerSec));
		writer.println("  *Throughput (KiB/sec): " + df.format(kiBytesPerSec));
		writer.println("  *Throughput (MiB/sec): " + df.format(miBytesPerSec));
		writer.println(" Latency per put (msec): " + df.format(latencyPerEntry));
		writer.println("   **Total Volume (MiB): " + df.format(totalVolumeInKiB));
		writer.println("   **Total Volume (MiB): " + df.format(totalVolumeInMiB));
		writer.println("   **Total Volume (GiB): " + df.format(totalVolumeInGiB));
		writer.println("   Payload Size (bytes): " + payloadSize);
		writer.println();
		writer.println(" * Throughput does not take the keys into account.");
		writer.println("   The actual rate is higher.");
		writer.println("** Total Volume do not take the keys into account.");
		writer.println("   The actual volume is higher.");
		writer.println();
	}
	
	abstract class AbstractThread extends Thread
	{
		int threadNum;
		int batchSize;
		NamedCache cache;
		int payloadSize;
		int threadStartIndex;
		int entryCountPerThread;
		String prefix;
		
		long putCount = 0;
		long elapsedTimeInMsec;
		long totalElapsedTimeInMsec;
		
		AbstractThread(int threadNum, CacheNameEnum mapNameEnum, int batchSize, int threadStartIndex, int entryCountPerThread, int payloadSize, String prefix)
		{
			this.threadNum = threadNum;
			this.batchSize = batchSize;
			this.threadStartIndex = threadStartIndex;
			this.entryCountPerThread = entryCountPerThread;
			cache = session.getCache(mapNameEnum.name());
			this.payloadSize = payloadSize;
			this.prefix = prefix;
		}
		
		@Override
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

	class EligibilityPutThread extends AbstractThread
	{
		public EligibilityPutThread(int threadNum, CacheNameEnum mapNameEnum, int batchSize, int threadStartIndex,
				int entryCountPerThread, int payloadSize, String prefix)
		{
			super(threadNum, mapNameEnum, batchSize, threadStartIndex, entryCountPerThread, payloadSize, prefix);
		}
		
		@Override
		public void __run()
		{
			byte data[] = new byte[payloadSize];
			int threadStopIndex = threadStartIndex + entryCountPerThread - 1;
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
			for (int i = threadStartIndex; i <= threadStopIndex; i+=MEMBER_SET_SIZE) {
				key = prefix + groupNumber;
			
				// Create a set of members that belong to the same group number
				for (int k = 0; k < MEMBER_SET_SIZE; k++) {
					cache.put(new EligKey(key, key, (short) keyIndex, 
								new Date(effectiveDateTime + k), new Date(termDateTime+k), 
								keyIndex, (short)keyIndex, keyIndex, keyIndex), 
							new Blob(data));
					putCount++;
					keyIndex++;
				}
				groupNumber++;
				
				effectiveDateTime += timeDelta;
				termDateTime += timeDelta;
			}
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}
	class EligibilityPutAllThread extends AbstractThread
	{
		public EligibilityPutAllThread(int threadNum, CacheNameEnum mapNameEnum, int batchSize, int threadStartIndex,
				int entryCountPerThread, int payloadSize, String prefix)
		{
			super(threadNum, mapNameEnum, batchSize, threadStartIndex, entryCountPerThread, payloadSize, prefix);
		}
	
		@Override
		public void __run()
		{
			HashMap map = new HashMap();
			byte data[] = new byte[payloadSize];
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
				map.clear();
				for (int j = 0; j < batchSize; j+=MEMBER_SET_SIZE) {
					key = prefix + groupNumber;
				
					// Create a set of members that belong to the same group number
					for (int k = 0; k < MEMBER_SET_SIZE; k++) {
						map.put(new EligKey(key, key, (short) keyIndex, 
									new Date(effectiveDateTime + k), new Date(termDateTime+k), 
									keyIndex, (short)keyIndex, keyIndex, keyIndex), 
								new Blob(data));
						keyIndex++;
					}
					groupNumber++;
					
					effectiveDateTime += timeDelta;
					termDateTime += timeDelta;
				}
				cache.putAll(map);
				putCount += map.size();
			}
			if (remainingCount > 0) {
				map.clear();
				for (int j = 0; j < remainingCount; j+=MEMBER_SET_SIZE) {
					key = prefix + groupNumber;
					for (int k = 0; k < MEMBER_SET_SIZE; k++) {
						map.put(new EligKey(key, key, (short) keyIndex, new Date(effectiveDateTime + k), new Date(termDateTime+k), keyIndex, (short)keyIndex, keyIndex, keyIndex), new Blob(data));
						keyIndex++;
					}
					groupNumber++;
					
					effectiveDateTime += timeDelta;
					termDateTime += timeDelta;
				}
				cache.putAll(map);
				putCount += map.size();
			}
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}
	
	class ProfilePutThread extends AbstractThread
	{
		
		public ProfilePutThread(int threadNum, CacheNameEnum mapNameEnum, int batchSize, int threadStartIndex, int entryCountPerThread, int payloadSize, String prefix)
		{
			super(threadNum, mapNameEnum, batchSize, threadStartIndex, entryCountPerThread, payloadSize, prefix);
		}

		@Override
		public void __run()
		{
			byte data[] = new byte[payloadSize];
			int threadStopIndex = threadStartIndex + entryCountPerThread - 1;
			int keyIndex = threadStartIndex;
			
			String key = null;
			
			long startTime = System.currentTimeMillis();
			for (int i = threadStartIndex; i <= threadStopIndex; i++) {
				key = prefix + keyIndex;
				cache.put(new ClientProfileKey(key, key, key), new Blob(data));
				putCount++;
				keyIndex++;
			}
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}
	
	class ProfilePutAllThread extends AbstractThread
	{
		
		public ProfilePutAllThread(int threadNum, CacheNameEnum mapNameEnum, int batchSize, int threadStartIndex, int entryCountPerThread, int payloadSize, String prefix)
		{
			super(threadNum, mapNameEnum, batchSize, threadStartIndex, entryCountPerThread, payloadSize, prefix);
		}
		
		@Override
		public void __run()
		{
			HashMap map = new HashMap();
			byte data[] = new byte[payloadSize];
			int outerLoopCount = entryCountPerThread / batchSize;
			int remainingCount = entryCountPerThread % batchSize;
			int threadStopIndex = threadStartIndex + outerLoopCount - 1;
			int keyIndex = threadStartIndex;
			
			String key = null;
			
			long startTime = System.currentTimeMillis();
			for (int i = threadStartIndex; i <= threadStopIndex; i++) {
				map.clear();
				for (int j = 0; j < batchSize; j++) {
					key = prefix + keyIndex;
					map.put(new ClientProfileKey(key, key, key), new Blob(data));
					keyIndex++;
				}
				cache.putAll(map);
				putCount += map.size();
			}
			if (remainingCount > 0) {
				map.clear();
				for (int j = 0; j < remainingCount; j++) {
					key = prefix + keyIndex;
					map.put(new ClientProfileKey(key, key, key), new Blob(data));
					keyIndex++;
				}
				cache.putAll(map);
				putCount += map.size();
			}
			long stopTime = System.currentTimeMillis();

			elapsedTimeInMsec = stopTime - startTime;
		}
	}	

	public void close() throws Exception
	{
		session.close();
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
		writeLine("   Displays or runs data ingestion test cases specified in the properties file.");
		writeLine("   The default properties file is");
		writeLine("      " + DEFAULT_ingestionPropertiesFile);
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
			perfPropertiesFilePath = DEFAULT_ingestionPropertiesFile;
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
		MEMBER_SET_SIZE = Integer.getInteger("memberSetSize", 10);
		PRINT_STATUS_INTERVAL_IN_SEC = Integer.getInteger(PROPERTY_printStatusIntervalInSec, DEFAULT_printStatusIntervalInSec);
		TEST_COUNT = Integer.getInteger(PROPERTY_testCount, DEFAULT_testCount);
		TEST_INTERVAL_IN_MSEC = Integer.getInteger(PROPERTY_testIntervalInMsec, DEFAULT_testIntervalInMsec);
		
		String prefix = System.getProperty(PROPERTY_prefix, DEFAULT_prefix);
		
		String mapNamesStr = System.getProperty(PROPERTY_mapNames, DEFAULT_mapNames);
		CacheNameEnum[] mapNameEnums = CacheNameEnum.getMapNameEnums(mapNamesStr);
		
		System.out.println();
		System.out.println("***************************************");
		if (showConfig) {
			System.out.println("Data Ingestion Test Configuration");
		} else {
			System.out.println("Data Ingestion Test");
		}
		System.out.println("***************************************");
		System.out.println();
		if (file.exists()) {
			System.out.println("Configuration File: " + file.getAbsolutePath());
		} else {
			System.out.println("Configuration File: N/A");
		}
		System.out.println();
		System.out.println("                   Test Run Count: " + TEST_COUNT);
		System.out.println("         Test Run Interval (msec): " + TEST_INTERVAL_IN_MSEC);
		
		for (int i = 0; i < mapNameEnums.length; i++) {
			final String mapName = mapNameEnums[i].name();
			final int batchSize = Integer.getInteger(mapName + "." + PROPERTY_batchSize, DEFAULT_batchSize);
			final String testCaseName = System.getProperty(mapName + "." + PROPERTY_testCase, DEFAULT_testCase.name());
			final TestCaseEnum testCaseEnum = TestCaseEnum.getTestCase(testCaseName);
			final int totalEntryCount = Integer.getInteger(mapName + "." + PROPERTY_totalEntryCount, DEFAULT_totalEntryCount);
			final int threadCount = Integer.getInteger(mapName + "." + PROPERTY_threadCount, DEFAULT_threadCount);
			final int payloadSize = Integer.getInteger(mapName + "." + PROPERTY_payloadSize, DEFAULT_PAYLOAD_SIZE);
			final int countPerThread = totalEntryCount / threadCount;
			final double totalVolumeInKiB = ((double)payloadSize*(double)totalEntryCount)/(double)(1024);
			final double totalVolumeInMiB = ((double)payloadSize*(double)totalEntryCount)/(double)(1024*1024);
			final double totalVolumeInGiB = ((double)payloadSize*(double)totalEntryCount)/(double)(1024*1024*1024);
			DecimalFormat df = new DecimalFormat("#.###");
			df.setRoundingMode(RoundingMode.HALF_UP);
			
			System.out.println();
			System.out.println("                        Test Case: " + testCaseEnum.name());
			System.out.println("                              Map: " + mapName);
			
			if (testCaseEnum == TestCaseEnum.putall) {
			System.out.println("                PutAll Batch Size: " + batchSize);
			}
			System.out.println("       Total Entry Count Per Test: " + totalEntryCount);
			System.out.println("                     Thread Count: " + threadCount);
			System.out.println("           Entry Count Per Thread: " + countPerThread);
			System.out.println("Actual Total Entry Count Per Test: " + countPerThread * threadCount);
			System.out.println("             Payload Size (bytes): " + payloadSize);
			System.out.println("             **Total Volume (KiB): " + df.format(totalVolumeInKiB));
			System.out.println("             **Total Volume (MiB): " +  df.format(totalVolumeInMiB));
			System.out.println("             **Total Volume (GiB): " +  df.format(totalVolumeInGiB));
			System.out.println("                           Prefix: " + prefix);
			System.out.println();
		}

		System.out.println();
		
		if (showConfig) {
			System.out.println("To run the test, specify the option, '-run'.");
			System.out.println();
			return;
		}
		
		System.out.println("Please wait until done. This may take some time. Status printed in every " + PRINT_STATUS_INTERVAL_IN_SEC + " sec.");
		System.out.println("Results:");
		
		final DataIngestionTest loader = new DataIngestionTest();

		threadsComplete = new boolean[mapNameEnums.length];
		
		for (int i = 0; i < mapNameEnums.length; i++) {
			final CacheNameEnum mapNameEnum = mapNameEnums[i];
			final String mapName = mapNameEnum.name();
			final int batchSize = Integer.getInteger(mapName + "." + PROPERTY_batchSize, DEFAULT_batchSize);
			final String testCaseName = System.getProperty(mapName + "." + PROPERTY_testCase, DEFAULT_testCase.name());
			final TestCaseEnum testCaseEnum = TestCaseEnum.getTestCase(testCaseName);
			final int totalEntryCount = Integer.getInteger(mapName+ "." + PROPERTY_totalEntryCount, DEFAULT_totalEntryCount);
			final int threadCount = Integer.getInteger(mapName + "." + PROPERTY_threadCount, DEFAULT_threadCount);
			final int payloadSize = Integer.getInteger(mapName+ "." + PROPERTY_payloadSize, DEFAULT_PAYLOAD_SIZE);
			final int index = i;

			new Thread(new Runnable() {
				public void run()
				{
					try {
						loader.runTest(mapNameEnum, batchSize, totalEntryCount, threadCount, payloadSize, prefix, testCaseEnum);
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
				loader.printTotalPuts(loopCount);
				break;
			}
			if (loopCount % PRINT_STATUS_INTERVAL_IN_SEC == 0) {
				loader.printTotalPuts(loopCount);
			}
			Thread.sleep(1000);
		}
		loader.close();
		System.out.println();
		System.out.println("Data ingestion complete");
		System.out.println();
		System.exit(0);
	}
}
