package myUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DB {
	private static String JDBC_DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static String DB_URL = "jdbc:sqlserver://10.1.211.7;databaseName=bizmobdb";
	//private static String USER_ID = "sa";
	//private static String USER_PASSWORD = "test123";
	private static String USER_ID = "bizmob";
	private static String USER_PASSWORD = "bizmob2013";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        //
    	try{
    		//Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
    		Class.forName(JDBC_DRIVER_NAME);
    		Connection connection = DriverManager.getConnection(DB_URL, USER_ID, USER_PASSWORD);
    		return connection;
    	}
    	catch(Exception e){    		
    		return null;
    	}
        
    }

    /*
     * 
     * 			<property name="username" value="bizmob" />
				<property name="password" value="bizmob2013" />
				<property name="poolMaximumActiveConnections" value="3" />
				<property name="poolMaximumIdleConnections" value="1" />
				<property name="poolPingEnabled" value="true" />
				<property name="poolPingQuery" value="select 1" />
     * */
    
}