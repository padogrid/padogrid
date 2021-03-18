package com.netcrest.padogrid.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * DebeziumAvroSchemaGenerator generates the Avro schema files from the Debezium
 * schemas retrieved from the Apicurio registry.
 * 
 * @author dpark
 *
 */
public class DebeziumAvroSchemaGenerator {

	public final static String PROPERTY_executableName = "executable.name";
	public final static String DEFAULT_REGISTRY_ARTIFACTS_URL = "http://localhost:8080";
	public final static String DEFAULT_ARTIFACTS_FILE = "etc/artifacts.json";
	public final static String DEFAULT_RESOURCES_DIR = "src/main/resources";

	File artifactsFile = new File(DEFAULT_ARTIFACTS_FILE);
	File resourcesDirFile = new File(DEFAULT_RESOURCES_DIR);

	String schemaRegistryArtifactsUrl;

	public DebeziumAvroSchemaGenerator() {
	}

	public DebeziumAvroSchemaGenerator(String registryUrl, String artifactsFilePath, String resourcesDir) {
		this.schemaRegistryArtifactsUrl = registryUrl;
		if (artifactsFilePath != null) {
			this.artifactsFile = new File(artifactsFilePath);
		}
		File file = new File(artifactsFilePath);
		if (file.exists() == false) {
			throw new RuntimeException(
					"ERROR: The specified artifacts file path does not exist: [" + artifactsFilePath + "]. Command aborted.");
		}
		if (resourcesDir != null) {
			this.resourcesDirFile = new File(resourcesDir);
		}
		if (resourcesDirFile.exists() == false) {
			resourcesDirFile.mkdirs();
		}
	}

	public void generateAvroSchemaFiles(Properties props) throws Exception {
		String packageName = props.getProperty("package.name");
		if (packageName == null) {
			throw new RuntimeException("Property undefined [package.name]. Command aborted.");
		}
		String artifactIds = props.getProperty("artifact.ids");
		if (artifactIds == null) {
			throw new RuntimeException("Property undefined [artifact.ids]. Command aborted.");
		}
		StringTokenizer st = new StringTokenizer(artifactIds, ",");
		while (st.hasMoreTokens()) {
			String artifactId = st.nextToken();
			int index = artifactId.lastIndexOf(".");
			String simpleName;
			if (index == -1) {
				simpleName = artifactId;
			} else {
				simpleName = artifactId.substring(index + 1);
			}
			if (simpleName.endsWith("-value")) {
				simpleName = simpleName.substring(0, simpleName.lastIndexOf("-"));
			}
			if (simpleName.endsWith("s")) {
				simpleName = simpleName.substring(0, simpleName.length() - 1);
			}
			simpleName = Character.toUpperCase(simpleName.charAt(0)) + simpleName.substring(1);
			JSONObject schema = getHttp(schemaRegistryArtifactsUrl, artifactId, packageName, simpleName, null);
			System.out.println(schema.toString(4));
		}
	}

	public void generateAvroSchemaFiles(JSONObject schemaJo) throws Exception {
		String url = schemaRegistryArtifactsUrl;
		if (url == null) {
			if (schemaJo.isNull("registry") == false) {
				url = schemaJo.getString("registry");
			}
		}
		if (url == null) {
			url = DEFAULT_REGISTRY_ARTIFACTS_URL;
		}

		if (url.endsWith("/") == false) {
			url = url + "/";
		}
		if (url.endsWith("api/artifacts/") == false) {
			url = url + "api/artifacts/";
		}

		ArrayList<JSONObject> schemaList = new ArrayList<JSONObject>();
		String packageName = schemaJo.getString("package");
		JSONArray artifacts = schemaJo.getJSONArray("artifacts");
		writeLine();
		writeLine("Schema Registry: " + url);
		writeLine(" Artifacts List: " + artifactsFile);
		writeLine("        Package: " + packageName);
		writeLine();
		writeLine("Generating schema files...");
		for (int i = 0; i < artifacts.length(); i++) {
			JSONObject jo = (JSONObject) artifacts.getJSONObject(i);
			String artifactId = jo.getString("id");
			String simpleName;
			if (jo.isNull("name")) {
				int index = artifactId.lastIndexOf(".");
				if (index == -1) {
					simpleName = artifactId;
				} else {
					simpleName = artifactId.substring(index + 1);
				}
				if (simpleName.endsWith("-key") || simpleName.endsWith("-value")) {
					simpleName = simpleName.substring(0, simpleName.lastIndexOf("-"));
				}
				if (simpleName.endsWith("s")) {
					simpleName = simpleName.substring(0, simpleName.length() - 1);
				}
				simpleName = Character.toUpperCase(simpleName.charAt(0)) + simpleName.substring(1);
			} else {
				simpleName = jo.getString("name");
			}
			JSONObject fieldMap = null;
			if (jo.isNull("fieldMap") == false) {
				fieldMap = jo.getJSONObject("fieldMap");
			}
			JSONObject schema = getHttp(url, artifactId, packageName, simpleName, fieldMap);
			schemaList.add(schema);
		}

		for (JSONObject schema : schemaList) {
			File file = writeSchema(schema);
			writeLine("   " + file);
		}
		writeLine("Complete.");
		writeLine();
	}

	private File writeSchema(JSONObject schema) throws IOException {
		if (schema == null) {
			return null;
		}
		String fileName;
		if (schema.isNull("file")) {
			fileName = schema.getString("name") + ".avsc";
		} else {
			fileName = schema.getString("file");
		}
		File file = new File(resourcesDirFile, fileName);
		FileWriter writer = new FileWriter(file);
		writer.write(schema.toString(4));
		writer.close();
		return file;
	}

	private JSONObject getHttp(String urlStr, String artifactId, String packageName, String simpleName,
			JSONObject fieldMap) throws Exception {

		JSONObject value = null;
		String requestUrl = urlStr + artifactId;

		URL url = new URL(requestUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			JSONObject jo = new JSONObject(response.toString());
			JSONArray fields = jo.getJSONArray("fields");
			for (int i = 0; i < fields.length(); i++) {
				JSONObject jo2 = fields.getJSONObject(i);
				String name = jo2.getString("name");
				if (name != null && name.equals("before")) {
					JSONArray type = jo2.getJSONArray("type");
					for (int j = 0; j < type.length(); j++) {
						Object obj = type.get(j);
						if (obj instanceof JSONObject) {
							JSONObject record = ((JSONObject) obj);
							String name2 = record.getString("name");
							if (name2 != null && name2.equals("Value")) {
								value = new JSONObject();
								JSONArray fields2 = record.getJSONArray("fields");
								if (fieldMap != null) {
									fields2.forEach(item -> {
										JSONObject jo3 = (JSONObject) item;
										String name3 = jo3.getString("name");
										if (fieldMap.isNull(name3) == false) {
											jo3.put("name", fieldMap.getString(name3));
										}
									});
								}
								value.put("fields", record.get("fields"));
								break;
							}
						}
					}
					if (value != null) {
						break;
					}
				}
			}
			if (value != null) {
				value.put("namespace", packageName);
				value.put("type", "record");
				value.put("name", simpleName);
			}

		} else {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			writeLine("ERROR: REST call failed [responseCode]. " + response.toString());
		}
		return value;
	}

	private static void writeLine() {
		System.out.println();
	}

	private static void writeLine(String line) {
		System.out.println(line);
	}

	private static void write(String str) {
		System.out.print(str);
	}

	private static JSONObject readJsonFile(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
		String line;
		StringBuffer buffer = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
		}
		reader.close();
		JSONObject jo = new JSONObject(buffer.toString());
		return jo;
	}

	private static void usage() {
		String executableName = System.getProperty(PROPERTY_executableName,
				DebeziumAvroSchemaGenerator.class.getName());
		writeLine();
		writeLine("NAME");
		writeLine("   " + executableName + " - Generate AVRO schema files");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine("    " + executableName + " [-artifacts artifacts_path] [-dir resources_directory]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine();
		writeLine("   Generates AVRO schema files by retrieving the artifacts defined in the artifacts");
		writeLine("   file. The default file is 'etc/artifacts.json'. The following is an example showing");
		writeLine("   the JSON format.");
		writeLine();
		writeLine("   {\n" + "	\"registry\": \"http://localhost:8080\",\n"
				+ "	\"package\": \"org.hazelcast.demo.nw.data.avro.generated\",\n" + "	\"artifacts\": [ \n"
				+ "		{\n" + "		\"id\": \"dbserver1.public.customers-value\",\n"
				+ "		 \"name\": \"__Customer\",\n" + "		 \"fieldMap\": \n" + "		 	{ \n"
				+ "		 		\"customerid\" : \"customerId\",\n" + "		 		\"address\" : \"address\",\n"
				+ "		 		\"city\" : \"city\",\n" + "		 		\"companyname\" : \"companyName\",\n"
				+ "		 		\"contactname\" : \"contactName\",\n"
				+ "		 		\"contacttitle\" : \"contactTitle\",\n" + "		 		\"country\" : \"country\",\n"
				+ "		 		\"fax\" : \"fax\",\n" + "		 		\"phone\" : \"phone\",\n"
				+ "		 		\"postalcode\" : \"postalCode\",\n" + "		 		\"region\" : \"region\"\n"
				+ "		 	}\n" + "		},\n" + "		{\n"
				+ "		\"id\": \"dbserver1.public.orders-value\",\n" + "		 \"name\": \"__Order\"\n" + "		}\n"
				+ "	]\n" + "   }");
		writeLine();
		writeLine("   \"package\"");
		writeLine("             [Required] package name.");
		writeLine();
		writeLine("   \"artifacts\"");
		writeLine("             [Required] defines a list of artifacts to retrieve from the schema registry.");
		writeLine();
		writeLine("   \"id\"");
		writeLine(
				"             [Required] defines the artifact ID, which always ends with '-key' for key attrbutes or '-value'");
		writeLine("             value attributes.");
		writeLine();
		writeLine("   \"registry\"");
		writeLine(
				"             [Optional] defines the schema registry URL. Note that the '-registry' option overrides");
		writeLine("             this attribute.");
		writeLine();
		writeLine("   \"name\"");
		writeLine(
				"             [Optional] defines the AVRO generated class name. If it is not specified then the last token");
		writeLine("             of \"id\" without the '-key' or '-value'. It also sets the first letter to uppercase");
		writeLine("             and removes 's' from the end to make it singular.");
		writeLine();
		writeLine("   \"fieldMap\"");
		writeLine(
				"             [Optional] defines a map for converting table column names to object field names. Keys");
		writeLine("             represent column names and values represent matching object field names.");
		writeLine();
		writeLine("OPTIONS");
		writeLine();
		writeLine("   -registry registry_url");
		writeLine(
				"             Registry URL. This option overrides the registry URL defined in the artifacts JSON file.");
		writeLine(
				"             If this option is not specified and the 'registry' key is not defined in the artifacts");
		writeLine("             JSON file the it defaults to " + DEFAULT_REGISTRY_ARTIFACTS_URL);
		writeLine();
		writeLine("   -artifacts artifacts_path");
		writeLine("             The artifacts JSON file path. Default: etc/artifacts.json");
		writeLine();
		writeLine("   -dir resources_directory");
		writeLine("              Directory path in which to generate schema files. If this option is not");
		writeLine("              specified then the resources directory defaults to '" + DEFAULT_RESOURCES_DIR + "'.");
		writeLine();
		writeLine("DEFAULT");
		writeLine("   " + executableName + " -registry " + DEFAULT_REGISTRY_ARTIFACTS_URL + " -artifacts "
				+ DEFAULT_ARTIFACTS_FILE + " -dir " + DEFAULT_RESOURCES_DIR);
		writeLine();
		System.exit(0);
	}

	public static void main(String... args) throws Exception {
		String arg;
		String registryUrl = null;
		String artifactsFilePath = DEFAULT_ARTIFACTS_FILE;
		String srcDir = DEFAULT_RESOURCES_DIR;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-registry")) {
				if (i < args.length - 1) {
					registryUrl = args[++i].trim();
				}
			} else if (arg.equals("-artifacts")) {
				if (i < args.length - 1) {
					artifactsFilePath = args[++i].trim();
				}
			} else if (arg.equals("-dir")) {
				if (i < args.length - 1) {
					srcDir = args[++i].trim();
				}
			}
		}

		File file = new File(artifactsFilePath);
		if (file.exists() == false) {
			writeLine("ERROR: The specified artifacts file path does not exist: [" + artifactsFilePath + "].");
			writeLine("       Command aborted.");
			System.exit(1);
		}

		DebeziumAvroSchemaGenerator generator = new DebeziumAvroSchemaGenerator(registryUrl, artifactsFilePath, srcDir);
		JSONObject schemaJo = readJsonFile(artifactsFilePath);
		try {
			generator.generateAvroSchemaFiles(schemaJo);
		} catch (Exception ex) {
			System.err.println("ERROR: " + ex);
		}
	}
}
