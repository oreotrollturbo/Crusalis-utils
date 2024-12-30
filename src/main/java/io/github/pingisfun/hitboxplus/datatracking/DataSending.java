package io.github.pingisfun.hitboxplus.datatracking;

import io.github.pingisfun.hitboxplus.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;

import java.util.concurrent.Executors;

public class DataSending {
    public static void init() {
        startLoop();
    }

    private static final long TEN_MINUTES_IN_MILLIS = 10 * 60 * 1000; // 10 minutes in milliseconds

    public static void startLoop() {
        // Create a new thread using an executor
        Executors.newSingleThreadExecutor().submit(() -> {
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            while (config.areAnalyticsEnabled) {
                try {

                    //TODO add the code here remember to add uuid and username

                    // Wait for 10 minutes
                    Thread.sleep(TEN_MINUTES_IN_MILLIS);
                } catch (InterruptedException e) {
                    // Handle thread interruption (e.g., shutdown)
                    System.out.println("Thread interrupted. Exiting loop.");
                    break;
                }
            }
        });
    }
}
