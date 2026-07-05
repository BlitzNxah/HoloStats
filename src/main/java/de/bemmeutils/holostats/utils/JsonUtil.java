package de.bemmeutils.holostats.utils;

import com.google.gson.*;
import de.bemmeutils.holostats.Addon;
import de.bemmeutils.holostats.api.Hologram;
import de.bemmeutils.holostats.api.Jackpot;
import de.bemmeutils.holostats.api.Wager;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JsonUtil {
    @Getter
    private JsonObject jsonObject;

    public JsonUtil(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public static JsonObject readJsonFromFile(String filePath) throws FileNotFoundException, JsonParseException {
        Scanner scanner = new Scanner(new File(filePath));
        StringBuilder jsonContent = new StringBuilder();
        while (scanner.hasNextLine()) {
            jsonContent.append(scanner.nextLine());
        }
        scanner.close();
        return new JsonParser().parse(jsonContent.toString()).getAsJsonObject();
    }

    public static JsonObject parseJson(String jsonString) throws JsonParseException {
        return new JsonParser().parse(jsonString).getAsJsonObject();
    }

    /**
     * Get a jackpot by its value
     *
     * @param value The money value of the jackpot.
     * @return An instance of the jackpot with the given value. If no jackpot is found, null is returned.
     */
    public Jackpot getJackpot(double value) {
        if (this.jsonObject.has("jackpots")) {
            JsonArray jackpots = this.jsonObject.getAsJsonArray("jackpots");
            for (int i = 0; i < jackpots.size(); i++) {
                JsonObject jackpotObject = jackpots.get(i).getAsJsonObject();
                if (jackpotObject.has("price") && jackpotObject.get("price").getAsDouble() == value) {
                    return new Jackpot(jackpotObject.get("price").getAsInt(), jackpotObject.get("times_purchased").getAsInt(), jackpotObject.get("last_username").getAsString());
                }
            }
        }
        return null;
    }

    /**
     * Get all jackpots from the JSON file.
     *
     * @return A list of all jackpots. If no jackpots exist, an empty list is returned.
     */
    public List<Jackpot> getAllJackpots() {
        List<Jackpot> jackpotList = new ArrayList<>();
        if (this.jsonObject.has("jackpots")) {
            JsonArray jackpots = this.jsonObject.getAsJsonArray("jackpots");
            for (int i = 0; i < jackpots.size(); i++) {
                JsonObject jackpotObject = jackpots.get(i).getAsJsonObject();
                if (jackpotObject.has("price")) {
                    jackpotList.add(new Jackpot(
                            jackpotObject.get("price").getAsInt(),
                            jackpotObject.get("times_purchased").getAsInt(),
                            jackpotObject.get("last_username").getAsString()
                    ));
                }
            }
        }
        return jackpotList;
    }

    /**
     * Add a jackpot to the JSON file.
     *
     * @param value The money value of the jackpot.
     */
    public void addJackpot(double value) {
        if (!this.jsonObject.has("jackpots")) {
            this.jsonObject.add("jackpots", new JsonArray());
        }
        JsonArray jackpots = this.jsonObject.getAsJsonArray("jackpots");
        JsonObject jackpotObject = new JsonObject();
        jackpotObject.addProperty("price", value);
        jackpotObject.addProperty("times_purchased", 0);
        jackpotObject.addProperty("last_username", "");
        jackpots.add(jackpotObject);
        saveJsonToFile();
    }

    /**
     * Saves an updated jackpot to the JSON file.
     *
     * @param value          The money value of the jackpot.
     * @param timesPurchased The number of times the jackpot has been purchased by the bot.
     * @param lastUsername   The last username that bought the jackpot. Doesn't need to be an actual username.
     */
    public void saveJackpot(double value, int timesPurchased, String lastUsername) {
        if (!this.jsonObject.has("jackpots")) {
            this.jsonObject.add("jackpots", new JsonArray());
        }
        JsonArray jackpots = this.jsonObject.getAsJsonArray("jackpots");
        for (int i = 0; i < jackpots.size(); i++) {
            JsonObject jackpotObject = jackpots.get(i).getAsJsonObject();
            if (jackpotObject.get("price").getAsDouble() == value) {
                jackpotObject.addProperty("times_purchased", timesPurchased);
                jackpotObject.addProperty("last_username", lastUsername);
                break;
            }
        }
        saveJsonToFile();
    }

    /**
     * Saves an updated jackpot to the JSON file.
     *
     * @param jackpot An instance of the jackpot to save.
     */
    public void saveJackpot(Jackpot jackpot) {
        if (!this.jsonObject.has("jackpots")) {
            this.jsonObject.add("jackpots", new JsonArray());
        }
        JsonArray jackpots = this.jsonObject.getAsJsonArray("jackpots");
        for (int i = 0; i < jackpots.size(); i++) {
            JsonObject jackpotObject = jackpots.get(i).getAsJsonObject();
            if (jackpotObject.get("price").getAsDouble() == jackpot.getPrice()) {
                jackpotObject.addProperty("times_purchased", jackpot.getTimesPurchased());
                jackpotObject.addProperty("last_username", jackpot.getLastUsername());
                break;
            }
        }
        saveJsonToFile();
    }

    /**
     * Removes a jackpot from the JSON file.
     *
     * @param value The money value of the jackpot.
     */
    public void removeJackpot(double value) {
        if (this.jsonObject.has("jackpots")) {
            JsonArray jackpots = jsonObject.getAsJsonArray("jackpots");
            JsonArray updatedJackpots = new JsonArray();
            for (int i = 0; i < jackpots.size(); i++) {
                JsonObject jackpotObject = jackpots.get(i).getAsJsonObject();
                if (jackpotObject.get("price").getAsInt() != value) {
                    updatedJackpots.add(jackpotObject);
                }
            }
            jsonObject.add("jackpots", updatedJackpots);
            saveJsonToFile();
        }
    }

    /**
     * Get a wager of a player the player's name
     *
     * @param uuid The uuid of the player
     * @return An object of the wager. If no wager is found, null is returned.
     */
    public Wager getWager(String uuid) {
        if (this.jsonObject.has("wagers")) {
            JsonArray wagers = this.jsonObject.getAsJsonArray("wagers");
            for (int i = 0; i < wagers.size(); i++) {
                JsonObject wagerObject = wagers.get(i).getAsJsonObject();
                if (wagerObject.has("uuid") && wagerObject.get("uuid").getAsString().equals(uuid)) {
                    return new Wager(wagerObject.get("uuid").getAsString(), wagerObject.get("player_name").getAsString(), wagerObject.get("wager").getAsDouble());
                }
            }
        }
        return null;
    }

    /**
     * Add a wager to the JSON file.
     *
     * @param playerUuid The player's uuid.
     * @param playerName The player's name.
     * @param wager      The amount of money the player wagers.
     */
    public void addWager(String playerUuid, String playerName, double wager) {
        if (!this.jsonObject.has("wagers")) {
            this.jsonObject.add("wagers", new JsonArray());
        }
        JsonArray wagers = this.jsonObject.getAsJsonArray("wagers");
        JsonObject wagerObject = new JsonObject();
        wagerObject.addProperty("uuid", playerUuid);
        wagerObject.addProperty("player_name", playerName);
        wagerObject.addProperty("wager", wager);
        wagers.add(wagerObject);
        saveJsonToFile();
    }

    /**
     * Saves an updated wager to the JSON file.
     *
     * @param wager An instance of the wager to save.
     */
    public void saveWager(Wager wager) {
        if (!this.jsonObject.has("wagers")) {
            this.jsonObject.add("wagers", new JsonArray());
        }
        JsonArray wagers = this.jsonObject.getAsJsonArray("wagers");
        for (int i = 0; i < wagers.size(); i++) {
            JsonObject wagerObject = wagers.get(i).getAsJsonObject();
            if (wagerObject.get("uuid").getAsString().equalsIgnoreCase(wager.getUuid())) {
                wagerObject.addProperty("player_name", wager.getPlayerName());
                wagerObject.addProperty("wager", wager.getWager());
                break;
            }
        }
        saveJsonToFile();
    }

    /**
     * Removes a wager entry from the JSON file.
     *
     * @param uuid The uuid of the player.
     */
    public void removeWager(String uuid) {
        if (this.jsonObject.has("wagers")) {
            JsonArray wagers = jsonObject.getAsJsonArray("wagers");
            JsonArray updatedWagers = new JsonArray();
            for (int i = 0; i < wagers.size(); i++) {
                JsonObject wagerObject = wagers.get(i).getAsJsonObject();
                if (!wagerObject.get("uuid").getAsString().equalsIgnoreCase(uuid)) {
                    updatedWagers.add(wagerObject);
                }
            }
            jsonObject.add("wagers", updatedWagers);
            saveJsonToFile();
        }
    }

    /**
     * Create a holo and save it to the JSON file.
     *
     * @param name         Name of the holo. Holo name is a number.
     * @param hologramType The type of the holo.
     */
    public void addHolo(int name, Hologram.HologramType hologramType) {
        if (!this.jsonObject.has("holos")) {
            this.jsonObject.add("holos", new JsonArray());
        }
        JsonArray holos = this.jsonObject.getAsJsonArray("holos");
        JsonObject holoObject = new JsonObject();
        holoObject.addProperty("name", name);
        holoObject.addProperty("type", hologramType.name());
        holoObject.add("lines", new JsonArray());
        holos.add(holoObject);
        saveJsonToFile();
    }

    /**
     * Get a holo by its name
     *
     * @param name Name of the holo. Holo name is a number.
     * @return an instance of the holo. If no holo is found, null is returned.
     */
    public Hologram getHologram(int name) {
        if (this.jsonObject.has("holos")) {
            JsonArray holos = this.jsonObject.getAsJsonArray("holos");
            for (int i = 0; i < holos.size(); i++) {
                JsonObject holoObject = holos.get(i).getAsJsonObject();
                if (holoObject.has("name") && holoObject.get("name").getAsInt() == name) {
                    Hologram hologram = new Hologram(
                            holoObject.get("name").getAsInt(),
                            Hologram.HologramType.valueOf(holoObject.get("type").getAsString())
                    );
                    if (holoObject.has("lines") && holoObject.get("lines").isJsonObject()) {
                        JsonObject linesObject = holoObject.getAsJsonObject("lines");
                        Map<Integer, Double> lines = new HashMap<>();
                        for (Map.Entry<String, JsonElement> entry : linesObject.entrySet()) {
                            try {
                                int lineNumber = Integer.parseInt(entry.getKey());
                                double jackpotPrice = entry.getValue().getAsDouble();
                                lines.put(lineNumber, jackpotPrice);
                            } catch (NumberFormatException e) {
                                System.err.println("Ungültige Zeilen-Nummer gefunden: " + entry.getKey());
                            }
                        }
                        hologram.setLines(lines);
                    }
                    return hologram;
                }
            }
        }
        return null;
    }

    /**
     * Get all holograms from the JSON file.
     *
     * @return A list of all holograms. If no holograms exist, an empty list is returned.
     */
    public List<Hologram> getAllHolograms() {
        List<Hologram> hologramList = new ArrayList<>();

        if (this.jsonObject.has("holos")) {
            JsonArray holos = this.jsonObject.getAsJsonArray("holos");
            for (int i = 0; i < holos.size(); i++) {
                JsonObject holoObject = holos.get(i).getAsJsonObject();
                if (holoObject.has("name") && holoObject.has("type")) {
                    Hologram hologram = new Hologram(
                            holoObject.get("name").getAsInt(),
                            Hologram.HologramType.valueOf(holoObject.get("type").getAsString())
                    );
                    if (holoObject.has("lines") && holoObject.get("lines").isJsonObject()) {
                        JsonObject linesObject = holoObject.getAsJsonObject("lines");
                        Map<Integer, Double> lines = new HashMap<>();
                        for (Map.Entry<String, JsonElement> entry : linesObject.entrySet()) {
                            try {
                                int lineNumber = Integer.parseInt(entry.getKey());
                                double jackpotPrice = entry.getValue().getAsDouble();
                                lines.put(lineNumber, jackpotPrice);
                            } catch (NumberFormatException e) {
                                System.err.println("Ungültige Zeilen-Nummer gefunden: " + entry.getKey());
                            }
                        }
                        hologram.setLines(lines);
                    }
                    hologramList.add(hologram);
                }
            }
        }
        return hologramList;
    }

    /**
     * Remove a holo from the JSON file.
     *
     * @param name The name of the holo. Holo name is a number.
     */
    public void removeHolo(int name) {
        if (this.jsonObject.has("holos")) {
            JsonArray holos = jsonObject.getAsJsonArray("holos");
            JsonArray updatedHolos = new JsonArray();
            for (int i = 0; i < holos.size(); i++) {
                JsonObject holoObject = holos.get(i).getAsJsonObject();
                if (holoObject.get("name").getAsInt() != name) {
                    updatedHolos.add(holoObject);
                }
            }
            jsonObject.add("holos", updatedHolos);
            saveJsonToFile();
        }
    }

    /**
     * Saves an updated hologram to the JSON file.
     *
     * @param hologram An instance of the hologram to save.
     */
    public void saveHologram(Hologram hologram) {
        if (!this.jsonObject.has("holos")) {
            this.jsonObject.add("holos", new JsonArray());
        }
        JsonArray holos = this.jsonObject.getAsJsonArray("holos");
        for (int i = 0; i < holos.size(); i++) {
            JsonObject holoObject = holos.get(i).getAsJsonObject();
            if (holoObject.get("name").getAsInt() == hologram.getName()) {
                holoObject.addProperty("type", hologram.getType().name());
                JsonObject linesObject = new JsonObject();
                for (Map.Entry<Integer, Double> entry : hologram.getLines().entrySet()) {
                    linesObject.addProperty(entry.getKey().toString(), entry.getValue());
                }
                holoObject.add("lines", linesObject);
                break;
            }
        }
        saveJsonToFile();
    }

    /**
     * Add a jackpot to a hologram.
     *
     * @param name    The name of the holo. Holo name is a number.
     * @param line    The line to display the jackpot on. Must be between 1 and 3 (inclusive).
     * @param jackpot The jackpot to display.
     * @throws IllegalArgumentException if {@code line} is not between 1 and 3.
     */
    public void addJackpotToHolo(int name, int line, Jackpot jackpot) {
        if (line < 1 || line > 3) throw new IllegalArgumentException("Line must be between 1 and 3");

    }

    private void saveJsonToFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(Addon.getADDON_FILE_PATH())) {
            gson.toJson(this.jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the top 3 wagers sorted by amount (highest to lowest).
     *
     * @return A list of the top 3 wagers. If fewer than 3 wagers exist, all available wagers are returned.
     */
    public List<Wager> getTop3Wagers() {
        List<Wager> allWagers = getAllWagers();

        return allWagers.stream()
                .sorted((w1, w2) -> Double.compare(w2.getWager(), w1.getWager()))
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * Get all wagers from the JSON file.
     *
     * @return A list of all wagers. If no wagers exist, an empty list is returned.
     */
    public List<Wager> getAllWagers() {
        List<Wager> wagerList = new ArrayList<>();
        if (this.jsonObject.has("wagers")) {
            JsonArray wagers = this.jsonObject.getAsJsonArray("wagers");
            for (int i = 0; i < wagers.size(); i++) {
                JsonObject wagerObject = wagers.get(i).getAsJsonObject();
                if (wagerObject.has("uuid") && wagerObject.has("player_name") && wagerObject.has("wager")) {
                    Wager wager = new Wager(
                            wagerObject.get("uuid").getAsString(),
                            wagerObject.get("player_name").getAsString(),
                            wagerObject.get("wager").getAsDouble()
                    );
                    wagerList.add(wager);
                }
            }
        }
        return wagerList;
    }
}
