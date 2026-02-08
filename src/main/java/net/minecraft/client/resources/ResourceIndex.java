package net.minecraft.client.resources;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ResourceIndex {
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, File> resourceMap = new HashMap<>();

    public ResourceIndex(File file, String resource) {
        if (resource != null) {
            File file1 = new File(file, "objects");
            File file2 = new File(file, "indexes/" + resource + ".json");

            try (var bufferedreader = Files.newReader(file2, StandardCharsets.UTF_8)) {
                JsonObject jsonobject = JsonParser.parseReader(bufferedreader).getAsJsonObject();
                JsonObject objects = JsonUtils.getJsonObject(jsonobject, "objects", null);

                if (objects != null) {
                    for (Entry<String, JsonElement> entry : objects.entrySet()) {
                        JsonObject jsonobject2 = (JsonObject) entry.getValue();
                        String s = entry.getKey();
                        String[] astring = s.split("/", 2);
                        String s1 = astring.length == 1 ? astring[0] : astring[0] + ":" + astring[1];
                        String s2 = JsonUtils.getString(jsonobject2, "hash");
                        File file3 = new File(file1, s2.substring(0, 2) + "/" + s2);
                        resourceMap.put(s1, file3);
                    }
                }
            } catch (IOException e) {
                logger.error("Can't find the resource index file: {}", file2, e);
            }
        }
    }

    public Map<String, File> getResourceMap() {
        return resourceMap;
    }
}
