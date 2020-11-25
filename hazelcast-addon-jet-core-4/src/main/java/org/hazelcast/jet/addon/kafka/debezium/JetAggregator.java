package org.hazelcast.jet.addon.kafka.debezium;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.hazelcast.jet.JetInstance;

/**
 * JetAggregator provides Debezium contextual information required by
 * transformer sink jobs.
 * 
 * @author dpark
 *
 */
public class JetAggregator {
	private JetInstance jet;
	private String connectorConfigFile;
	private LocalContext localContext;
	private TransformerContext transformerContext;

	public JetAggregator(JetInstance jet, String connectorConfigFile) {
		this.jet = jet;
		this.connectorConfigFile = connectorConfigFile;
		init();
	}

	public LocalContext getLocalContext() {
		return localContext;
	}

	private void init() {
		try {
			this.localContext = getLocalContext(jet);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private JSONObject readConnectorConfig(InputStream is) throws IOException {
		StringBuffer buffer = new StringBuffer();
		InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);

		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}

		return new JSONObject(buffer.toString());
	}

	private JSONObject readConnectorConfig(String configFilePath) throws IOException {
		File file = new File(configFilePath);
		FileReader reader = new FileReader(file);
		LineNumberReader lnr = new LineNumberReader(reader);
		String line;
		StringBuffer buffer = new StringBuffer();
		while ((line = lnr.readLine()) != null) {
			buffer.append(line);
		}
		lnr.close();
		return new JSONObject(buffer.toString());
	}

	private InputStream getFileFromResourceAsStream(String fileName) {

		// The class loader that loaded the class
		ClassLoader classLoader = JetAggregator.class.getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		// the stream holding the file content
		if (inputStream == null) {
			throw new IllegalArgumentException("file not found! " + fileName);
		} else {
			return inputStream;
		}
	}

	private LocalContext getLocalContext(JetInstance jet) {
		if (localContext == null) {
			try {
				if (connectorConfigFile == null) {
					connectorConfigFile = "jet-connector.json";
				}
				InputStream is = getFileFromResourceAsStream(connectorConfigFile);
				JSONObject json = readConnectorConfig(is);
				localContext = new LocalContext(jet, json);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return localContext;
	}

	public TransformerContext getTransformerContext(JetInstance jet) {
		if (transformerContext == null) {
			try {
				if (connectorConfigFile == null) {
					connectorConfigFile = "jet-connector.json";
				}
				InputStream is = getFileFromResourceAsStream(connectorConfigFile);
				JSONObject json = readConnectorConfig(is);
				transformerContext = new TransformerContext(jet, json);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return transformerContext;
	}
}