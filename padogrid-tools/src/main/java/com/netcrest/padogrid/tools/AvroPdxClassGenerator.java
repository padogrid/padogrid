package com.netcrest.padogrid.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

public class AvroPdxClassGenerator {
	Date timestamp = new Date();

	/**
	 * Creates a AvroPdxClassGenerator object that generates PdxSerializable classes
	 * based on domain class information read from the schemas.
	 * 
	 * @param factoryId    Factory ID.
	 * @param classId      Domain class ID.
	 * @param classVersion If less than 1, then KeyType classes are not versioned
	 *                     and domain classes are not generated.
	 */
	public AvroPdxClassGenerator() {
	}

	public File generateDomainClass(File schemaFile, File srcDir, String targetPackageName) throws IOException, URISyntaxException {
		InputStream is = new FileInputStream(schemaFile);
		String schemaStr = readFile(is);
		JSONObject schema = new JSONObject(schemaStr);
		return generateDomainClass(schemaFile.getName(), schema, srcDir, targetPackageName);
	}

	private String readFile(InputStream is) {
		Scanner reader = new Scanner(is);
		StringBuffer buffer = new StringBuffer();
		while (reader.hasNext()) {
			buffer.append(reader.nextLine()).append("\n");
		}
		reader.close();
		return buffer.toString();
	}

	private String getJavaType(String avroType) {
		if (avroType == null) {
			return "String";
		} else if (avroType.equalsIgnoreCase("string") || avroType.equalsIgnoreCase("fixed")) {
			return "String";
		} else if (avroType.equalsIgnoreCase("double")) {
			return "double";
		} else if (avroType.equalsIgnoreCase("float")) {
			return "float";
		} else if (avroType.equalsIgnoreCase("int")) {
			return "int";
		} else if (avroType.equalsIgnoreCase("long")) {
			return "long";
		} else if (avroType.equalsIgnoreCase("boolean")) {
			return "boolean";
		} else {
			return null;
		}
	}
	
	/**
	 * Returns null if key not found
	 * @param jo
	 * @param key
	 * @return
	 */
	private String getJsonString(JSONObject jo, String key) {
		try {
			return jo.getString(key);
		} catch (Exception ex) {
			return null;
		}
	}

	public File generateDomainClass(String schemaSource, JSONObject schema, File srcDir, String targetPackageName)
			throws IOException, URISyntaxException {
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("PdxDomainClassTemplate.txt");
		String template = readFile(is);
		is.close();

		String namespace = getJsonString(schema, "namespace");
		String name = schema.getString("name");
		JSONArray fields = schema.getJSONArray("fields");
		
		String packageName = null;
		if (targetPackageName != null && targetPackageName.length() > 0) {
			packageName = targetPackageName;
		} else if (namespace != null && namespace.length() > 0) {
			packageName = namespace;
		}
		String domainClassName = name;
		String domainFullClassName;
		if (packageName == null) {
			domainFullClassName = domainClassName;
		} else {
			domainFullClassName = packageName + "." + domainClassName;
		}

		if (packageName == null) {
			template = template.replaceAll("\\$\\{DOMAIN_PACKAGE\\}\\R", "");
		} else {
			template = template.replaceAll("\\$\\{DOMAIN_PACKAGE\\}", packageName);
		}
		template = template.replaceAll("\\$\\{DOMAIN_CLASS_NAME\\}", domainClassName);
		template = template.replaceAll("\\$\\{TIMESTAMP\\}", timestamp.toString());
		template = template.replaceAll("\\$\\{GENERATOR_NAME\\}", AvroPdxClassGenerator.class.getCanonicalName());
		template = template.replaceAll("\\$\\{SCHEMA_SOURCE_NAME\\}", schemaSource);

		// ${IMPORT_PACKAGES}, ${PROPERTY_DEFINITION}
		TreeSet<String> importSet = new TreeSet<String>();
		StringBuffer propDeclarationBuffer = new StringBuffer(2000);
		StringBuffer propMethodsBuffer = new StringBuffer(2000);
		StringBuffer writeBuffer = new StringBuffer(2000);
		StringBuffer readBuffer = new StringBuffer(2000);
		StringBuffer comparableBuffer = new StringBuffer(100);
		StringBuffer compareToBuffer = new StringBuffer(2000);
		StringBuffer toStringBuffer = new StringBuffer(2000);
		boolean isDate = false;
		
		Iterator<?> iterator = fields.iterator();
		ArrayList<String> nameList = new ArrayList<String>();
		while (iterator.hasNext()) {
			JSONObject field = (JSONObject) iterator.next();
			String fieldName;
			Object ft;
			try {
				fieldName = field.getString("name");
				ft = field.get("type");
			} catch (Exception ex) {
				continue;
			}
			Object defaultValue = null;
			String fieldType = null;
			if (ft instanceof JSONArray) {
				List<?> list = ((JSONArray) ft).toList();
				if (list.size() > 0) {
					defaultValue = list.get(0);
				}
				if (list.size() > 1) {
					Object ft2 = list.get(1);
					if (ft2 instanceof Map) {
						Map jo = (Map)ft2;
						fieldType = (String)jo.get("type");
//						String connectVersion = jo.getString("connect.version");
//						String connectName = jo.getString("connect.name");
					} else {
						fieldType = ft2.toString();
					}
				}
			} else {
				fieldType = ft.toString();
			}
			nameList.add(fieldName);

			String javaType = getJavaType(fieldType);
			String pdxType;
			if (javaType == null) {
				// Unsupported Avro type
				if (fieldType == null) {
				} else if (fieldType.equalsIgnoreCase("array")) {
				} else if (fieldType.equalsIgnoreCase("enum")) {
				} else if (fieldType.equalsIgnoreCase("map")) {
				} else if (fieldType.equalsIgnoreCase("record")) {
				} else if (fieldType.equalsIgnoreCase("bytes")) {
					pdxType = "ByteArray";
				}
			} else {

				// PDX types
				pdxType = javaType;
				String methodFieldName = firstChar2UpperCase(fieldName);

				// members
				propDeclarationBuffer.append("\n\t");
				propDeclarationBuffer.append("private ");
				propDeclarationBuffer.append(javaType);
				propDeclarationBuffer.append(" ");
				propDeclarationBuffer.append(fieldName);
				propDeclarationBuffer.append(";");

				// setter
				propMethodsBuffer.append("\n\t");
				propMethodsBuffer.append("public void set");
				propMethodsBuffer.append(methodFieldName);
				propMethodsBuffer.append("(");
				propMethodsBuffer.append(javaType);
				propMethodsBuffer.append(" ");
				propMethodsBuffer.append(fieldName);
				propMethodsBuffer.append(") {\n");
				propMethodsBuffer.append("\t\t");
				propMethodsBuffer.append("this.");
				propMethodsBuffer.append(fieldName);
				propMethodsBuffer.append("=");
				propMethodsBuffer.append(fieldName);
				propMethodsBuffer.append(";\n");
				propMethodsBuffer.append("\t}\n\n");

				// getter
				propMethodsBuffer.append("\t");
				propMethodsBuffer.append("public ");
				propMethodsBuffer.append(javaType);
				propMethodsBuffer.append(" get");
				propMethodsBuffer.append(methodFieldName);
				propMethodsBuffer.append("() {\n");
				propMethodsBuffer.append("\t\treturn ");
				propMethodsBuffer.append("this.");
				propMethodsBuffer.append(fieldName);
				propMethodsBuffer.append(";\n");
				propMethodsBuffer.append("\t}\n");

//			String importStatement = null;
//			if (type.isPrimitive() == false && type.getPackage().getName().equals("java.lang") == false) {
//				importStatement = "import " + type.getName() + ";\n";
//			}
//			if (importStatement != null) {
//				importSet.add(importStatement);
//			}

				writeBuffer.append("\n\t\t");
				writeBuffer.append("writer.write");

				readBuffer.append("\n\t\t");
				readBuffer.append("this.");
				readBuffer.append(fieldName);
				readBuffer.append(" = reader.read");

				String typeName = firstChar2UpperCase(fieldType);
				writeBuffer.append(typeName);
				readBuffer.append(typeName);
				writeBuffer.append("(\"");
				writeBuffer.append(fieldName);
				writeBuffer.append("\", ");
				writeBuffer.append(fieldName);
				writeBuffer.append(");");
				readBuffer.append("(\"");
				readBuffer.append(fieldName);
				readBuffer.append("\");");
			}
		}

		// toString()
		// Sort names
		String[] sortedNames = nameList.toArray(new String[nameList.size()]);
		Arrays.sort(sortedNames);
		for (int i = 0; i < sortedNames.length; i++) {
			String argName = sortedNames[i].substring(0, 1).toLowerCase() + sortedNames[i].substring(1);
			if (i > 0) {
				toStringBuffer.append("\n\t\t\t + ");
				toStringBuffer.append("\", ");
			} else {
				toStringBuffer.append("\"[");
			}
			toStringBuffer.append(argName);
			toStringBuffer.append("=\" + this.");
			toStringBuffer.append(argName);

		}
		toStringBuffer.append(" + \"]\"");

		// ------------

		template = template.replaceAll("\\$\\{PROPERTY_DECLARATION\\}", propDeclarationBuffer.toString());
		template = template.replaceAll("\\$\\{PROPERTY_DEFINITION\\}", propMethodsBuffer.toString());
		propMethodsBuffer.delete(0, propMethodsBuffer.length());
		for (

		String importStatement : importSet) {
			propMethodsBuffer.append(importStatement);
		}
		template = template.replaceAll("\\$\\{IMPORT_PACKAGES\\}", propMethodsBuffer.toString());
		template = template.replaceAll("\\$\\{WRITE_PDX\\}", writeBuffer.toString());
		template = template.replaceAll("\\$\\{READ_PDX\\}", readBuffer.toString());
		template = template.replaceAll("\\$\\{TO_STRING\\}", toStringBuffer.toString());
		template = template.replaceAll("\\$\\{COMPARABLE\\}", comparableBuffer.toString());
		template = template.replaceAll("\\$\\{COMPARE_TO_METHOD\\}", compareToBuffer.toString());

		String importPackages = "";
		if (isDate) {
			importPackages = "import java.util.Date;";
		}
		if (importPackages.length() == 0) {
			template = template.replaceAll("\\$\\{IMPORT_PACKAGES\\}.*\\R\\R", "");
		} else {
			template = template.replaceAll("\\$\\{IMPORT_PACKAGES\\}", importPackages);
		}

		// Write to file
		srcDir.mkdirs();
		String domainClassFilePath = domainFullClassName.replaceAll("\\.", "/") + ".java";
		File domainClassFile = new File(srcDir, domainClassFilePath);
		domainClassFile.getParentFile().mkdirs();
		FileWriter writer = null;
		try {
			writer = new FileWriter(domainClassFile);
			writer.write(template);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

		return domainClassFile;
	}

	private String firstChar2UpperCase(String str) {
		return Character.toString(str.charAt(0)).toUpperCase() + str.substring(1);
	}

	/**
	 * Generates KeyType classes for all schema files found in the specified schema
	 * directory.
	 * 
	 * @param schemaDir Directory where schema files are kept.
	 * @param srcDir    Source directory where KeyType classes are generated.
	 */
	public void generateAll(File schemaDir, File srcDir, String targetPackageName) {
		if (schemaDir == null) {
			System.err.println("Schema directory must be specified.");
			return;
		}
		if (srcDir == null) {
			System.err.println("Source directory must be specified.");
			return;
		}
		File schemaFiles[] = schemaDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".avsc");
			}

		});

		if (schemaFiles == null) {
			writeLine();
			writeLine("Schema directory not valid: " + schemaDir.getAbsolutePath());
			writeLine();
		} else if (schemaFiles.length == 0) {
			writeLine();
			writeLine("Schema files not found in directory " + schemaDir.getAbsolutePath());
			writeLine();
		} else {
			srcDir.mkdirs();
			writeLine();
			for (int i = 0; i < schemaFiles.length; i++) {
				writeLine((i + 1) + ". " + schemaFiles[i].getAbsolutePath());
				try {
					File domainClassFile = generateDomainClass(schemaFiles[i], srcDir, targetPackageName);
					if (domainClassFile != null) {
						writeLine("   Generated: " + domainClassFile.getAbsolutePath());
					}
				} catch (Exception e) {
					writeLine("   Error: " + e.getMessage());
					System.err.println(e);
					e.printStackTrace();
				}
			}
//
//			writeLine();
//			writeLine("DESCRIPTION");
//			writeLine("          The generated classes are set to the default values shown below. The");
//			writeLine("          class IDs are incremented starting from first class ID.");
//			writeLine();
//			writeLine("SYSTEM PROPERTIES");
//			writeLine("          You can change the default IDs using the following system properties.");
//			writeLine("          If you change the factory ID then you must also set the same value in");
//			writeLine("          the configuration files.");
			writeLine();
			writeLine("Source code generation complete.");
			writeLine();
		}
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

	private static void usage() {
		String executable = System.getenv("EXECUTABLE");
		if (executable == null) {
			executable = AvroPdxClassGenerator.class.getSimpleName();
		}
		writeLine();
		writeLine("NAME");
		writeLine("   " + executable + " - Generate PdxSerializable classes based on Avro schema files");
		writeLine();
		writeLine("SNOPSIS");
		writeLine("   " + executable
				+ " [-schemaDir schema_directory] [-srcDir output_directory] [-v version_number] [-?]");
		writeLine();
		writeLine("CAUTION");
		writeLine("   This command overwrites the existing source code. Make sure to back up the");
		writeLine("   output source directory first before running this command in case if you need");
		writeLine("   to revert to the existing code.");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Generates PdxSerializable classes based on the specified Avro schema files.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -schemaDir schema_directory");
		writeLine("             Import directory path that contains *.avsc files");
		writeLine();
		writeLine("   -srcDir output_directory");
		writeLine("             Source directory path where the key type classes are to be generated");
		writeLine();
		writeLine("   -tp target_package");
		writeLine("             The name of the package where the target classes to be generated. This option");
		writeLine("             overrides the 'namespace' value in the schemas. If this option is not specified");
		writeLine("             and the 'namespace' entry is not found in the schemas, then the classes are");
		writeLine("             created in the default package, i.e., no package name is assigned.");
		writeLine();
		writeLine("   -v version_number");
		writeLine("             Versions the generated PdxSerializable class. If version number is not specified");
		writeLine("             then it defaults to 1.");
		writeLine();
		writeLine("DEFAULT");
		writeLine("   " + executable + " -schemaDir data/schema -srcDir src/generated -v 1");
		writeLine();
		System.exit(0);
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		String arg;
		String schemaDirPath = "etc/avro";
		String srcDirPath = "src/generated";
		String targetPackageName = null;
		int version = 1;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-schemaDir")) {
				if (i < args.length - 1) {
					schemaDirPath = args[++i];
				}
			} else if (arg.equals("-srcDir")) {
				if (i < args.length - 1) {
					srcDirPath = args[++i];
				}
			} else if (arg.equals("-tp")) {
				if (i < args.length - 1) {
					targetPackageName = args[++i].trim();
				}
			} else if (arg.equals("-v")) {
				if (i < args.length - 1) {
					version = Integer.parseInt(args[++i]);
				} else {
					version = 1;
				}
			}
		}
		AvroPdxClassGenerator generator = new AvroPdxClassGenerator();
		File schemaDir = new File(schemaDirPath);
		File srcDir = new File(srcDirPath);
		generator.generateAll(schemaDir, srcDir, targetPackageName);
	}
}
