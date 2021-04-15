package com.example.testaware.offlineAuth;


import android.util.Log;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class JSONParser {
    private static ArrayList<AuthenticatedUser> userObjList;
    private static AuthenticatedUser userObj;
     private static String LOG = "LOG-Test-Aware-Parser";





    public static void writeUser(String connectedPeerIP, String connectedPeerKey) {
        userObjList = new ArrayList<>();

        try {
            File file = new File("/data/data/com.example.testaware/AuthenticatedUsers.txt");
            if (file.exists()) {
                Reader reader = Files.newBufferedReader(Paths.get(String.valueOf(Paths.get("/data/data/com.example.testaware/AuthenticatedUsers.txt"))));

                JsonObject parser = (JsonObject) Jsoner.deserialize(reader);

                JsonArray users = (JsonArray) parser.get("users");
                users.forEach(entry -> {
                    JsonObject user = (JsonObject) entry;
                    userObj = new AuthenticatedUser(user.get("ip").toString(), user.get("key").toString());
                    userObjList.add(userObj);

                });
                reader.close();
            }
            ArrayList<JsonObject> objects = new ArrayList<>();
            BufferedWriter writer = Files.newBufferedWriter(Paths.get("/data/data/com.example.testaware/AuthenticatedUsers.txt"));

            JsonObject keyIPMapping = new JsonObject();
            keyIPMapping.put("MAP", "mapping");

            JsonArray userArray = new JsonArray();
            if (userObjList.isEmpty()) {  //nothing has been written before
                JsonObject p1 = new JsonObject();
                p1.put("ip", connectedPeerIP);
                p1.put("key", connectedPeerKey);
                userArray.addAll(Arrays.asList(p1));

            } else {
                for (int i = 0; i < userObjList.size(); i++) {
                    JsonObject p1 = new JsonObject();
                    p1.put("ip", connectedPeerIP);
                    p1.put("key", connectedPeerKey);
                    objects.add(p1);

                }
                userArray.addAll(Arrays.asList(objects));
            }

            keyIPMapping.put("users", userArray);
            Jsoner.serialize(keyIPMapping, writer);
            writer.close();

        } catch (IOException | JsonException e) {
            e.printStackTrace();
            Log.i(LOG, "hello"+ e);
        }


    }

    public static ArrayList<AuthenticatedUser> readUser() {
        userObjList = new ArrayList<>();
        try {
            Reader reader = Files.newBufferedReader(Paths.get(String.valueOf(Paths.get("/data/data/com.example.testaware/AuthenticatedUsers.txt"))));

            JsonObject parser = (JsonObject) Jsoner.deserialize(reader);

            JsonArray users = (JsonArray) parser.get("users");
            users.forEach(entry -> {
                JsonObject user = (JsonObject) entry;
                userObj = new AuthenticatedUser(user.get("ip").toString(), user.get("key").toString());
                userObjList.add(userObj);
            });
            reader.close();



        } catch (JsonException e) {
            e.printStackTrace();
            Log.i(LOG, "hello"+ e);    
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(LOG, "hello"+ e);
        }
        return userObjList;

    }


}

