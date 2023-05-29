package padogrid.mqtt.test.client.cluster.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttPersistenceException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import padogrid.mqtt.client.cluster.IClusterConfig;

/**
 * MqttClient is not thread safe across multiple endpoints. This test case
 * demonstrates connection failures with two instances of MqttClient. Even
 * though each instance has a different server URI, the first instance gets
 * disconnected by the server.
 * <p>
 * <ul>
 * <li>Client error message: The Server Disconnected the client. Disconnect RC:
 * 130 (32204).</li>
 * <li>Mosquitto log message: Client thread_test disconnected due to protocol
 * error.</li>
 * </ul>
 * <p>
 * This test case requires a cluster with the following endpoints:
 * <ul><li>tcp://loccalhost:1883-1884</li></ul>
 * 
 * @author dpark
 *
 */
public class ThreadTest implements IClusterConfig {
	static MqttClient client;
	static MqttClient client2;
	static int publisherThreadCount = 10;
	static boolean useExecutiveService = false;
	static String protocol = "ws";
	static String serverURI1;
	static String serverURI2;

	@BeforeClass
	public static void setUp() {
		int port1 = 1883;
		int port2 = 1884;
		if (protocol == "ws") {
			port1 = 8083;
			port2 = 8084;
		}
		serverURI1 = protocol + "://localhost:" + port1;
		serverURI2 = protocol + "://localhost:" + port2;

		if (useExecutiveService) {
			try {
				ExecutorService es = Executors.newSingleThreadExecutor();
				Future<MqttClient> future1 = es.submit(new Client(serverURI1, "thread_test"));
				client = future1.get();
			} catch (Exception ex) {
				System.err.println("ERROR: " + serverURI1);
				ex.printStackTrace();
			}
			
			// Second instance. This fails for websocket.
			try {
				ExecutorService es2 = Executors.newSingleThreadExecutor();
				Future<MqttClient> future2 = es2.submit(new Client(serverURI2, "thread_test2"));
				client2 = future2.get();
			} catch (Exception ex) {
				System.err.println("ERROR: " + serverURI2);
				ex.printStackTrace();
			}

		} else {

			try {
				client = new MqttClient(serverURI1, "thread_test");
				client.connect();
				for (int i = 0; i < 1; i++) {
					publishConnectionMessage(client);
				}
			} catch (Exception ex) {
				System.err.println("ERROR: " + serverURI1);
				ex.printStackTrace();
			}

			// Second instance. This fails for websocket.
			try {
				// Works up to 3 publisher threads
				client2 = new MqttClient(serverURI2, "thread_test2");
				client2.connect();
				client2.disconnect();
				client2.close();
			} catch (Exception ex) {
				System.err.println("ERROR: " + serverURI2);
				ex.printStackTrace();
			}
		}
	}

	@Test
	public void testConfig() throws MqttException, InterruptedException, ExecutionException {

		ExecutorService es = Executors.newFixedThreadPool(publisherThreadCount);

		Publisher publisher = new Publisher();
		List<Publisher> list = new ArrayList<Publisher>();
		for (int i = 0; i < publisherThreadCount; i++) {
			list.add(publisher);
		}
		List<Future<Boolean>> futureList = es.invokeAll(list);
		for (Future<Boolean> future : futureList) {
			future.get();
		}
	}

	static private void publishConnectionMessage(MqttClient client) {
		for (int i = 0; i < 1; i++) {
			String message = "Connection message " + i;
			try {
				client.publish("__padogrid/__test", message.getBytes(), 0, false);
				System.out.println(message);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	static class Client implements Callable<MqttClient> {

		String serverURI;
		String clientId;

		Client(String serverURI, String clientId) {
			this.serverURI = serverURI;
			this.clientId = clientId;
		}

		@Override
		public MqttClient call() throws Exception {
			MqttClient client = new MqttClient(serverURI, clientId);
			client.connect();
			for (int i = 0; i < 1; i++) {
				publishConnectionMessage(client);
			}
			return client;
		}

	}

	class Publisher implements Callable<Boolean> {

		@Override
		public Boolean call() throws Exception {
			for (int i = 0; i < 10000000; i++) {
				String message = "Message " + i;
				try {
					client.publish("topic1", message.getBytes(), 0, false);
					System.out.println(message);
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (client.isConnected() == false) {
						try {
							client.connect();
						} catch (MqttException ex) {
							if (ex.getReasonCode() == 0) {
								System.err.println(ex);
								System.out.println("Close and reopening...");
								client.close();
								client = new MqttClient(serverURI1, "thread_test");
								try {
									client.connect();
								} catch (MqttException ex2) {
									System.err.println("Close/open failed: " + ex);
								}
							}

						}
					}
				}
				Thread.sleep(1000);
			}
			return true;
		}

	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (client != null) {
			if (client.isConnected()) {
				client.disconnect();
			}
			client.close();
		}
	}
}
