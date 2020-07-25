package org.hazelcast.addon.apps.jet.demo;

import static com.hazelcast.jet.Traversers.traverseArray;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.function.Functions.wholeItem;
import static java.util.Comparator.comparingLong;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.hazelcast.addon.apps.jet.util.JetUtil;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;

/**
 * Demonstrates a simple Word Count job in the Pipeline API. Inserts words from
 * the specified file(s) into IMaps, inserts word counts to separate IMaps, and
 * prints the top 100 frequent words found in each file.
 */
public class WordCountJob {

	private JetInstance jet;
	private File[] files;
	private Pipeline[] pipelines;
	private boolean outputWords = Boolean.getBoolean("outputWords");

	/**
	 * This code illustrates a few more things about Jet, new in 0.5. See comments.
	 */
	private void go(File[] files) {
		this.files = files;
		jet = JetUtil.getJetInstance();
		setupFiles();
		System.out.print("\nCounting words... ");
		long start = System.nanoTime();
		buildPipelines();
		int i = 0;
		for (Pipeline p : pipelines) {
			jet.newJob(p).join();
			System.out.println("[" + i++ + "] Pipeline Done: " + p);
		}
		System.out.print("done in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " milliseconds.");
		printResults();
	}

	private void cleanup() {
		for (int i = 0; i < files.length; i++) {
			jet.getMap(getTextMapName(i)).clear();
			jet.getMap(getCountMapName(i)).clear();
		}
	}

	private Pipeline buildPipeline(int fileNum) {
		Pattern delimiter = Pattern.compile("\\W+");
		Pipeline p = Pipeline.create();
		BatchStage<String> stage = p.readFrom(Sources.<Long, String>map(getTextMapName(fileNum)))
				.flatMap(e -> traverseArray(delimiter.split(e.getValue().toLowerCase())));
				
		if (outputWords) {
			stage = stage.filter(word -> {System.err.println("************Word: " + word); return !word.isEmpty();});
		} else {
			stage = stage.filter(word -> !word.isEmpty());
		}
		stage.groupingKey(wholeItem())
			.aggregate(counting())
			.writeTo(Sinks.map(getCountMapName(fileNum)));
		return p;
	}

	private void buildPipelines() {
		pipelines = new Pipeline[files.length];
		for (int i = 0; i < files.length; i++) {
			pipelines[i] = buildPipeline(i);
		}
	}

	private String getTextMapName(int fileNum) {
		return "text_" + fileNum;
	}

	private String getCountMapName(int fileNum) {
		return "count_" + fileNum;
	}

	private void setup(int fileNum, File file) {
		String mapName = getTextMapName(fileNum);
		try {
			long[] lineNum = { 0 };
			Map<Long, String> bookLines = new HashMap<>();
			InputStream stream = new FileInputStream(file);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
				reader.lines().forEach(line -> bookLines.put(++lineNum[0], line));
			}
			jet.getMap(mapName).putAll(bookLines);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setupFiles() {
		ExecutorService es = Executors.newFixedThreadPool(files.length);
		Future<?>[] futures = new Future<?>[files.length];
		for (int i = 0; i < files.length; i++) {
			final int fileNum = i;
			futures[fileNum] = es.submit(() -> setup(fileNum, files[fileNum]));
		}
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		es.shutdown();
	}

	private void printResults(int fileNum, String filePath) {
		String countMapName = getCountMapName(fileNum);
		final int limit = 100;
		System.out.println();
		System.out.println("[" + fileNum + "] File: " + filePath);
		System.out.println("-----------------------------------------");
		System.out.format(" Top %d entries are:%n", limit);
		final Map<String, Long> counts = jet.getMap(countMapName);
		System.out.println("/-------+---------\\");
		System.out.println("| Count | Word    |");
		System.out.println("|-------+---------|");
		counts.entrySet().stream().sorted(comparingLong(Entry<String, Long>::getValue).reversed()).limit(limit)
				.forEach(e -> System.out.format("|%6d | %-8s|%n", e.getValue(), e.getKey()));
		System.out.println("\\-------+---------/");
		System.out.println();
	}

	private void printResults() {
		for (int i = 0; i < files.length; i++) {
			printResults(i, files[i].getPath());
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Please specify one or more text files separated by space. Command aborted.");
			System.exit(1);
		}

		File files[] = new File[args.length];
		int i = 0;
		for (String filePath : args) {
			File file = new File(filePath);
			if (file.exists() == false) {
				System.err.println("ERROR: Invalid file. File does not exist [" + file + "]");
			}
			files[i++] = file;
		}
		System.setProperty("hazelcast.logging.type", "none");
		WordCountJob job = new WordCountJob();
		job.go(files);
		job.cleanup();
	}
}
