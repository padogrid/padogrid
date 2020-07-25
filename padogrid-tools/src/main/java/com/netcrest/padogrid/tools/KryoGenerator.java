package com.netcrest.padogrid.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.netcrest.pado.internal.util.ClassFinder;

/**
 * KryoGenerator generates the KryoSerializer class for a given package that
 * contains the data classes. Please see the usages for details.
 * 
 * @author dpark
 *
 */
public class KryoGenerator {

	public final static String PROPERTY_executableName = "executable.name";
	public final static int DEFAULT_TYPE_ID = 1100;
	public final static String DEFAULT_SRC_DIR = "src/main/java";

	String packageName;
	String jarPath;
	int typeId = DEFAULT_TYPE_ID;
	File srcDirFile = new File(DEFAULT_SRC_DIR);

	public KryoGenerator(String packageName, String jarPath, int typeId, String srcDir) {
		this.packageName = packageName;
		this.jarPath = jarPath;
		this.typeId = typeId;
		this.srcDirFile = new File(srcDir);
	}

	public void generateKryoSerializer()
			throws IOException, URISyntaxException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		
		// Find all the domain classes in the package.
		Class<?> ksClass = null;
		Class<?>[] classes = ClassFinder.getClasses(jarPath, packageName, false);
		
		if (classes.length == 0) {
			writeLine();
			writeLine("The specified package is empty. Command aborted.");
			writeLine("   Package: " + packageName);
			writeLine("       Jar: " + jarPath);
			writeLine();
			return;
		}
		
		ArrayList<Class<?>> domainClassList = new ArrayList<Class<?>>(classes.length);
		for (Class<?> clazz : classes) {
			if (clazz.getSimpleName().startsWith("KryoSerializer")) {
				if (clazz.getSimpleName().equals("KryoSerializer")) {
					ksClass = clazz;
				}
			} else if (clazz.isMemberClass() || clazz.isAnonymousClass() || clazz.isInterface() || clazz.isEnum()
					|| clazz.isPrimitive() || clazz.isLocalClass() || clazz.isAnnotation()) {
				continue;
			} else {
				domainClassList.add(clazz);
			}
		}

		// If KryoSerializer already exists then determine the new (non-registered) classes
		// and append them at the end of the existing class list. The new KryoSerializer
		// we generate will keep the order of the previous classes and append the new ones.
		ArrayList<Class<?>> newClassList;
		List<Class<?>> classList = null;
		if (ksClass == null) {
			newClassList = new ArrayList<Class<?>>(domainClassList);
		} else {
			newClassList = new ArrayList<Class<?>>(domainClassList.size());
			Method method = ksClass.getMethod("getClassList");
			classList = (List<Class<?>>) method.invoke(ksClass);
			for (Class<?> clazz : domainClassList) {
				method = ksClass.getMethod("getClassId", Class.class);
				Integer classId = (Integer) method.invoke(ksClass, clazz);
				if (classId == null) {
					newClassList.add(clazz);
				}
			}
			domainClassList = new ArrayList<Class<?>>(classList);
			domainClassList.addAll(newClassList);
		}

		if (newClassList.size() == 0) {
			writeLine();
			writeLine("No new classes found in the specified package. Command aborted.");
			writeLine("   Package: " + packageName);
			writeLine("       Jar: " + jarPath);
			writeLine();
			return;
		}
		
		// CLASSES
		int i = 0;
		StringBuffer buffer = new StringBuffer(2000);
		for (Class<?> clazz : domainClassList) {
			if (i > 0) {
				buffer.append(", ");
			}
			buffer.append(clazz.getSimpleName() + ".class");
			i++;
		}
		
		// Update the base string
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("HazelcastKryoSerializerTemplate.txt");
		String kryoSerializerStr = readFile(is);
		is.close();

		String userName = System.getProperty("user.name");
		kryoSerializerStr = kryoSerializerStr.replaceAll("\\$\\{DATE\\}", new Date().toString());
		kryoSerializerStr = kryoSerializerStr.replaceAll("\\$\\{AUTHOR\\}", userName);
		kryoSerializerStr = kryoSerializerStr.replaceAll("\\$\\{KRYO_SERIALIZER_PACKAGE\\}", packageName);
		kryoSerializerStr = kryoSerializerStr.replaceAll("\\$\\{TYPE_ID\\}", Integer.toString(typeId));
		kryoSerializerStr = kryoSerializerStr.replaceAll("\\$\\{CLASSES\\}", buffer.toString());

		// Write to file
		srcDirFile.mkdirs();
		String kryoSerializerClassFileName = packageName.replaceAll("\\.", "/") + "/KryoSerializer.java";
		File kryoSerializerClassFile = new File(srcDirFile, kryoSerializerClassFileName);
		kryoSerializerClassFile.getParentFile().mkdirs();
		FileWriter writer = null;
		try {
			writer = new FileWriter(kryoSerializerClassFile);
			writer.write(kryoSerializerStr);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

		// Print Summary
		writeLine();
		writeLine("  New class count: " + newClassList.size());
		writeLine("Total class count: " + domainClassList.size());
		i = 0;
		for (Class<?> clazz : domainClassList) {
			writeLine("[" + i++ + "] " + clazz.getName());
		}
		writeLine("KryoSerializer generated: ");
		writeLine("   " + kryoSerializerClassFile.getAbsolutePath());
		writeLine();
		writeLine("To register KryoSerializer, add the following lines in the Hazelcast configuration file.");
		writeLine("    <serialization>");
		writeLine("        <serializers>");
		writeLine("             <global-serializer override-java-serialization=\"true\">");
		writeLine("                 " + packageName + ".KryoSerializer");
		writeLine("             </global-serializer>");
		writeLine("        </serializers>");
		writeLine("    </serialization>");
		writeLine();
	}

	private String readFile(InputStream is) throws IOException, URISyntaxException {
		StringBuffer buffer = new StringBuffer(2000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append("\n");
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
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
		String executableName = System.getProperty(PROPERTY_executableName, KryoGenerator.class.getName());
		writeLine();
		writeLine("NAME");
		writeLine("   " + executableName + " - Generate KryoSerializer");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine(
				"    " + executableName + " -package package_name -jar jar_path [-id type_id] [-dir source_directory]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine();
		writeLine("   Generates a KryoSerializer class in the specified package where the domain classes are");
		writeLine("   located. It registers all the classes found in the specified package except KryoSerializer.");
		writeLine("   It assigns the specified type ID to the serializer and reserves a single byte to uniquely");
		writeLine("   identify the classes found in the specified package. The class ID begins from 0 and reserves");
		writeLine("   0xFF for undefined classes. Therefore, a total of 255 classes are allowed per package.");
		writeLine();
		writeLine("   If KryoSerialzer already exists in the package and new classes are added in the package");
		writeLine("   since KryoSerialzer is last created, then the new classes are appended to KryoSerializer");
		writeLine("   by incrementing the last class ID. This ensures the preservance of the existing class IDs.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -package package_name");
		writeLine("             The name of the package where the domain classes are located.");
		writeLine();
		writeLine("   -jar jar_path");
		writeLine("             The path of the jar file that contains the data classes in the specified package.");
		writeLine();
		writeLine("   -id type_id");
		writeLine("              Type ID of KryoSerializer. This ID uniquely identifies the serializer. If this");
		writeLine("              option is not specified then the type ID defaults to " + DEFAULT_TYPE_ID + ".");
		writeLine();
		writeLine("   -dir source_directory");
		writeLine("              Directory path in which to generate KryoSerializer. If this option is not");
		writeLine("              specified then the source directory defaults to '" + DEFAULT_SRC_DIR + "'.");
		writeLine();
		writeLine("DEFAULT");
		writeLine("   " + executableName + " -package package_name -jar jar_path -id " + DEFAULT_TYPE_ID + " -dir "
				+ DEFAULT_SRC_DIR);
		writeLine();
		System.exit(0);
	}

	public static void main(String... args) throws Exception {

		String arg;
		String packageName = null;
		String jarPath = null;
		int typeId = 1100;
		String srcDir = "src/main/java";
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-package")) {
				if (i < args.length - 1) {
					packageName = args[++i].trim();
				}
			} else if (arg.equals("-jar")) {
				if (i < args.length - 1) {
					jarPath = args[++i].trim();
				}
			} else if (arg.equals("-id")) {
				if (i < args.length - 1) {
					String val = args[++i].trim();
					if (val.startsWith("-")) {
						continue;
					}
					typeId = Integer.parseInt(val);
				}
			} else if (arg.equals("-dir")) {
				if (i < args.length - 1) {
					srcDir = args[++i].trim();
				}
			}
		}

		if (packageName == null) {
			writeLine("ERROR: Package name must be specified. Please use the '-package' option to specify the");
			writeLine("       package name. Command aborted.");
			System.exit(1);
		} else if (jarPath == null) {
			writeLine("ERROR: Jar path must be specified. Please use the '-jar' option to specify the");
			writeLine("       jar path. Command aborted.");
			System.exit(1);
		}

		File file = new File(jarPath);
		if (file.exists() == false) {
			writeLine("ERROR: The specified jar path does not exist: [" + jarPath + "].");
			writeLine("       Command aborted.");
			System.exit(1);
		}

		KryoGenerator generator = new KryoGenerator(packageName, jarPath, typeId, srcDir);
		generator.generateKryoSerializer();
	}
}
