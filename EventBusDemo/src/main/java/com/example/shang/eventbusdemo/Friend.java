package com.example.shang.eventbusdemo;

/**
 * Created by Shang on 2017/7/11.
 */
public class Friend {

    private String name;
    private String password;

    public Friend(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
