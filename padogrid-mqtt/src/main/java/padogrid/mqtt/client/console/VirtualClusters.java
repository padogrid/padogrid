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

package padogrid.mqtt.client.console;

import java.io.File;

import padogrid.mqtt.client.cluster.HaClusters;

/**
 * {@linkplain VirtualClusters} starts virtual clusters defined in the specified
 * configuration file.
 * 
 * @author dpark
 *
 */
public class VirtualClusters implements Constants {

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
		String executable = System.getProperty(PROPERTY_executableName, VirtualClusters.class.getName());
		writeLine();
		writeLine("NAME");
		writeLine("   " + executable + " - Start MQTT virtual clusters");
		writeLine();
		writeLine("SNOPSIS");
		writeLine("   " + executable + " -config config_file [-log log_file] [-?]");
		writeLine();
		writeLine("DESCRIPTION");
		writeLine("   Starts virtual clusters defined by the sepcified configuration file. This");
		writeLine("   command is useful for starting plugin-controlled and/or bridged virtual clusters.");
		writeLine("   A plugin-controlled virtual cluster applies application logic to data and a bridged");
		writeLine("   virtual cluster moves data from cluster to cluster.");
		writeLine();
		writeLine("   The log files are generate in the following user home directory:");
		writeLine();
		writeLine("      ~/.padogrid/log");
		writeLine();
		writeLine("   -config config_file");
		writeLine("             Configuration file.");
		writeLine();
		writeLine("   -log log_file");
		writeLine("             Optional log file.");
		writeLine("             Default: ~/.padogrid/log/" + PROPERTY_executableName + ".log");
	}

	public static void main(String... args) {
		String configFilePath = null;
		String arg;
		boolean isHelp = false;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
				// Defer exit in case the plugins also provides usages.
				isHelp = true;
			} else if (arg.equals("-config")) {
				if (i < args.length - 1) {
					configFilePath = args[++i].trim();
				}
			}
		}

		// Validate inputs
		if (isHelp == false && configFilePath == null) {
			System.err.printf("ERROR: -config not specified. Command aborted.%n");
			System.exit(2);
		}

		// Start virtual clusters
		try {
			// Connect
			HaClusters.initialize(new File(configFilePath), args);
			if (isHelp) {
				System.exit(0);
			}
			HaClusters.connect();

			// Register a shutdown hook thread to gracefully shutdown
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					HaClusters.stop();
					writeLine("Virtual clusters stopped.");
				}
			});

			writeLine("VirtualClusters started: [" + configFilePath + "].");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf(
					"ERROR: Exception occurred while creating the virtual cluster: [file=%s]. Command aborted.%n",
					configFilePath);
			System.exit(-1);
		}
	}
}
