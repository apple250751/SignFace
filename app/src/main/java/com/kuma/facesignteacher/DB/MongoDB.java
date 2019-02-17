package com.kuma.facesignteacher.DB;


import android.util.Log;

import com.kuma.facesignteacher.Global.Global;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Created by kuma on 2017/12/28.
 */

public class MongoDB {

    private static final String DATABASE_NAME = "project";
    private static final String TAG = "MONGODB";
    private MongoClient client;
    private MongoDatabase database;
    private boolean isConnect;

    public MongoDB() {
        this.client = null;
        this.database = null;
        this.isConnect = false;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean connect() {
        if (isActivity()) {
            return false;
        }
        //String uri = "mongodb://" + Config.getMONGO_DB_IP() + ":" + String.valueOf(Config.getMONGO_DB_PORT());
        try {
            //client = new MongoClient(new MongoClientURI(uri));
            client = new MongoClient(Global.getMONGO_DB_IP(), Global.getMONGO_DB_PORT());
            database = client.getDatabase(DATABASE_NAME);
            isConnect = true;
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isActivity() {
        return isConnect || client != null || database != null;
    }

    public boolean close() {
        if (isActivity()) {
            client.close();
            isConnect = false;
            database = null;
            client = null;
            return true;
        } else {
            return false;
        }
    }

    public boolean insertMany(String collectionName, List<Document> document) {
        if (!isActivity()) {
            return false;
        }
        try {
            if (database.getCollection(collectionName) == null)
                database.createCollection(collectionName);
            database.getCollection(collectionName).insertMany(document);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean insertOne(String collectionName, Document document) {
        if (!isActivity()) {
            return false;
        }
        try {
            if (database.getCollection(collectionName) == null)
                database.createCollection(collectionName);
            database.getCollection(collectionName).insertOne(document);
            Log.e(TAG, "insert ok!!");
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateOne(String collectionName, Bson filter, Bson document) {
        if (!isActivity()) {
            return false;
        }
        try {
            database.getCollection(collectionName).updateOne(filter, document);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean updateMany(String collectionName, Bson filter, Bson document) {
        if (!isActivity()) {
            return false;
        }
        try {
            database.getCollection(collectionName).updateMany(filter, filter);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deleteOne(String collectionName, Bson filter) {
        if (!isActivity()) {
            return false;
        }
        try {
            database.getCollection(collectionName).deleteOne(filter);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMany(String collectionName, Bson filter) {
        if (!isActivity()) {
            return false;
        }
        try {
            database.getCollection(collectionName).deleteMany(filter);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean removeCollection(String collectionName) {
        if (!isActivity()) {
            return false;
        }
        try {
            database.getCollection(collectionName).drop();
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    public List<Document> find(String collectionName, Bson fiter) {
        List<Document> data = new ArrayList<>();
        if (!isActivity()) {
            return null;
        }
        try {
            MongoCursor<Document> it = database.getCollection(collectionName).find(fiter).iterator();
            while (it.hasNext()) {
                data.add(it.next());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
        }
        return data;
    }

    public List<Document> getAllData(String collectionName) {
        List<Document> data = new ArrayList<>();
        if (!isActivity()) {
            return null;
        }
        try {
            MongoCursor<Document> it = database.getCollection(collectionName).find().iterator();
            while (it.hasNext()) {
                data.add(it.next());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
        }
        return data;
    }

    public void printCollection(String collectionName) {
        if (!isActivity()) {
            return;
        }
        List<Document> data = getAllData(collectionName);
        for (int i = 0; i < data.size(); i++) {
            System.out.println(data.get(i));
        }
    }
}
