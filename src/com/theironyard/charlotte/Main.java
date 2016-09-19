package com.theironyard.charlotte;


import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Spark;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
//IS MY public/index.html folder in the wrong place? did i need to recreate it or use and update the default one?
    //need to change the addUser function file
    //delete function - Change "stringify" to delete

    public static void createTables(Connection conn) throws SQLException {
//      Connect to the database and create a table with four columns: id, username, address, and email.

        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, username VARCHAR, address VARCHAR, email VARCHAR)");

    }

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.externalStaticFileLocation("public");
        Spark.init();

//        Create a GET route called /user that calls selectUsers and returns the data as JSON.
//        Create a POST route called /user that parses request.body() into a User object and calls insertUser to put it in the database.
//Create a PUT route called /user that parses request.body() into a User object and calls updateUser to update it in the database.
//Create a DELETE route called /user/:id that gets the id via request.params(":id") and gives it to deleteUser to delete it in the database.
        Spark.get(
                "/user",
                ((request, response) -> {
                    ArrayList<User> users = selectUsers(conn);
                    JsonSerializer s = new JsonSerializer();
                    return s.serialize(users);
                })
        );

        Spark.post(
                "/user",
                ((request, response) -> {
                    String body = request.body();
                    JsonParser p = new JsonParser();
                    User user = p.parse(body, User.class);
                    insertUser(conn, user.username, user.email, user.address);
                    return "";
                })
        );

        Spark.post(
                "/user/:id",
                ((request, response) -> {
                    String stringId = request.queryParams("id");

                    int id = Integer.parseInt(stringId);
                    deleteUser(conn, id);

                    response.redirect("/");
                    return "";
                })

        );

        Spark.put(
                "/user",
                ((request, response) -> {
                String body = request.body();
                JsonParser p = new JsonParser();
                User user = p.parse(body, User.class);
                updateUser(conn, user);
                return "";
                })
        );



    }

    public static void insertUser(Connection conn, String username, String email, String address) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?, ?)");
        stmt.setString(1, username);
        stmt.setString(2, email);
        stmt.setString(3, address);
        stmt.execute();
    }

    public static ArrayList<User> selectUsers(Connection conn) throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("id");
            String username = results.getString("username");
            String email = results.getString("email");
            String address = results.getString("address");
            users.add(new User(id, username, email, address));
        }
        return users;
    }

    public static void deleteUser(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void updateUser(Connection conn, User newUser) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE uers set username = ?, email = ?, address = ? where id = ?");
        stmt.setString(1, newUser.getUsername());
        stmt.setString(2, newUser.getEmail());
        stmt.setString(3, newUser.getAddress());
        stmt.setInt(4, newUser.getId());
        stmt.execute();
    }

}