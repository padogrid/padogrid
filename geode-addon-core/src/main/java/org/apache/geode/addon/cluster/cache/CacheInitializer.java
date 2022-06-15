/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
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
package org.apache.geode.addon.cluster.cache;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.util.CacheListenerAdapter;

/**
 * CacheInitializer is a general purpose utility class for passing
 * initialization information to the application during the cache startup
 * time. It allows the application to register classes that implement
 * CacheInitializable in the cache.xml file. It instantiates the registered
 * classes and invokes CacheInitializable.postInit() after a delay period
 * specified by the <code>"initDelay"</code> parameter. For example, the
 * following configures the add-on CacheInitializer component, which get invoked
 * after 5000 milliseconds. The default delay is 20,000 milliseconds (or 20
 * seconds).
 * <p>
 * 
 * <pre>
 *    &lt;region name="init"&gt;
      &lt;region-attributes data-policy="empty"&gt;
         &lt;cache-listener&gt;
            &lt;class-name&gt; 
               org.apache.geode.addon.cluster.cache.CacheInitializer 
            &lt;/class-name&gt;
            &lt;parameter name="initDelay"&gt;
               &lt;string&gt;5000&lt;/string&gt;
            &lt;/parameter&gt;
            &lt;parameter name="cacheserver"&gt;
               &lt;declarable&gt;
                  &lt;class-name&gt;org.apache.geode.addon.cluster.cache.CacheServerInitializer&lt;/class-name&gt;
                  &lt;parameter name="system.property.prefix"&gt;
                     &lt;string&gt;cacheserver&lt;/string&gt;
                  &lt;/parameter&gt;
               &lt;/declarable&gt;
            &lt;/parameter&gt;
         &lt;/cache-listener&gt;
      &lt;/region-attributes&gt;
   &lt;/region&gt;
 * </pre>
 * 
 * @author dpark
 * @implNote This class has been ported from Pado. Support for multirouter has
 *           been removed.
 *
 */
public class CacheInitializer extends CacheListenerAdapter implements Declarable {

	private long getLong(Properties properties, String property, long defaultValue) {
		try {
			return Long.parseLong(properties.getProperty(property));
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public void init(final Properties props) {
		long initDelay = getLong(props, "initDelay", 20000);
		final ArrayList<CacheInitializable> cacheInitializableList = new ArrayList();

		Set propSet = props.entrySet();
		for (Object object : propSet) {
			Map.Entry entry = (Map.Entry) object;
			String key = (String) entry.getKey();
			Object obj = entry.getValue();
			if (obj instanceof CacheInitializable) {
				CacheInitializable initializable = (CacheInitializable) obj;
				cacheInitializableList.add(initializable);
			}
		}

		new Timer("Pado-CacheInitializer", true).schedule(new TimerTask() {
			public void run() {
				for (CacheInitializable initializer : cacheInitializableList) {
					initializer.postInit();
				}
			}
		}, initDelay);
	}
}
