package com.ewallet;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/** Very simple persistence: serializes the user map to a local file
 *  so balances/history survive between runs. Not meant for production use. */
public class DataStore {

    private static final String FILE_NAME = "ewallet_data.ser";
    private static Map<String, User> users = new HashMap<>();

    public static Map<String, User> getUsers() {
        return users;
    }

    public static User findUser(String username) {
        return users.get(username.toLowerCase());
    }

    public static boolean registerUser(String username, String password) {
        String key = username.toLowerCase();
        if (users.containsKey(key)) return false;
        users.put(key, new User(username, password));
        save();
        return true;
    }

    @SuppressWarnings("unchecked")
    public static void load() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (Map<String, User>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Could not load saved data, starting fresh: " + e.getMessage());
                users = new HashMap<>();
            }
        }
        seedAdmin();
    }

    /** Makes sure a default admin account always exists: username "admin", password "123". */
    private static void seedAdmin() {
        if (!users.containsKey("admin")) {
            users.put("admin", new User("admin", "123", true));
        }
    }

    public static void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Could not save data: " + e.getMessage());
        }
    }
}
