package com.netcrest.padogrid.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.netcrest.pado.internal.util.ClassFinder;

/**
 * WrapperGenerator generates extensions to the classes found in the specified
 * package. Please see the usages for details.
 * 
 * @author dpark
 *
 */
public class WrapperGenerator {

	public final static String PROPERTY_executableName = "executable.name";
	public final static int DEFAULT_TYPE_ID = 1100;
	public final static String DEFAULT_SRC_DIR = "src/main/java";
	public final static boolean DEFAULT_IS_OVERWRITE = false;

	String sourcePackageName;
	String targetPackageName;
	String jarPath;
	boolean isOverwrite = false;
	File srcDirFile = new File(DEFAULT_SRC_DIR);

	public WrapperGenerator(String sourcePackageName, String targetPackageName, String jarPath, String srcDir,
			boolean isOverwrite) {
		this.sourcePackageName = sourcePackageName;
		this.targetPackageName = targetPackageName;
		this.jarPath = jarPath;
		this.srcDirFile = new File(srcDir);
		this.isOverwrite = isOverwrite;
	}

	private List<Class<?>> findClasses(String packageName) throws ClassNotFoundException, IOException {
		Class<?>[] classes = ClassFinder.getClasses(jarPath, packageName, false);
		ArrayList<Class<?>> domainClassList = new ArrayList<Class<?>>(classes.length);
		for (Class<?> clazz : classes) {
			if (clazz.isMemberClass() || clazz.isAnonymousClass() || clazz.isInterface() || clazz.isEnum()
					|| clazz.isPrimitive() || clazz.isLocalClass() || clazz.isAnnotation()) {
				continue;
			} else {
				domainClassList.add(clazz);
			}
		}
		return domainClassList;
	}

	public void generateWrappers() throws IOException, URISyntaxException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("WrapperClassTemplate.txt");
		String baseWrapperClassStr = readFile(is);
		is.close();

		String userName = System.getProperty("user.name");
		baseWrapperClassStr = baseWrapperClassStr.replaceAll("\\$\\{DATE\\}", new Date().toString());
		baseWrapperClassStr = baseWrapperClassStr.replaceAll("\\$\\{AUTHOR\\}", userName);
		baseWrapperClassStr = baseWrapperClassStr.replaceAll("\\$\\{WRAPPER_CLASS_PACKAGE\\}", targetPackageName);

		List<Class<?>> sourceClassList = findClasses(sourcePackageName);
		List<Class<?>> existingTargetClassList = findClasses(targetPackageName);
		List<String> targetClassNameList = new ArrayList<String>(sourceClassList.size());
		HashMap<String, Class<?>> targetSourceMap = new HashMap<String, Class<?>>(sourceClassList.size(), 1f);

		// Determine target class names
		for (Class<?> clazz : sourceClassList) {
			String targetClass = clazz.getSimpleName();
			targetClass = targetClass.replaceFirst("^_+", "");
			targetClass = targetClass.replaceFirst("__$+", "");
			targetClass = targetClass.replaceAll("[Aa][Vv][Rr][Oo]", "");
			targetClassNameList.add(targetClass);
			targetSourceMap.put(targetClass, clazz);
		}

		// If not to overwrite the existing target classes then remove
		// the existing ones from targetClassNameList.
		if (isOverwrite == false) {
			// Remove the existing classes from the source list
			Iterator<String> iterator = targetClassNameList.iterator();
			while (iterator.hasNext()) {
				String targetClass = iterator.next();
				for (Class<?> clazz : existingTargetClassList) {
					if (targetClass.equals(clazz.getSimpleName())) {
						iterator.remove();
						break;
					}
				}
			}
		}

		// Generate code using the template.
		srcDirFile.mkdirs();
		writeLine();
		writeLine("Existing class count: " + existingTargetClassList.size());
		writeLine("Existing classes: ");
		int i = 0;
		for (Class<?> clazz : existingTargetClassList) {
			write("   [" + i++ + "] " + clazz.getName());
			if (isOverwrite && targetClassNameList.contains(clazz.getSimpleName())) {
				writeLine(" (Overwritten)");
			} else {
				writeLine();
			}
		}
		writeLine();
		writeLine("Generated class count: " + targetClassNameList.size());
		writeLine("Generated classes:");
		i = 0;
		for (String targetClassName : targetClassNameList) {
			String wrapperClassStr = new String(baseWrapperClassStr);
			Class<?> sourceClass = targetSourceMap.get(targetClassName);
			wrapperClassStr = wrapperClassStr.replaceAll("\\$\\{SOURCE_CLASS\\}", sourceClass.getName());
			wrapperClassStr = wrapperClassStr.replaceAll("\\$\\{TARGET_CLASS\\}", targetClassName);

			// Write to file
			String wrapperFileName = targetPackageName.replaceAll("\\.", "/") + "/" + targetClassName + ".java";
			File wrapperClassFile = new File(srcDirFile, wrapperFileName);
			wrapperClassFile.getParentFile().mkdirs();
			FileWriter writer = null;
			try {
				writer = new FileWriter(wrapperClassFile);
				writer.write(wrapperClassStr);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
			writeLine("   [" + i++ + "] " + targetPackageName + "." + targetClassName);
		}

		writeLine();
		writeLine("Source code directory: ");
		writeLine("   " + srcDirFile.getAbsolutePath());
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
		String executableName = System.getProperty(PROPERTY_executableName, WrapperGenerator.class.getName());
		writeLine();
		writeLine("NAME");
		writeLine(
				"   " + executableName + " - Generate classes that extend the classes found in the specified package");
		writeLine();
		writeLine("SYNOPSIS");
		writeLine("    " + executableName
				+ " -sp source_package -tp target_package -jar jar_path [-dir target_directory] [-overwrite]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine();
		writeLine("   Generates class extensions to the classes found in the specified package.");
		writeLine("   The existing classes are preserved and will not be overwritten.");
		writeLine();
		writeLine("OPTIONS");
		writeLine("   -sp source_package");
		writeLine("             The name of the package where the source classes are located.");
		writeLine();
		writeLine("   -tp target_package");
		writeLine("             The name of the package where the target classes to be generated.");
		writeLine();
		writeLine("   -jar jar_path");
		writeLine("             The path of the jar file that contains the source package.");
		writeLine();
		writeLine("   -dir source_directory");
		writeLine("              Directory path in which to generate the wrapper classes. If this option is not");
		writeLine("              specified then the source directory defaults to '" + DEFAULT_SRC_DIR + "'.");
		writeLine();
		writeLine("   -overwrite");
		writeLine("             If specified, then overwrites the existing classes. If this option is not");
		writeLine("             specified then the exsiting classes are preserved and not overwritten.");
		writeLine();
		writeLine();
		writeLine("DEFAULT");
		writeLine("   " + executableName + " -sp source_package -tp target_package -jar jar_path -dir "
				+ DEFAULT_SRC_DIR);
		writeLine();
		System.exit(0);
	}

	public static void main(String... args) throws Exception {

		String arg;
		String sourcePackageName = null;
		String targetPackageName = null;
		String jarPath = null;
		String srcDir = DEFAULT_SRC_DIR;
		boolean isOverwrite = DEFAULT_IS_OVERWRITE;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			} else if (arg.equals("-sp")) {
				if (i < args.length - 1) {
					sourcePackageName = args[++i].trim();
				}
			} else if (arg.equals("-tp")) {
				if (i < args.length - 1) {
					targetPackageName = args[++i].trim();
				}
			} else if (arg.equals("-jar")) {
				if (i < args.length - 1) {
					jarPath = args[++i].trim();
				}
			} else if (arg.equals("-dir")) {
				if (i < args.length - 1) {
					srcDir = args[++i].trim();
				}
			} else if (arg.equals("-overwrite")) {
				isOverwrite = true;
			}
		}

		if (sourcePackageName == null) {
			writeLine("ERROR: The source package name must be specified. Please use the '-sp' option to specify the");
			writeLine("       source package name. Command aborted.");
			System.exit(1);
		} else if (targetPackageName == null) {
			writeLine("ERROR: The target package name must be specified. Please use the '-tp' option to specify the");
			writeLine("       target package name. Command aborted.");
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

		WrapperGenerator generator = new WrapperGenerator(sourcePackageName, targetPackageName, jarPath, srcDir,
				isOverwrite);
		generator.generateWrappers();
	}
}
