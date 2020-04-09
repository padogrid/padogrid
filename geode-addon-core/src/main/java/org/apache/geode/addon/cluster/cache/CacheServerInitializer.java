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

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.geode.LogWriter;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CacheServerInitializer is a convenience utility class that initializes cache
 * servers defined in the cache.xml file. It eliminates the need of creating
 * multiple cache.xml files that are differ only by cache server settings.
 * CacheServerInitializer achieves this by allowing the startup script to pass
 * in the cache server configuration information via system properties.
 * <p>
 * The system properties must begin with the following default prefix excluding
 * the quotes.
 * 
 * <pre>
 * gfinit.cacheserver."
 * </pre>
 * 
 * The prefix <code>"gfnit."</code> is reserved and cannot be changed. The
 * default <code>"cacheserver."</code> prefix can be changed using the paremeter
 * name <code>"system.property.prefix"</code> in the cache.xml file. For
 * example,
 * <p>
 * 
 * <pre>
 *    &lt;region name="__init"&gt;
      &lt;region-attributes data-policy="empty"&gt;
         &lt;cache-listener&gt;
            &lt;class-name&gt; 
               org.apache.geode.addon.cluster.cache.CacheInitializer 
            &lt;/class-name&gt;
            &lt;parameter name="initDelay"&gt;
               &lt;string&gt;5000&lt;/string&gt;
            &lt;/parameter&gt;
            <font color="blue"><b>&lt;parameter name="cacheserver"&gt;
               &lt;declarable&gt;
                  &lt;class-name&gt;org.apache.geode.addon.cluster.cache.CacheServerInitializer&lt;/class-name&gt;
                  &lt;parameter name="system.property.prefix"&gt;
                     &lt;string&gt;cacheserver&lt;/string&gt;
                  &lt;/parameter&gt;
               &lt;/declarable&gt;
            &lt;/parameter&gt;</b></font>
         &lt;/cache-listener&gt;
      &lt;/region-attributes&gt;
   &lt;/region&gt;
 * </pre>
 * 
 * The system properties must be identifiable using numbers as follows:
 * <p>
 * 
 * <pre>
 cacheserver ... -J-Dgfinit.cacheserver.1.port=40401 -J-Dgfinit.cacheserver.1.notify-by-subscription=true -J-Dgfinit.cacheserver.1.groups="foo, yong" \
                 -J-Dgfinit.cacheserver.2.port=40402 -J-Dgfinit.cacheserver.2.notify-by-subscription=true -J-Dgfinit.cacheserver.2.groups="foo2, yong2" ...
 * </pre>
 * 
 * The above example defines two cache servers. Each cache server must be
 * uniquely enumerated. Server groups must be in double quotes and comma
 * separated. That means CacheServerInitializer does not support server group
 * names with commas. There is no requirement for having the numbers in
 * sequential order. The only requirement is each cache server must have a
 * unique number.
 * 
 * @author dpark
 * @implNote This class has been ported from Pado.
 *
 */
public class CacheServerInitializer implements CacheInitializable {
	private Properties initProps = new Properties();

	public void init(Properties props) {
		this.initProps = props;
	}

	public void postInit() {
		initCacheServer();
	}

	private void initCacheServer() {
		String prefix = initProps.getProperty("system.property.prefix", "cacheserver");
		prefix = "gfinit." + prefix;

		// cacheserver.1.port
		// cacheserver.1.bind-address
		// cacheserver.2.port
		// cacheserver.2.bind-address
		// cacheserver.3.port
		// cacheserver.3.bind-address

		Cache cache = CacheFactory.getAnyInstance();
		Logger logger = LogManager.getLogger(this.getClass());
		TreeSet keyNumSet = new TreeSet();
		Properties sysProps = System.getProperties();

		// Extract all valid key numbers form the system properties
		Set keySet = sysProps.keySet();
		for (Object object : keySet) {
			String key = (String) object;
			if (key.startsWith(prefix + ".")) {
				String split[] = key.split("\\.");
				if (split.length < 4) {
					logger.error("Invalid cache server initializer system property: " + key);
				} else {
					try {
						int keyNum = Integer.parseInt(split[2]);
						keyNumSet.add(keyNum);
					} catch (Exception ex) {
						logger.error("Invalid cache server initializer system property: " + key
								+ ". A number must follow after the prefix.", ex);
					}
				}
			}
		}

		// Add cache servers
//		 <cache-server port="44444" 
//			  bind-address="" hostname-for-clients="" 
//			  load-poll-interval="" max-connections="" 
//			  max-threads="" maximum-message-count="" 
//			  maximum-time-between-pings=""
//			  message-time-to-live="" 
//			  notify-by-subscription="false" 
//			  socket-buffer-size="">

		for (Object object : keyNumSet) {
			org.apache.geode.cache.server.CacheServer server;
			int keyNum = (Integer) object;
			String top = prefix + "." + keyNum + ".";
			int port = Integer.getInteger(top + "port", -1);
			if (port >= 0) {
				server = cache.addCacheServer();
				server.setPort(port);
			} else {
				logger.error("CacheServer port undefined. Check your system property: " + top + "port");
				continue;
			}

			String bindAddress = System.getProperty(top + "bind-address");
			if (bindAddress != null) {
				server.setBindAddress(bindAddress);
			}
			String hostName = System.getProperty(top + "hostname-for-clients");
			if (hostName != null) {
				server.setHostnameForClients(hostName);
			}
			long loadPollInterval = Long.getLong("load-poll-interval", -1);
			if (loadPollInterval >= 0) {
				server.setLoadPollInterval(loadPollInterval);
			}
			int maxCons = Integer.getInteger(top + "max-connections", -1);
			if (maxCons >= 0) {
				server.setMaxConnections(maxCons);
			}
			int maxThreads = Integer.getInteger(top + "max-threads", -1);
			if (maxThreads >= 0) {
				server.setMaxThreads(maxThreads);
			}
			int maxMessageCount = Integer.getInteger(top + "maximum-message", -1);
			if (maxMessageCount >= 0) {
				server.setMaximumMessageCount(maxMessageCount);
			}
			int maximumTimeBetweenPings = Integer.getInteger(top + "maximum-time-between-pings", -1);
			if (maximumTimeBetweenPings >= 0) {
				server.setMaximumTimeBetweenPings(maximumTimeBetweenPings);
			}
			int messageTimeToLive = Integer.getInteger(top + "message-time-to-live", -1);
			if (messageTimeToLive >= 0) {
				server.setMessageTimeToLive(messageTimeToLive);
			}
			String val = System.getProperty(top + "notify-by-subscription");
			if (val != null) {
				boolean notifyBySub = Boolean.getBoolean(top + "notify-by-subscription");
				server.setNotifyBySubscription(notifyBySub);
			}
			int socketBufferSize = Integer.getInteger(top + "socket-buffer-size", -1);
			if (socketBufferSize >= 0) {
				server.setSocketBufferSize(socketBufferSize);
			}

			val = System.getProperty(top + "groups");
			if (val != null) {
				String groups[] = val.split(",");
				for (int i = 0; i < groups.length; i++) {
					groups[i] = groups[i].trim();
				}
				server.setGroups(groups);
			}

			try {
				server.start();
				logger.info("CacheServer started via CacheServerInitializer: " + server);
			} catch (IOException ex) {
				logger.error("Exception while starting CacheServer", ex);
			}
		}
	}
}
