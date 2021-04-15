package com.example.testaware.offlineAuth;


import com.github.cliftonlabs.json_simple.Jsonable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;

import lombok.SneakyThrows;


public class AuthenticatedUser implements Jsonable {

    public String ipAddress;
    public String pubKey;

    public AuthenticatedUser(){

    }
    public AuthenticatedUser(String ipAddress, String pubKey){
        this.ipAddress=ipAddress;
        this.pubKey=pubKey;
    }


    public String getIpAddress() {
        return ipAddress;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setIpAddress(String ipAddress){
        this.ipAddress=ipAddress;
    }

    public void setPubKey(String pubKey){
        this.pubKey=pubKey;
    }



    @Override
    public String toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("ip", this.ipAddress);
            obj.put("key", this.pubKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    @Override
    public void toJson(Writer writable) {
        try {
            writable.write(this.toJson());
        } catch (Exception ignored) {
        }
    }
}
