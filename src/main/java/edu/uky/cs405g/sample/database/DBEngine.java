package edu.uky.cs405g.sample.database;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.Console;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DBEngine {

    private DataSource ds;

    public boolean isInit = false;

    public DBEngine(String host, String database, String login, String password) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            String dbConnectionString = null;


            if(database == null) {

                dbConnectionString ="jdbc:mysql://" + host + "?" +
                        "user=" + login  +"&password=" + password + "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            } else {

                dbConnectionString ="jdbc:mysql://" + host + "/" + database  + "?" +
                        "user=" + login  +"&password=" + password + "&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            }

            ds = setupDataSource(dbConnectionString);


            isInit = true;


        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static DataSource setupDataSource(String connectURI) {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = null;
            connectionFactory = new DriverManagerConnectionFactory(connectURI, null);


        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<>(connectionPool);

        return dataSource;
    }



    public Map<String,String> getLocations() {
        Map<String,String> teamIdMap = new HashMap<>();

        Statement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;

            queryString = "SELECT * FROM location";

            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(queryString);

            while (rs.next()) {
                String teamId = rs.getString("lid");
                String teamName = rs.getString("address");

                teamIdMap.put(teamId, teamName);
            }

            rs.close();
            stmt.close();
            conn.close();

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return teamIdMap;
    }

    public Map<String,String> getLocation(String address) {
        Map<String,String> locationMap = new HashMap<>();

        Statement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;

            queryString = "SELECT * FROM location WHERE address='" + address + "'";

            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(queryString);

            while (rs.next()) {
                String lid = rs.getString("lid");
                locationMap.put("lid", lid);
                locationMap.put("address", address);
            }

            rs.close();
            stmt.close();
            conn.close();

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return locationMap;
    }

    public int executeUpdate(String stmtString) {
        int result = -1;
        try {
            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            result = stmt.executeUpdate(stmtString);
            stmt.close();
            conn.close();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return  result;
    }

    public int dropTable(String tableName) {
        int result = -1;
        try {
            Connection conn = ds.getConnection();
            String stmtString = null;

            stmtString = "DROP TABLE " + tableName;

            Statement stmt = conn.createStatement();

            result = stmt.executeUpdate(stmtString);

            stmt.close();
            conn.close();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }


    //utility functions

    public boolean databaseExist(String databaseName)  {
        boolean exist = false;

        Statement stmt = null;
        ResultSet rs = null;

        try {

            Connection conn = ds.getConnection();
            String queryString = null;

            queryString = "SELECT COUNT(1) FROM INFORMATION_SCHEMA.SCHEMATA " +
                    "WHERE SCHEMA_NAME  = N'" + databaseName + "'";

            stmt = conn.createStatement();

            rs = stmt.executeQuery(queryString);
            rs.next();
            exist = rs.getBoolean(1);

            rs.close();
            stmt.close();
            conn.close();

            //todo likely better way to do this hack to let derby work
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return exist;
    }

    public List<String> getDatabaseNames()  {

        List<String> databaseNames = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ds.getConnection();
            databaseNames = new ArrayList<>();

            String queryString = null;

            queryString = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA";

            stmt = conn.createStatement();

            rs = stmt.executeQuery(queryString);

            while (rs.next()) {
                databaseNames.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
            conn.close();

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        return databaseNames;

    }

    public List<String> getTableNames(String database)  {

        List<String> tableNames = null;

        Statement stmt = null;
        ResultSet rs = null;

        try {

            Connection conn = ds.getConnection();
            tableNames = new ArrayList<>();
            String queryString = null;

            queryString = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";

            stmt = conn.createStatement();

            rs = stmt.executeQuery(queryString);

            while (rs.next()) {
                tableNames.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return tableNames;
    }

    public List<String> getColumnNames(String database, String table)  {

        List<String> columnNames = null;

        Statement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ds.getConnection();
            columnNames = new ArrayList<>();
            String queryString = null;

            queryString = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='" + database + "' AND TABLE_NAME='" + table + "'";

            stmt = conn.createStatement();

            rs = stmt.executeQuery(queryString);

            while (rs.next()) {
                columnNames.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
            conn.close();

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return columnNames;
    }

    public boolean tableExist(String tableName)  {
        boolean exist = false;

        Statement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = ds.getConnection();
            String queryString = null;

            queryString = "SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_NAME = N'" + tableName + "'";

            stmt = conn.createStatement();

            rs = stmt.executeQuery(queryString);
            rs.next();
            exist = rs.getBoolean(1);

            rs.close();
            stmt.close();
            conn.close();


            //todo likely better way to do this hack to let derby work
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return exist;
    }



}
