package net.alpaca.capturepods.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PartySettings {

    private final Map<UUID, Boolean> partyCaptureSettings;
    private final Map<UUID, Boolean> partyReleaseSettings;

    private final File jsonFile;
    private final Gson gson;

    // Scheduler for periodic saves
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private void ensurePartyExists(UUID partyId) {
        partyCaptureSettings.putIfAbsent(partyId, false);   // default: allow capture
        partyReleaseSettings.putIfAbsent(partyId, false);  // default: deny release
    }

    public PartySettings(File directory ) {
        partyCaptureSettings = new HashMap<>();
        partyReleaseSettings = new HashMap<>();

        gson = new GsonBuilder().setPrettyPrinting().create();

        jsonFile = new File(directory, "partySettings.json");
        System.out.println("=============================== PartySettings created ==============================");
        System.out.println("directory: " + directory.getAbsolutePath());
        System.out.println("jsonFile: " + jsonFile.getAbsolutePath());
        loadFromFile();
        startPeriodicSave();
    }

    public boolean isCaptureAllowed(UUID partyID) {
        ensurePartyExists(partyID);
        return partyCaptureSettings.get(partyID);
    }

    public boolean isReleaseAllowed(UUID partyID) {
        ensurePartyExists(partyID);
        return partyReleaseSettings.get(partyID);
    }

    public void setCaptureAllowed(UUID partyID, boolean allowed) {
        ensurePartyExists(partyID);
        partyCaptureSettings.put(partyID, allowed);
    }

    public void setReleaseAllowed(UUID partyID, boolean allowed) {
        ensurePartyExists(partyID);
        partyReleaseSettings.put(partyID, allowed);
    }

    public void removeParty(UUID partyId) {
        partyCaptureSettings.remove(partyId);
        partyReleaseSettings.remove(partyId);
    }


    /**
     * Save both maps to JSON file.
     */
    public synchronized boolean saveToFile() {
        Map<String, Map<String, Boolean>> toSave = new HashMap<>();

        // Convert UUID keys to strings for JSON
        Map<String, Boolean> capture = new HashMap<>();
        for (Map.Entry<UUID, Boolean> entry : partyCaptureSettings.entrySet()) {
            capture.put(entry.getKey().toString(), entry.getValue());
            System.err.println("capture: " + capture);
        }

        Map<String, Boolean> release = new HashMap<>();
        for (Map.Entry<UUID, Boolean> entry : partyReleaseSettings.entrySet()) {
            release.put(entry.getKey().toString(), entry.getValue());
        }

        toSave.put("partyCaptureSettings", capture);
        toSave.put("partyReleaseSettings", release);

        try (Writer writer = new BufferedWriter(new FileWriter(jsonFile))) {
            gson.toJson(toSave, writer);
        } catch (IOException e) {
            System.err.println("Could not save to " + jsonFile.getAbsolutePath());
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private synchronized boolean loadFromFile() {
        if (!jsonFile.exists()) return true; // no file yet, ignore

        try (Reader reader = new BufferedReader(new FileReader(jsonFile))) {
            Type type = new TypeToken<Map<String, Map<String, Boolean>>>() {
            }.getType();
            Map<String, Map<String, Boolean>> loaded = gson.fromJson(reader, type);

            if (loaded.containsKey("partyCaptureSettings")) {
                partyCaptureSettings.clear();
                for (Map.Entry<String, Boolean> entry : loaded.get("partyCaptureSettings").entrySet()) {
                    try {
                        partyCaptureSettings.put(UUID.fromString(entry.getKey()), entry.getValue());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            if (loaded.containsKey("partyReleaseSettings")) {
                partyReleaseSettings.clear();
                for (Map.Entry<String, Boolean> entry : loaded.get("partyReleaseSettings").entrySet()) {
                    try {
                        partyReleaseSettings.put(UUID.fromString(entry.getKey()), entry.getValue());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Could not load from " + jsonFile.getAbsolutePath());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void startPeriodicSave() {
        scheduler.scheduleAtFixedRate(this::saveToFile, 3, 3, TimeUnit.MINUTES);
    }

    public void shutdown() {
        scheduler.shutdown();
        saveToFile();
    }
}
