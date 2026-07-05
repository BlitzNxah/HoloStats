package de.bemmeutils.holostats.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.bemmeutils.holostats.Addon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileUtil {
    public static void checkCreateFile(String path, List<String> jsonArrays) {
        File file = new File(path);
        if (file.exists()) {
            return;
        } else {
            try {
                if (file.getParentFile() != null && !file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (file.createNewFile()) {
                    JsonObject root = new JsonObject();
                    for (String jsonArray : jsonArrays) {
                        root.add(jsonArray, new JsonArray());
                    }
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                        writer.write(gson.toJson(root));
                        return;
                    } catch (IOException exception) {
                        exception.printStackTrace();
                        return;
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                return;
            }
        }
        return;
    }

    public static void setupAddonFiles(List<String> jsonArrays) {
        checkCreateFile(Addon.getADDON_FILE_PATH(), jsonArrays);
    }
}

