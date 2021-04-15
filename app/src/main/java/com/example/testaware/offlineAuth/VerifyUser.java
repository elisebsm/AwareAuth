package com.example.testaware.offlineAuth;


import android.util.Log;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;



/*Methods used to verify user that server wants to connect to - mappings of pub key and mac/Ip- after done challenge response*/

public class VerifyUser {

    private static String LOG = "LOG-Test-Aware-Verify-User";

    private static AuthenticatedUser parsedUser;
    private static AuthenticatedUser currentUser;
    private static ArrayList<AuthenticatedUser> userList;

    public static void setAuthenticatedUser(String connectedPeerIP, String connectedPeerKey) {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/AuthenticatedUsers.txt", true));
            String mapping = connectedPeerIP + "split"+ connectedPeerKey;
            writer.write(mapping);
            Log.i(LOG, "Successfully wrote key and ip to the file.");

            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isAuthenticatedUser(String peerIP, String peerKey) {
        boolean isAuthenticated = false;

        String ip;
        String key;
        String thisLine = null;

        try {
            File file = new File("/data/data/com.example.testaware/AuthenticatedUsers.txt");
            Scanner sc = new Scanner(file);
            while (sc.hasNext()) {
                thisLine = sc.nextLine();
                String[] splitLine = thisLine.split("split");
                ip = splitLine[0];
                key = splitLine[1];
                if (ip == peerIP) {
                    if (key == peerKey) {
                        isAuthenticated = true;
                        Log.i(LOG, "Successfully read key and ip to the file.");
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return isAuthenticated;
    }


}


    /*

        public static boolean isAuthenticatedUser(String peerIP, String key){
            boolean cont= true;
            boolean isAuthenticated=false;
            AuthenticatedUser user;
            try {
                FileInputStream fileIn = new FileInputStream("/data/data/com.example.testaware/authenticatedUsers.txt");
                while (cont) {
                    try {
                        ObjectInputStream in = new ObjectInputStream(fileIn);
                        arrayList = (ArrayList<AuthenticatedUser>) in.readObject();
                        if (arrayList != null) {
                            for (int i=0; i< arrayList.size(); i++) {
                                user= arrayList.get(i);
                                if (user.getIpAddress().equals(peerIP) && user.getPubKey().equals(key)) {
                                    isAuthenticated = true;
                                    in.close();
                                    fileIn.close();
                                    Log.i(LOG, "Successfully read key and ip to the file.");
                                    cont = false;
                                }
                            }
                        } else {
                            in.close();
                            fileIn.close();
                            System.out.println("No users authenticated in file");
                            Log.i(LOG, "No users authenticated in file");
                            cont=false;
                        }
                        cont=false;
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("An error occurred. ");
                        e.printStackTrace();

                    }
                }
            }
            catch (IOException i) {
                System.out.println("An error occurred. ");
                i.printStackTrace();

            }
            return isAuthenticated;
        }


        public static void setAuthenticatedUser( String connectedPeerIP,String connectedPeerKey) {
            String thisLine=null;

            try{
                File file = new File("/data/data/com.example.testaware/authenticatedUsers.txt");
                if(!file.exists()){
                    arrayList = new ArrayList<>();
                }
                else {
                    BufferedReader myReader = new BufferedReader(new FileReader("/data/data/com.example.testaware/authenticateUsers.txt"));
                    while ((thisLine = myReader.readLine()) != null) {
                        thisLine= arrayList.toArray(user);
                    }
                    myReader.close();
                }
                arrayList.add(new AuthenticatedUser(connectedPeerIP,connectedPeerKey));
                BufferedWriter myWriter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/authenticatedUsers.txt",true));
                myWriter.write(String.valueOf(arrayList));
                myWriter.close();
                Log.i(LOG, "Successfully wrote key and ipaddr to the file.");



            } catch (IOException ) {
                System.out.println("An error occurred.");
                i.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    */


