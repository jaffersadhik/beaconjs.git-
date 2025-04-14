package com.itextos.beacon.platform.mysqltabledatadump;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionToFile {

    // Method to save collection to file
    public static void saveCollection(List<Map<String, Object>> data,String filepath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(data);
            System.out.println("Collection saved to disk.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveCollectionForCreate(Map<String,Map<String, String>> data,String filepath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(data);
            System.out.println("Collection saved to disk.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveCollectionForGeneral(Map<String,Map<String, String>> data,String filepath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(data);
            System.out.println("Collection saved to disk.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveCollection(Map<String, String> data,String filepath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(data);
            System.out.println("Collection saved to disk.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load collection from file
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> loadCollection(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("No data file found.");
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            return (List<Map<String, Object>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    
    @SuppressWarnings("unchecked")
    public static Map<String, String> getWalletDetail(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("No data file found. filepath "+filepath);
            return new HashMap<String, String>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            return (Map<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new HashMap<String, String>();
    }

    
    
    @SuppressWarnings("unchecked")
    public static  Map<String,Map<String, String>> getGeneralDetail(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            System.out.println("No data file found. filepath "+filepath);
            return new HashMap<String,Map<String, String>>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            return (Map<String,Map<String, String>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new HashMap<String,Map<String, String>>();
    }
    
    
    public static void main(String[] args) {
        // Sample Data
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", 1);
        row1.put("name", "Alice");
        row1.put("age", 25);
        data.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", 2);
        row2.put("name", "Bob");
        row2.put("age", 30);
        data.add(row2);

        // Save Collection to File
        saveCollection(data,"");

        // Load Collection from File
        List<Map<String, Object>> loadedData = loadCollection("");
        System.out.println("Loaded Data: " + loadedData);
    }
}
