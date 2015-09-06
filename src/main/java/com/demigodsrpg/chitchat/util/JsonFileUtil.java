package com.demigodsrpg.chitchat.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class JsonFileUtil {
    private final File FOLDER;
    private final boolean PRETTY;

    public JsonFileUtil(File folder, boolean pretty) {
        FOLDER = folder;
        PRETTY = pretty;
    }

    private void createFile(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (Exception oops) {
            oops.printStackTrace();
        }
    }

    public void removeFile(String key) {
        File file = new File(FOLDER.getPath() + "/" + key + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public void saveToFile(String key, Map data) {
        if (data != null) {
            File file = new File(FOLDER.getPath() + "/" + key + ".json");
            if (!(file.exists())) {
                createFile(file);
            }
            Gson gson = PRETTY ? new GsonBuilder().setPrettyPrinting().create() : new GsonBuilder().create();
            String json = gson.toJson(data);
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.print(json);
                writer.close();
            } catch (Exception oops) {
                oops.printStackTrace();
            }
        }
    }

    public Map loadFromFile(String key) {
        Gson gson = new GsonBuilder().create();
        try {
            File file = new File(FOLDER.getPath() + "/" + key + ".json");
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(inputStream);
                Map value = gson.fromJson(reader, Map.class);
                reader.close();
                return value;
            }
        } catch (Exception oops) {
            oops.printStackTrace();
        }
        return new HashMap<>();
    }
}
