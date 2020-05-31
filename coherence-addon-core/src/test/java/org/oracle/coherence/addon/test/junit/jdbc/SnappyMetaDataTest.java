package org.oracle.coherence.addon.test.junit.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.coherence.addon.kafka.debezium.DebeziumKafkaSinkConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SnappyMetaDataTest {

	private static Connection conn;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			Class.forName(DebeziumKafkaSinkConnector.DEFAULT_CONNECTION_DRIVER_CLASS);
			conn = DriverManager.getConnection(DebeziumKafkaSinkConnector.DEFAULT_CONNECTION_URL, "app", "app");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		conn.close();
	}

	@Test
	public void testMetaData() throws SQLException {
		DatabaseMetaData dbMetaData = conn.getMetaData();
		ResultSet columns = dbMetaData.getColumns(null, "inventory", "customers", null);
		while (columns.next()) {
			String columnName = columns.getString("COLUMN_NAME");
		    int dataType = columns.getInt("DATA_TYPE");
		    String typeName = columns.getString("TYPE_NAME");
		    System.out.println(columnName + ": int type" + dataType + ", " + typeName);
		}
	}

}
