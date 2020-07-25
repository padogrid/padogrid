package org.snappydata.addon.kafka.debezium;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionThread.class);

	private String connectionUrl;
	private Properties props;
	private Connection conn;
	private int numConnectionTries = Integer.MAX_VALUE;
	private long connectionIntervalInMsec = 10000;

	public ConnectionThread(String connectionUrl, Properties props) {
		this.connectionUrl = connectionUrl;
		this.props = props;
	}

	@Override
	public void run() {
		boolean isConnected = false;
		int count = 0;
		while (isConnected == false && count < numConnectionTries) {
			try {
				conn = DriverManager.getConnection(connectionUrl, props);
				isConnected = true;
			} catch (SQLException e) {
				count++;
				if (numConnectionTries < Integer.MAX_VALUE) {
					logger.error("[" + count + "/" + numConnectionTries +"] Connection failure. Trying again in " + connectionIntervalInMsec + " msec.", e);
				} else {
					logger.error("[" + count + "] Connection failure. Trying again in " + connectionIntervalInMsec + " msec.", e);
				}
			}
		}
	}

}
