package com.example.alsadmin.object;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Management {

    private String username;
    private String password;
    private String role;
    private String registerDateTime;
    private String loggedInDateTime;
    private String loggedOutDateTime;

    public Management(){
        //
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRegisterDateTime() {
        return registerDateTime;
    }

    public void setRegisterDateTime(String registerDateTime) {
        this.registerDateTime = registerDateTime;
    }

    public String getLoggedInDateTime() {
        return loggedInDateTime;
    }

    public void setLoggedInDateTime(String loggedInDateTime) {
        this.loggedInDateTime = loggedInDateTime;
    }

    public String getLoggedOutDateTime() {
        return loggedOutDateTime;
    }

    public void setLoggedOutDateTime(String loggedOutDateTime) {
        this.loggedOutDateTime = loggedOutDateTime;
    }

    @Exclude
    public Map<String, Object> managementMap(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("username", username);
        result.put("password", password);
        result.put("registerDateTime", registerDateTime);
        result.put("loggedInDateTime", loggedInDateTime);
        result.put("loggedOutDateTime", loggedOutDateTime);
        result.put("role", role);
        return result;
    }
}
