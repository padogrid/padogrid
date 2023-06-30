package padogrid.mqtt.test.client.cluster.junit.plugins;

import java.util.Properties;

import padogrid.mqtt.client.cluster.HaMqttClient;
import padogrid.mqtt.client.cluster.IHaMqttPlugin;

public class TestAppPlugin implements IHaMqttPlugin {

	HaMqttClient haclient;
	protected String pluginName;
	protected String mykey;

	public TestAppPlugin() {

	}

	/**
	 * Plugins are launched in by a daemon thread. This method will stop when the
	 * main thread stops.
	 */
	@Override
	public void run() {
		int count = 0;
		while (count < 10) {
			try {
				System.out.printf("TestAppPlugin.run():[count=%d]%n", ++count);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean prelude(String pluginName, String description, Properties props, String... args) {
		this.pluginName = pluginName;
		mykey = props.getProperty("mykey", "not found");
		System.out.printf("TestAppPlugin.prelude(): [pluginName=%s, description=%s, mykey=%s]%n", this.pluginName,
				description, mykey);
		return true;
	}

	@Override
	public boolean init(String pluginName, String description, Properties props, String... args) {
		mykey = props.getProperty("mykey", "not found");
		System.out.printf("TestAppPlugin.init(): [pluginName=%s, description=%s, mykey=%s]%n", this.pluginName,
				description, mykey);
		return true;
	}

	@Override
	public void stop() {
		System.out.printf("TestAppPlugin.stop()%n");
	}
}
