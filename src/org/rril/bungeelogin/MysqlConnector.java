package org.rril.bungeelogin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mysql connector for bungeelogin plugin
 * 
 * @author Stakzz
 * @version 0.9.0
 */
public class MysqlConnector {

	/** Current connection */
	private Connection connection;

	/**
	 * Default constructor for new MysqlConnector
	 * 
	 * @param host
	 * @param port
	 * @param database
	 * @param user
	 * @param password
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public MysqlConnector(String host, int port, String database, String user, String password) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

		this.connection = DriverManager.getConnection(url, user, password);
                /*this.connection.createStatement().executeQuery("CREATE TABLE IF NOT EXISTS `users` (\n" +
"  `id` int(11) NOT NULL,\n" +
"  `username` varchar(1024) COLLATE utf16_unicode_ci NOT NULL,\n" +
"  `password` varchar(64) COLLATE utf16_unicode_ci NOT NULL,\n" +
"  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
"  `block` tinyint(1) NOT NULL DEFAULT '0'\n" +
") ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf16 COLLATE=utf16_unicode_ci;"
+ "ALTER TABLE `users`\n" +
"  ADD PRIMARY KEY (`id`);\n");*/
	}

	/**
	 * Query executor
	 * 
	 * @param query
	 * @return result of query
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String query) throws SQLException {
		return this.connection.createStatement().executeQuery(query);
	}

	/**
	 * Update executor
	 * 
	 * @param query
	 * @return result Number of modified lines
	 * @throws SQLException
	 */
	public int executeUpdate(String query) throws SQLException {
		return this.connection.createStatement().executeUpdate(query);
	}

	/**
	 * Close database connection
	 * 
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException {
		if (this.connection != null)
			this.connection.close();
	}
}
