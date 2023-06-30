package padogrid.mqtt.test.client.cluster.junit.plugins;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.eclipse.paho.mqttv5.client.MqttClient;

import padogrid.mqtt.client.cluster.HaMqttClient;
import padogrid.mqtt.client.cluster.IHaMqttConnectorPublisher;
import padogrid.mqtt.client.cluster.IHaMqttConnectorSubscriber;

public class TestConnector implements IHaMqttConnectorPublisher, IHaMqttConnectorSubscriber {

	HaMqttClient haclient;
	protected String pluginName;
	protected String endpoint;

	public TestConnector() {

	}
	
	/**
	 * Plugins are launched in by a daemon thread. This method will stop when the
	 * main thread stops.
	 */
	@Override
	public void run() {
		int count = 0;
		while (count < 3) {
			try {
				System.out.printf("TestConnector.run():[count=%d]%n", ++count);
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
		endpoint = props.getProperty("endpoint", "localhost:9009");
		System.out.printf("TestConnector.prelude(): [pluginName=%s, description=%s, endpoint=%s]%n", this.pluginName, description, endpoint);
		return true;
	}

	@Override
	public boolean init(String pluginName, String description, Properties props, String...args) {
		endpoint = props.getProperty("endpoint", "localhost:9009");
		System.out.printf("TestConnector.init(): [pluginName=%s, description=%s, endpoint=%s]%n", this.pluginName, description, endpoint);
		return true;
	}

	@Override
	public void start(HaMqttClient haclient) {
		this.haclient = haclient;
		System.out.printf("TestConnector.start()%n");
	}

	private void printMessage(String topic, byte[] payload) {
		System.out.printf("TestConnector.parseMessage(): [thread=%s, topic=%s, payload=%s]%n", Thread.currentThread(), topic, new String(payload, StandardCharsets.UTF_8));
	}

	@Override
	public void stop() {
		System.out.printf("TestConnector.stop()%n");
	}

	@Override
	public byte[] beforeMessagePublished(MqttClient[] clients, String topic, byte[] payload) {
		System.out.printf("TestConnector.beforeMessagePublished(): [thread=%s, topic=%s, payload=%s]%n", Thread.currentThread(), topic, new String(payload, StandardCharsets.UTF_8));
		printMessage(topic, payload);
		return payload;
	}

	@Override
	public void afterMessagePublished(MqttClient[] clients, String topic, byte[] payload) {
		System.out.printf("TestConnector.afterMessagePublished(): [thread=%s, topic=%s, payload=%s]%n", Thread.currentThread(), topic, new String(payload, StandardCharsets.UTF_8));
	}

	@Override
	public void messageArrived(MqttClient client, String topic, byte[] payload) {
		System.out.printf("TestConnector.messageArrived(): [thread=%s, topic=%s, payload=%s]%n", Thread.currentThread(), topic, new String(payload, StandardCharsets.UTF_8));
	}

}
