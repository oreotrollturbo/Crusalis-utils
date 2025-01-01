package io.github.pingisfun.hitboxplus.datatracking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class DataTracking {

    public static int locationPings = 0;
    public static int positionPings = 0;
    public static int deaths = 0;
    public static int rallyPoints = 0;
    public static int openedConfig = 0;
    public static int flagsDetected = 0;
    //public static int flagsPlaced = 0;
    public static int joinedCrusalis = 0;

    public static void wipeVariables(){
        locationPings = 0;
        positionPings = 0;
        deaths = 0;
        rallyPoints = 0;
        openedConfig = 0;
        flagsDetected = 0;
        //flagsPlaced = 0;
        joinedCrusalis = 0;
    }

    public static void sendDataToPlayer(){
        MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(locationPings)));
        MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(positionPings)));
        MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(deaths)));
        MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(rallyPoints)));
        MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(openedConfig)));
        MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(flagsDetected)));
        MinecraftClient.getInstance().player.sendMessage(Text.literal(String.valueOf(joinedCrusalis)));
    }

    public static boolean noDataChanges(){
        return locationPings + positionPings + deaths + rallyPoints + openedConfig == 0;
    }
}
