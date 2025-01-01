package io.github.pingisfun.hitboxplus.datatracking;

import io.github.pingisfun.hitboxplus.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class DataSending {
    public static void init() {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (config.areAnalyticsDisabled) return;
        startLoop();
    }

    private static final long TEN_MINUTES_IN_MILLIS = 10 * 60 * 100; // 10 minutes in milliseconds

    public static void startLoop() {
        // Create a new thread using an executor
        Executors.newSingleThreadExecutor().submit(() -> {
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            while (config.areAnalyticsDisabled) {
                try {

                    Thread.sleep(TEN_MINUTES_IN_MILLIS);

                    if (DataTracking.noDataChanges()) continue;
                    sendData();

                } catch (InterruptedException e) {
                    // Handle thread interruption (e.g., shutdown)
                    System.out.println("Thread interrupted. Exiting loop.");
                    break;
                }
            }
        });
    }

    public static void sendData() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return;
        }

        String username = MinecraftClient.getInstance().getSession().getUsername();
        String uuid = MinecraftClient.getInstance().getSession().getUuidOrNull().toString();
        int deaths = DataTracking.deaths;
        int locationPings = DataTracking.locationPings;
        int locationWaypoints = DataTracking.positionPings;
        int rallyPoints = DataTracking.rallyPoints;
        int openedConfig = DataTracking.openedConfig;
        int loggedOnCrusalis = DataTracking.joinedCrusalis;
        int flagsDetected = DataTracking.flagsDetected;

        try {
            // Build the URL
            String urlString = buildUrl(uuid, username, deaths, locationPings, locationWaypoints, rallyPoints, openedConfig, loggedOnCrusalis, flagsDetected);

            // Create the HTTP request asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "MinecraftClient/1.0");

                    int responseCode = connection.getResponseCode();
                    System.out.println("Response Code: " + responseCode);

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    System.out.println(response.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reset the tracking variables after sending the data
        DataTracking.wipeVariables();
    }


    // Method to build the URL with variables
    private static String buildUrl(String uuid, String username, int deaths, int locationPings,
                                   int locationWaypoints, int rallyPoints, int openedConfig, int loggedOnCrusalis,
                                   int flagsDetected) throws UnsupportedEncodingException {
        String baseUrl = "https://crusalis-api-666731169374.europe-west10.run.app?";

        // Encode the parameters to avoid issues with special characters
        String encodedUuid = URLEncoder.encode(uuid, "UTF-8");
        String encodedUsername = URLEncoder.encode(username, "UTF-8");

        // Build the URL with the variables
        return baseUrl + "uuid=" + encodedUuid +
                "&username=" + encodedUsername +
                "&deaths=" + deaths +
                "&location_pings=" + locationPings +
                "&location_waypoints=" + locationWaypoints +
                "&rally_points=" + rallyPoints +
                "&opened_config=" + openedConfig +
                "&logged_on_crusalis=" + loggedOnCrusalis +
                "&flags_detected=" + flagsDetected;
    }
}
