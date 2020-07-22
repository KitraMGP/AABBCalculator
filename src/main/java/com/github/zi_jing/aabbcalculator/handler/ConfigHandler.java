package com.github.zi_jing.aabbcalculator.handler;

import com.github.zi_jing.aabbcalculator.Main;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

// TODO
public class ConfigHandler {
    public static final File CONFIG_FILE = new File("config.json");

    public static String language;

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            Main.firstStart = true;
            language = "en_us";
        }
    }

    public static void saveConfig() {
        JsonObject root = new JsonObject();
        root.addProperty("language", language);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileUtils.writeStringToFile(CONFIG_FILE, gson.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("ERROR: Can't read config file!");
            e.printStackTrace();
        }
    }
}
