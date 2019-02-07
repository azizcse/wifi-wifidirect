package com.w3.meshlib.data;

import android.content.Context;
import android.text.TextUtils;

import com.w3.meshlib.MeshApp;
import com.w3.meshlib.model.Routing;
import com.w3.meshlib.model.Routing_;
import com.w3.meshlib.model.User;
import com.w3.meshlib.model.User_;

import java.util.List;

import io.objectbox.Box;

public class DBManager {
    private static DBManager dbManager;

    private Box<User> userBox;
    private Box<Routing> routingBox;

    private DBManager() {

        userBox = MeshApp.getBoxStore().boxFor(User.class);
        routingBox = MeshApp.getBoxStore().boxFor(Routing.class);
    }

    public static DBManager on() {
        if (dbManager == null) {
            dbManager = new DBManager();
        }
        return dbManager;
    }

    public long saveUser(User user) {
        User dbUser = userBox.query().equal(User_.userId, user.getUserId()).build().findFirst();
        insertRoutValue(user);
        if (dbUser == null) {
            return userBox.put(user);
        } else {
            dbUser.setUserName(user.getUserName());
            dbUser.setUserId(user.getUserId());
            dbUser.setDeviceName(user.getDeviceName());
            dbUser.setDeviceMac(user.getDeviceMac());
            dbUser.setBleUser(user.isBleUser());
            dbUser.setIpAddress(user.getIpAddress());
            dbUser.setDirectConnection(user.isDirectConnection());
            return userBox.put(dbUser);
        }

    }

    private void insertRoutValue(User user) {
        if(TextUtils.isEmpty(user.getUserId())) return;
        Routing dbRouting = routingBox.query().equal(Routing_.userId, user.getUserId()).build().findFirst();
        Routing rout = new Routing();

        if (dbRouting == null) {
            rout.setUserId(user.getUserId());
            rout.setBleUser(user.isBleUser());
            rout.setIpAddress(user.getIpAddress());
            rout.setMacAddress(user.getDeviceMac());
            routingBox.put(rout);
        } else {
            dbRouting.setUserId(user.getUserId());
            dbRouting.setIpAddress(user.getIpAddress());
            dbRouting.setMacAddress(user.getDeviceMac());
            dbRouting.setBleUser(user.isBleUser());
            routingBox.put(dbRouting);
        }
    }

    public List<User> getAllUsers() {
        return userBox.getAll();
    }

    public String getMedium(String userId){
        Routing routing = routingBox.query().equal(Routing_.userId, userId).build().findFirst();
        if(routing != null) {
            return routing.getMedium();
        }
        return null;
    }

    public Routing getRouting(String userId){
        return routingBox.query().equal(Routing_.userId, userId).build().findFirst();
    }


}
