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
package padogrid.mqtt.client.cluster;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import padogrid.mqtt.client.cluster.config.ClusterConfig;
import padogrid.mqtt.client.cluster.config.ClusterConfig.Cluster;
import padogrid.mqtt.client.cluster.config.ClusterConfig.Plugin;

/**
 * {@linkplain ClusterService} is a singleton class for managing the broker
 * discovery service running on a dedicate thread.
 * 
 * @author dpark
 *
 */
public class ClusterService {

	private static ClusterService clusterService;

	private final String args[];

	private final static Logger logger = LogManager.getLogger(ClusterService.class);

	private ClusterConfig clusterConfig = new ClusterConfig();

	private String tag;
	private boolean isServiceEnabled;
	private int initialDelayInMsec = 0;
	private int delayInMsec;
	private String defaultClusterName;

	// <pluginName, IHaMqttPlugin>
	private ConcurrentHashMap<String, IHaMqttPlugin> hapluginMap = new ConcurrentHashMap<String, IHaMqttPlugin>(5, 1f);

	private ScheduledExecutorService ses;

	// <clusterName, ClusterState>
	private ConcurrentHashMap<String, ClusterState> clusterStateMap = new ConcurrentHashMap<String, ClusterState>();

	// <pluginName, ClusterConfig.Plugin>
	private ConcurrentHashMap<String, Plugin> pluginMap = new ConcurrentHashMap<String, Plugin>();

	private volatile boolean isStarted = false;

	/**
	 * Returns the singleton instance of ClusterService. The
	 * {@link #initialize(Properties)} method must be invoked once prior to invoking
	 * this method. Otherwise, it will return null.
	 */
	static ClusterService getClusterService() {
		return clusterService;
	}

	private ClusterService(String... args) {
		this.args = args;
	}

	/**
	 * Initializes and starts the ClusterService with the default cluster when
	 * invoked for the first time. Subsequent invocations have no effect.
	 * 
	 * @param isStart true to start the service. If false, then the {@link #start()}
	 *                method must be invoked to start the service.
	 * @return ClusterService instance
	 * @throws IOException
	 */
	static synchronized ClusterService initialize(boolean isStart, String... args) throws IOException {
		return initialize((ClusterConfig) null, isStart, args);
	}

	/**
	 * Initializes and starts the ClusterService when invoked for the first time.
	 * Subsequent invocations have no effect.
	 * 
	 * @param clusterConfig Cluster configuration. If null, then the configuration
	 *                      file (yaml) defined by the system property,
	 *                      {@linkplain IClusterConfig#PROPERTY_CLIENT_CONFIG_FILE},
	 *                      is read. If the system property is not defined, then
	 *                      {@linkplain IClusterConfig#DEFAULT_CLIENT_CONFIG_FILE}
	 *                      in the class path is read. If all fails, then the
	 *                      default settings are applied.
	 * @param isStart       true to start the service. If false, then the
	 *                      {@link #start()} method must be invoked to start the
	 *                      service.
	 * 
	 * @return ClusterService instance
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	static synchronized ClusterService initialize(ClusterConfig clusterConfig, boolean isStart, String... args)
			throws IOException {
		if (clusterService == null) {
			if (clusterConfig == null) {
				String configFile = System.getProperty(IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE);
				if (configFile != null && configFile.length() > 0) {
					File file = new File(configFile);
					logger.info(String.format("Configuring ClusterService with system property %s... [%s]", IClusterConfig.PROPERTY_CLIENT_CONFIG_FILE, file.toString()));
					Yaml yaml = new Yaml(new Constructor(ClusterConfig.class));
					yaml.setBeanAccess(BeanAccess.FIELD);
					FileReader reader = new FileReader(file);
					clusterConfig = yaml.load(reader);
				} else {
					logger.info(String.format("Configuring ClusterService with default configuration... [%s]", IClusterConfig.DEFAULT_CLIENT_CONFIG_FILE));
					InputStream inputStream = ClusterService.class.getClassLoader()
							.getResourceAsStream(IClusterConfig.DEFAULT_CLIENT_CONFIG_FILE);
					if (inputStream != null) {
						Yaml yaml = new Yaml(new Constructor(ClusterConfig.class));
						yaml.setBeanAccess(BeanAccess.FIELD);
						clusterConfig = yaml.load(inputStream);
					}
				}
			}

			clusterService = new ClusterService(args);
			clusterService.init(clusterConfig);
			if (isStart) {
				clusterService.start();
			}
		}
		return clusterService;
	}

	/**
	 * Initializes and starts the ClusterService when invoked for the first time.
	 * Subsequent invocations have no effect.
	 * 
	 * @param configFile Cluster configuration file. If null, then the configuration
	 *                   file (yaml) defined by the system property,
	 *                   {@linkplain IClusterConfig#PROPERTY_CLIENT_CONFIG_FILE}, is
	 *                   read. If the system property is not defined, then
	 *                   {@linkplain IClusterConfig#DEFAULT_CLIENT_CONFIG_FILE} in
	 *                   the class path is read. If all fails, then the default
	 *                   settings are applied.
	 * @param isStart    true to start the service. If false, then the
	 *                   {@link #start()} method must be invoked to start the
	 *                   service.
	 * 
	 * @return ClusterService instance
	 * @throws IOException Thrown if unable to read the configuration source.
	 */
	static synchronized ClusterService initialize(File configFile, boolean isStart, String... args) throws IOException {
		if (clusterService == null) {
			ClusterConfig clusterConfig = null;
			if (configFile != null) {
				logger.info(String.format("Configuring ClusterService with specified configuration file... [%s]", configFile.toString()));
				Yaml yaml = new Yaml(new Constructor(ClusterConfig.class));
				yaml.setBeanAccess(BeanAccess.FIELD);
				FileReader reader = new FileReader(configFile);
				clusterConfig = yaml.load(reader);
			}
			clusterService = initialize(clusterConfig, isStart, args);
		}
		return clusterService;
	}

	/**
	 * Initializes ClusterService when invoked for the first time.
	 * 
	 * @param clusterConfig Cluster configuration
	 */
	private synchronized void init(ClusterConfig clusterConfig) throws IOException {

		if (clusterConfig == null) {
			clusterConfig = this.clusterConfig;
		} else {
			this.clusterConfig = clusterConfig;
		}
		this.tag = clusterConfig.getTag();
		this.isServiceEnabled = clusterConfig.isEnabled();
		this.delayInMsec = clusterConfig.getProbeDelay();
		this.defaultClusterName = clusterConfig.getDefaultCluster();

		Plugin[] plugins = clusterConfig.getPlugins();
		if (plugins != null) {
			for (Plugin plugin : plugins) {
				String pluginName = plugin.getName();
				pluginMap.put(pluginName, plugin);
				if (plugin.getName() != null && plugin.isEnabled()) {
					IHaMqttPlugin haplugin = createPlugin(plugin.getName(), plugin.getContext());
					if (haplugin != null) {
						boolean isKeep = haplugin.prelude(plugin.getName(), plugin.getDescription(), plugin.getProps(),
								args);
						if (isKeep) {
							hapluginMap.put(plugin.getName(), haplugin);
						}
					}

				}
			}
		}

		// Initialize clusters
		ClusterConfig.Cluster[] clusters = clusterConfig.getClusters();
		if (clusters != null) {
			for (ClusterConfig.Cluster cluster : clusters) {
				if (cluster.isEnabled()) {
					String clusterName = cluster.getName();
					if (clusterName == null || clusterName.length() == 0) {
						clusterName = IClusterConfig.DEFAULT_CLUSTER_NAME;
					}
				}

				// Create HaMqttClient. Connect only if autoConnect is enabled.
				try {
					HaMqttClient client = HaClusters.getOrCreateHaMqttClient(cluster);
					if (cluster.isAutoConnect()) {
						client.connect();
					}
				} catch (MqttException | IOException e) {
					// ignore
				}
			}
		}

		// Build pub/sub bridge clusters for all ClusterState instances
		buildBridgeClusters();

		// Initialize all APP plugins by invoking their init() methods.
		// Note that CLUSTER plugin prelude() methods are invoked during the cluster
		// initialization step above.
		if (plugins != null) {
			for (Plugin plugin : plugins) {
				if (plugin.getName() != null && plugin.isEnabled() && plugin.getContext() == PluginContext.APP) {
					IHaMqttPlugin haplugin = hapluginMap.get(plugin.getName());
					if (haplugin != null) {
						boolean isKeep = haplugin.init(plugin.getName(), plugin.getDescription(), plugin.getProps(),
								args);
						if (isKeep) {
							Thread pluginThread = addPluginThread(plugin.getName(), haplugin);
							pluginThread.start();
						} else {
							hapluginMap.remove(plugin.getName());
						}
					}
				}
			}
		}

		logger.info(String.format("initialized [isServiceEnabled=%s, delayInMsec=%s]", isServiceEnabled, delayInMsec));
	}

	/**
	 * Builds both incoming and outgoing bridge clusters for all ClusterState
	 * instances.
	 */
	private void buildBridgeClusters() {
		Cluster[] clusters = clusterConfig.getClusters();
		for (Cluster cluster : clusters) {
			String clusterName = cluster.getName();
			ClusterState state = clusterStateMap.get(clusterName);
			if (state != null) {
				state.buildBridgeClusters(cluster);
			}
		}
	}

	/**
	 * Returns a new MqttClientPersistence instance to be used for the clusters that
	 * have not defined persistence. It returns a new MqttClientPersistence instance
	 * if it is defined and can be created. Otherwise, it returns null. If it
	 * returns null, then the default persistence, i.e., MqttDefaultFilePersistence
	 * should be used.
	 * <p>
	 * If it fails to create a new instance then it logs a warning message and
	 * returns null.
	 */
	public MqttClientPersistence createMqttClientPersistence() {
		MqttClientPersistence persistence = null;
		if (clusterConfig.getPersistence() != null) {
			try {
				persistence = clusterConfig.getPersistence().createMqttClientPersistence();
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.warn(String.format(
						"Exception raised while creating MqttClientPersistence [%s]. Proceeding with the default persistence instead.",
						e.getMessage()));
			}
		}
		return persistence;
	}

	ClusterState addHaClient(HaMqttClient haclient, ClusterConfig.Cluster clusterConfig,
			MqttClientPersistence persistence, ScheduledExecutorService executorService) {
		ClusterState state = null;
		if (haclient != null) {
			state = clusterStateMap.get(haclient.getClusterName());
			if (state == null) {
				state = new ClusterState(haclient, clusterConfig, persistence, executorService);
				clusterStateMap.put(haclient.getClusterName(), state);
			}
		}
		return state;
	}

	/**
	 * Creates and returns the specified plugin if the plugin is enabled.
	 * 
	 * @param pluginName Plugin name
	 * @param context    Plugin context. Returns null if the named plugin is not for
	 *                   the specified context.
	 * @return null if undefined or unable to create. If unable to create, then it
	 *         logs an error message.
	 */
	private IHaMqttPlugin createPlugin(String pluginName, PluginContext context) {
		IHaMqttPlugin haplugin = null;
		try {
			Plugin p = pluginMap.get(pluginName);
			if (p != null && p.isEnabled() && p.getContext() == context) {
				haplugin = createHaMqttConnector(p);
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error(String.format(
					"Exception raised while creating IHaMqttConnector. [connectorName=%s] %s This plugin is discarded.",
					pluginName, e.getMessage()), e);
		}

		return haplugin;
	}

	/**
	 * Returns the specified plugin that has already been created.
	 * 
	 * @param pluginName Plugin name
	 * @return null if the specified plugin is not found
	 */
	public IHaMqttPlugin getClusterPlugin(String pluginName) {
		if (pluginName == null) {
			return null;
		}
		return hapluginMap.get(pluginName);
	}

	/**
	 * Invokes the specified plugin's init() method and returns the plugin. If the
	 * init() method returns null, then it in turn returns null.
	 * 
	 * @param pluginName Plugin name
	 * @return null if the plugin is not found or its init() method returned null
	 */
	public IHaMqttPlugin initClusterPlugin(String pluginName) {
		IHaMqttPlugin haplugin = hapluginMap.get(pluginName);
		if (haplugin != null) {
			Plugin plugin = pluginMap.get(pluginName);
			boolean isKeep = haplugin.init(pluginName, plugin.getDescription(), plugin.getProps(), args);
			if (isKeep == false) {
				haplugin = null;
			}
		}
		return haplugin;
	}

	/**
	 * Returns a new instance of {@link #getClassName()} with the
	 * {@linkplain IHaMqttPlugin#init(String, Properties)} method invoked. It
	 * returns null if the class name is undefined, i.e., null.
	 * 
	 * @param plugin Plugin configuration
	 * 
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private IHaMqttPlugin createHaMqttConnector(Plugin plugin)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		IHaMqttPlugin haplugin = null;
		String className = plugin.getClassName();
		if (className != null) {
			Class<?> clazz = Class.forName(className);
			if (IHaMqttPlugin.class.isAssignableFrom(IHaMqttPlugin.class) == false) {
				throw new InstantiationException(
						String.format("Invalid class type. Connector classes must implement %s: [%s]",
								IHaMqttPlugin.class.getSimpleName(), className));
			}
			java.lang.reflect.Constructor<?> constructor = clazz.getConstructor(null);
			haplugin = (IHaMqttPlugin) constructor.newInstance();
		}
		return haplugin;
	}

	/**
	 * Closes and removes the specified client. Once removed, the client is no
	 * longer operational.
	 * 
	 * @param haclient Client to remove
	 * @param force    true to forcibly close the client
	 */
	void removeHaClient(HaMqttClient haclient, boolean force) {
		if (haclient == null) {
			return;
		}
		ClusterState state = clusterStateMap.remove(haclient.getClusterName());
		if (state != null) {
			state.close(force);
		}
	}

	/**
	 * Starts the service when invoked for the first time. Subsequent invocations
	 * have no effect. It starts only if {@link #isServiceEnabled()} is true. This
	 * method must be invoked to activate the service.
	 */
	public synchronized void start() {
		if (isServiceEnabled && isStarted == false) {
			if (ses != null) {
				if (ses.isShutdown()) {
					return;
				}
			}
			ses = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r, ClusterService.class.getSimpleName());
					thread.setDaemon(true);
					return thread;
				}
			});

			// Periodic probing entry point
			ses.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					for (Map.Entry<String, ClusterState> entry : clusterStateMap.entrySet()) {
						entry.getValue().reviveDeadEndpoints();
					}
				}
			}, initialDelayInMsec, delayInMsec, TimeUnit.MILLISECONDS);

			isStarted = true;
			logger.info(String.format("ClusterService started: %s", this));
		}
	}

	/**
	 * Stops the service. Once stopped, the service is no longer operational and
	 * usable.
	 */
	public synchronized void stop() {
		if (ses != null) {
			close();
			ses.shutdown();
			logger.info("ClusterService stopped. No longer operational.");
		}
	}

	/**
	 * Connects all clusters. Normally, the application connects each cluster
	 * individually. This is a convenience method that connects all clusters without
	 * having the application to connect each cluster individually.
	 */
	public synchronized void connect() {
		for (ClusterState clusterState : clusterStateMap.values()) {
			clusterState.connect();
		}
	}

	/**
	 * Adds a new plugin thread to the plugin thread group
	 * 
	 * @param pluginName Plugin name
	 * @param haplugin   Plugin instance
	 * @return Thread instance
	 */
	Thread addPluginThread(String pluginName, IHaMqttPlugin haplugin) {
		Thread thread = new Thread(haplugin, pluginName);
		thread.setDaemon(true);
		return thread;
	}
	
	/**
	 * Returns the specified plugin configuration.
	 * @param pluginName Plugin name
	 */
	public Plugin getPluginConfig(String pluginName) {
		return pluginMap.get(pluginName);
	}

	/**
	 * Disconnects all {@linkplain ClusterState} objects.
	 */
	public synchronized void disconnect() {
		for (ClusterState clusterState : clusterStateMap.values()) {
			clusterState.disconnect();
		}
	}

	/**
	 * Closes the cluster service by disconnecting and closing all
	 * {@linkplain ClusterState} objects.
	 */
	public synchronized void close() {
		// Stop app plugins
		for (IHaMqttPlugin haplugin : hapluginMap.values()) {
			haplugin.stop();
		}

		// Close clusters
		for (ClusterState clusterState : clusterStateMap.values()) {
			clusterState.disconnect();
			clusterState.close(false);
		}
	}

	/**
	 * Returns the default cluster name. The default cluster name is configurable.
	 * If not configured, then it returns
	 * {@link IClusterConfig#DEFAULT_CLUSTER_NAME}.
	 */
	public String getDefaultClusterName() {
		return this.defaultClusterName;
	}

	/**
	 * Returns true if the discovery service is enabled. Disabled discovery service
	 * means no Broker probing is conducted in the dedicated thread.
	 */
	public boolean isServiceEnabled() {
		return this.isServiceEnabled;
	}

	/**
	 * Returns the service delay in msec between probes.
	 */
	public int getServiceDelayInMsec() {
		return delayInMsec;
	}

	public ClusterState getClusterState(HaMqttClient haclient) {
		if (haclient == null) {
			return null;
		}
		return clusterStateMap.get(haclient.getClusterName());
	}

	/**
	 * Returns all ClusterState instances.
	 * 
	 * @return Non-null array
	 */
	public ClusterState[] getClusterStates() {
		return clusterStateMap.values().toArray(new ClusterState[clusterStateMap.size()]);
	}

	/**
	 * Returns true if the service has been started, i.e., the {@link #start} method
	 * has been invoked.
	 */
	public boolean isStarted() {
		return isStarted;
	}

	/**
	 * Returns true if the thread has been terminated.
	 */
	public boolean isTerminated() {
		if (ses == null) {
			return true;
		} else {
			return ses.isTerminated();
		}
	}

	@Override
	public String toString() {
		return "ClusterService [isServiceEnabled=" + isServiceEnabled + ", delayInMsec=" + delayInMsec + ", tag=" + tag
				+ ", isStarted=" + isStarted + "]";
	}
}
