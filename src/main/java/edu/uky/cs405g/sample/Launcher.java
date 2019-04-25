package edu.uky.cs405g.sample;

import com.google.gson.Gson;
import edu.uky.cs405g.sample.database.DBEngine;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;


public class Launcher {

    public static Gson gson;
    public static DBEngine dbEngine;

    public static void main(String[] args) throws IOException {

        gson = new Gson();

        //Database Client initialization
        String DBuser = "smsb222";
        String DBpassword = "9MyPassword01";
        //for your laptop DBhost = "localhost", for your VM instance DBhost = [your account].netlab.uky.edu
        String DBhost = "localhost";
        String DBname = "proj";

        //This is a test comment! - Kyra - master

        System.out.println("Starting Database...");
        dbEngine = new DBEngine(DBhost, DBname, DBuser, DBpassword);

        //Empty DB init, uncomment this is you are creating a new database.
        //code in the dbInit() function shows how to create a table
        //dbInit();

        //Embedded HTTP initialization
        startServer();

        try {
            while (true) {
                Thread.sleep(5000);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private static void startServer() throws IOException {

        final ResourceConfig rc = new ResourceConfig()
                .packages("edu.uky.cs405g.sample.httpcontrollers");

        System.out.println("Starting Web Server...");
        URI BASE_URI = UriBuilder.fromUri("http://0.0.0.0/").port(9990).build();
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);

        try {
            httpServer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dbInit() {

        //drop users table if exist
        dbEngine.dropTable("location");

        //create users table
        String createUsersTable = "create table location(lid varchar(100), address varchar(100) not null, primary key (lid))";
        dbEngine.executeUpdate(createUsersTable);

    }

}