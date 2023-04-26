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

import org.mqtt.addon.test.perf.GroupTest.DataStructureEnum;
import org.mqtt.addon.test.perf.GroupTest.TestCaseEnum;

public interface Constants {

	// Perf properties
	public final String PROPERTY_executableName = "executable.name";
	public final String PROPERTY_resultsDir = "results.dir";
	public final String PROPERTY_memberSetSize = "memberSetSize";
	public final String PROPERTY_mapNames = "mapNames";
	public final String PROPERTY_printStatusIntervalInSec = "printStatusIntervalInSec";
	public final String PROPERTY_prefix = "prefix";
	public final String PROPERTY_totalEntryCount = "totalEntryCount";
	public final String PROPERTY_batchSize = "batchSize";
	public final String PROPERTY_threadCount = "threadCount";
	public final String PROPERTY_testCase = "testCase";
	public final String PROPERTY_testCount = "testCount";
	public final String PROPERTY_testIntervalInMsec = "testIntervalInMsec";

	public final String DEFAULT_resultsDir = "results";
	public final String DEFAULT_ingestionPropertiesFile = "../etc/ingestion.properties";
	public final String DEFAULT_groupPropertiesFile = "../etc/group.properties";
	public final static int DEFAULT_memberSetSize = 10;
	public final static int DEFAULT_testCount = 1;
	public final static int DEFAULT_testIntervalInMsec = 0;
	public final static int DEFAULT_printStatusIntervalInSec = 1000;
	public final static int DEFAULT_threadCount = (int) (Runtime.getRuntime().availableProcessors() * 1.5);
	public final static int DEFAULT_batchSize = 1000;
	public final static int DEFAULT_totalEntryCount = 100000;
	public final static DataStructureEnum DEFAULT_ds = DataStructureEnum.topic;
	public final static TestCaseEnum DEFAULT_testCase = TestCaseEnum.publish;

	// Ingestion specific properties
	public final String PROPERTY_payloadSize = "payloadSize";
	
	public final static int DEFAULT_PAYLOAD_SIZE = 1024;
}
