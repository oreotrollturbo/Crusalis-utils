package io.github.pingisfun.hitboxplus.waypoints;

import com.mojang.authlib.GameProfile;
import io.github.pingisfun.hitboxplus.HitboxPlus;
import io.github.pingisfun.hitboxplus.ModConfig;
import io.github.pingisfun.hitboxplus.util.ConfEnums;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointSet;
import xaero.common.minimap.waypoints.WaypointsManager;


import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static java.lang.Math.hypot;

public class WaypointUtils {

    static ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

    public static int color = 0;

    static int yOffset = config.yOffset;

    //########################################################################
    //                                                                       #
    //                                FUNCTIONS                              #
    //                                                                       #
    //########################################################################


    public static boolean isInRange (int playerX,int playerZ, int waypointX, int waypointZ){

        int trueX = abs(playerX - waypointX); // Get the difference between the player X and waypoint X
        int trueZ = abs(playerZ - waypointZ); // Get the difference between the player Z and waypoint Z

        int distance = (int) hypot(trueX,trueZ); //Calculate the distance thanks to the pythagorian theorem
        // keeping it simple with just integers

        if (config.flagToWaypointAdvanced.limitRange == ConfEnums.FlagLimiter.WITHIN){ // If the setting is set to WITHIN
            return distance <= config.flagToWaypointAdvanced.pingDistanceLimit; // Return true if it is within the distance
        }else {//This one is when you have the setting to OUT_OF
            return distance > config.flagToWaypointAdvanced.pingDistanceLimit; // Return true if it is out of the distance
        }
    }

    public static List<Waypoint> getWaypointList(){ // This simply makes the code more DRY
        XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
        if (minimapSession == null) return null;
        WaypointsManager waypointsManager = minimapSession.getWaypointsManager();
        WaypointSet waypointSet = waypointsManager.getWaypoints();
        if (waypointSet == null) return null;

        return waypointSet.getList(); // All it does is get the waypoint list so that you don't have to do it in every part individually
    }

    public static void makeTimerWaypoint(List<Waypoint> waypoints , int x, int y, int yOffset, int z , int color, String town, String waypointSymbol,int time,boolean isTown){

        String waypointName;

        if (isTown) {
            waypointName = "Flag on " + town;
        } else {
            waypointName = town;
        }


        // Make a thread with a timer to auto delete the waypoint
        assert waypoints != null;
        waypoints.add(new Waypoint(x, y + yOffset, z, // Add the waypoint
                waypointName, waypointSymbol, color, 0, true));

        Waypoint lastWaypoint = waypoints.get(waypoints.size() - 1); //Get the waypoint in the thread to delete it later

        if (config.flagToWaypointAdvanced.playFlagSounds) { //play a sound if the setting is on and the list has the town
            assert MinecraftClient.getInstance().player != null;
            MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_BELL_USE, 1, config.flagToWaypointAdvanced.pitch);
        }

        if (time == 0){
            deleteWaypointInTime(lastWaypoint ,config.removeCooldown);
        }else {
            deleteWaypointInTime(lastWaypoint ,time);
        }
    }

    public static void deleteWaypointInTime(Waypoint waypoint, int time){

        new Thread(() -> {

            try { // Count down and then delete the waypoint
                TimeUnit.SECONDS.sleep(time); // 4 minutes by default
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            deleteWaypoint(waypoint);// Delete the waypoint
        }).start();
    }

    public static void deleteWaypoint(Waypoint waypoint){
        if (waypoint == null || getWaypointList() == null) return;

        HitboxPlus.pings.values().removeIf(value -> value.equals(waypoint));

        getWaypointList().remove(waypoint);
    }

    public static boolean isValid(String message, GameProfile player){

        if (message == null || message.isEmpty()) return false;

        if (player == null){
            if (config.coordSharing.locationSharing.acceptCoordsFromFriends){
                for (String nick : config.friend.list) {

                    if (!message.contains(nick)){
                        continue;
                    }

                    return true;
                }
            }
        } else {
            if (config.coordSharing.locationSharing.acceptCoordsFromFriends &&
            config.friend.list.contains(player.getName())){
                return true;
            }
        }


        if (message.contains("[Local]") && config.coordSharing.acceptCoordsFromLocal){
            return true;
        }
        else if (message.contains("[Town]") && config.coordSharing.acceptCoordsFromTown){
            return true;
        }
        else if (message.contains("[Nation]") && config.coordSharing.acceptCoordsFromNation){
            return true;
        }
        else if (message.contains("[Ally]") && config.coordSharing.acceptCoordsFromAlly){
            return true;
        }

        return false;
    }

}
