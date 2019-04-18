package edu.uky.cs405g.sample.httpcontrollers;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.uky.cs405g.sample.Launcher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

@Path("/api")
public class API {

    private Type mapType;
    private Gson gson;

    public API() {
        mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        gson = new Gson();
    }


    //curl http://localhost:9998/api/check
    //{"status_code":1}
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {

        String responseString = "{\"status_code\":0}";
        try {

            //Here is where you would put your system test, but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1}";

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }


    //curl http://localhost:9998/api/listlocations
    //{"779a038b-aacc-44ca-b8cc-99671475061f":"800 Rose St.","1e4494a9-5677-49e4-b59f-b77c7900c73f":"123 Campus Road"}
    @GET
    @Path("/listlocations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTeams() {
        String responseString = "{}";
        try {
            Map<String,String> teamMap = Launcher.dbEngine.getLocations();

            responseString = Launcher.gson.toJson(teamMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //curl http://localhost:9998/api/getlocation/800%20Rose%20St.
    //{"address":"800 Rose St.","lid":"c078b038-8ad2-4f45-adf0-03a22fffa8b9"}
    @GET
    @Path("/getlocation/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTeam(@PathParam("address") String address) {
        String responseString = "{}";
        try {

            Map<String,String> teamMap = Launcher.dbEngine.getLocation(address);

            responseString = Launcher.gson.toJson(teamMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //curl http://localhost:9998/api/removelocation/ff2f86ba-ea87-4f5d-8d39-4bdd20b7a532
    //{"status":"1"}
    @GET
    @Path("/removelocation/{location_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLocation(@PathParam("location_id") String locationId) {
        String responseString = "{}";
        try {


            String queryString = "delete from location WHERE lid='" + locationId + "'";

            System.out.println(queryString);

            int status = Launcher.dbEngine.executeUpdate(queryString);

            System.out.println("status: " + status);

            responseString = "{\"status\":\"" + status +"\"}";


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //curl -d '{"address":"800 Rose St."}' -H "Content-Type: application/json" -X POST http://localhost:9998/api/addlocation
    //{"address":"800 Rose St.","lid":"ff2f86ba-ea87-4f5d-8d39-4bdd20b7a532"}
    @POST
    @Path("/addlocation")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crunchifyREST11(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        String returnString = null;
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String address = myMap.get("address");

            Map<String,String> addressMap = Launcher.dbEngine.getLocation(address);

            if(addressMap.size() == 0) {

                //generate a new unique location Id
                String locationId = UUID.randomUUID().toString();

                String createUsersTable = "insert into location values ('" + locationId + "','" + address  + "')";

                System.out.println(createUsersTable);

                Launcher.dbEngine.executeUpdate(createUsersTable);

                addressMap = Launcher.dbEngine.getLocation(address);

                returnString = gson.toJson(addressMap);


            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't insert duplicate location address!")
                        .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Path("/addprovider")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addProvider(InputStream incomingData) {

        StringBuilder addProvider = new StringBuilder();
        String returnString = null;
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                addProvider.append(line);
            }

            String jsonString = addProvider.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String department_id = myMap.get("department_id");
            String npi = myMap.get("npi");

            Map<String,String> departmentMap = Launcher.dbEngine.getProvider(department_id);

            if(departmentMap.size() == 0) {

                String createUsersTable = "insert into provider values ('" + npi + "','" + department_id  + "')";

                System.out.println(createUsersTable);

                int status = Launcher.dbEngine.executeUpdate(createUsersTable);

                returnString = "{\"status\":\"" + status +"\"}\n";


            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't insert duplicate provider department!")
                        .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getprovider/{npi}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProvider(@PathParam("npi") String npi) {
        String responseString = "{}";
        try {

            Map<String,String> teamMap = Launcher.dbEngine.getProviderNPI(npi);

            responseString = Launcher.gson.toJson(teamMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

}
