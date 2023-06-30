package padogrid.mqtt.client.cluster;

import java.util.Properties;

/**
 * {@link IHaMqttPlugin} provides the entry point to virtual cluster plugins.
 * The plugins come in three flavors: {@linkplain IHaMqttPlugin},
 * {@linkplain IHaMqttConnectorPublisher},
 * {@linkplain IHaMqttConnectorSubscriber}.
 * <ul>
 * <li>{@linkplain IHaMqttPlugin} - Application plugin. Starts during the
 * application startup time and has no association with virtual clusters.</li>
 * <li>{@linkplain IHaMqttConnectorPublisher} - Publisher connector. Launches
 * during cluster initialization. Responsible for intercepting publishing or
 * published messages by the application.</li>
 * <li>{@linkplain IHaMqttConnectorSubscriber} - Subscriber connector. Luanches
 * during cluster initialization. Responsible for intercepting subscribed
 * messages.</li>
 * </ul>
 * 
 * @see IHaMqttConnectorPublisher
 * @see IHaMqttConnectorSubscriber
 * @author dpark
 *
 */
public interface IHaMqttPlugin extends Runnable {

	/**
	 * Invoked just before the cluster service is initialized. This method could be
	 * used for validating the plugin environment before proceeding to initialize
	 * the plugin. If it returns false, then the plugin is discarded and logged
	 * accordingly.
	 * 
	 * @param pluginName
	 * @param description
	 * @param props
	 * @param args
	 * 
	 * @return false to discard the plugin, true to keep the plugin
	 */
	default boolean prelude(String pluginName, String description, Properties props, String... args) {
		return true;
	}
	
	/**
	 * Initializes the plugin. This method is invoked after the cluster service has
	 * fully been initialized. For connectors, this method is invoked after
	 * {@link HaMqttClient} has fully been initialized. If it returns false, then
	 * the plugin is discarded and logged accordingly.
	 * 
	 * @param pluginName  Unique plugin name
	 * @param description Plugin description. If undefined then null.
	 * @param props       Properties for initializing the connector
	 * @param args        Optional command line arguments.
	 * 
	 * @return false to discard the plugin, true to keep the plugin
	 * 
	 * @see ClusterService#initialize(boolean, String...)
	 * @see ClusterService#initialize(padogrid.mqtt.client.cluster.config.ClusterConfig,
	 *      boolean, String...)
	 * @see ClusterService#initialize(java.io.File, boolean, String...)
	 */
	default boolean init(String pluginName, String description, Properties props, String... args) {
		return true;
	}
	
	default void run() {
	}

	/**
	 * Invoked when this plugin {@link HaMqttClient} terminates. It is the plugin's
	 * responsibility to clean up itself in this method.
	 */
	void stop();
}
