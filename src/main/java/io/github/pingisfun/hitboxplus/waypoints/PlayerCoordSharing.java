package io.github.pingisfun.hitboxplus.waypoints;

import com.mojang.authlib.GameProfile;
import io.github.pingisfun.hitboxplus.HitboxPlus;
import io.github.pingisfun.hitboxplus.util.Encryption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import xaero.common.minimap.waypoints.Waypoint;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static io.github.pingisfun.hitboxplus.waypoints.WaypointUtils.*;

public class PlayerCoordSharing {

    public static void handlePlayerWaypoint(String message, GameProfile sender){


        //########################################################################
        //                                                                       #
        //                  COORDINATE DETECTION NORMAL MESSAGES                 #
        //                                                                       #
        //########################################################################


        if (!config.coordSharing.locationSharing.acceptCoordsFromFriends || !config.friend.list.contains(sender.getName()) ||
                !message.contains("my coords (")) {
            return;
        }

        handleWaypointCreation(message, sender.getName());
    }

    public static void handleServerWaypoint(String message){

        // The reason the function that detects player code doesn't work is because many servers to
        // filter/redirect messages they convert them into server messages. Nodes does this too;

        if (message == null || !config.coordSharing.locationSharing.acceptCoordsFromFriends || !message.contains("my coords (")) {
            return;
        }

        for (String nick : config.friend.list) {

            if (!message.contains(nick)){
                continue;
            }

            handleWaypointCreation(message,nick);

            return;
        }

        if (message.contains("[Local]") && config.coordSharing.acceptCoordsFromLocal){
            handleWaypointCreation(message, "local");
        }
        else if (message.contains("[Town]") && config.coordSharing.acceptCoordsFromTown){
            handleWaypointCreation(message, "town");
        }
        else if (message.contains("[Nation]") && config.coordSharing.acceptCoordsFromNation){
            handleWaypointCreation(message, "nation");
        }
        else if (message.contains("[Ally]") && config.coordSharing.acceptCoordsFromAlly){
            handleWaypointCreation(message, "ally");
        }
    }

    public static Boolean handlePlayerPing(String message, GameProfile sender){

        // The reason the function that detects player code doesn't work is because many servers in order to
        // filter/redirect messages they convert them into server messages . Nodes does this too;

        if (message == null || !config.coordSharing.pingSharing.acceptPings || !message.contains("pinged location {")
        || !config.friend.list.contains(sender.getName())) {
            return true;
        }

        handlePingCreation(message,sender.getName());

        return false;
    }


    public static Boolean handleServerPing(String message){

        // The reason the function that detects player code doesn't work is because many servers in order to
        // filter/redirect messages they convert them into server messages . Nodes does this too;

        if (message == null || !config.coordSharing.pingSharing.acceptPings || !message.contains("pinged location {")) {
            return true;
        }

        boolean showMessage = config.coordSharing.pingSharing.pingsInChat;

        for (String nick : config.friend.list) {

            if (!message.contains(nick)){
                continue;
            }

            handlePingCreation(message,nick);

            return showMessage;
        }

        if (message.contains("[Local]") && config.coordSharing.acceptCoordsFromLocal){
            handleWaypointCreation(message, "local");
            return showMessage;
        }
        else if (message.contains("[Town]") && config.coordSharing.acceptCoordsFromTown){
            handleWaypointCreation(message, "town");
            return showMessage;
        }
        else if (message.contains("[Nation]") && config.coordSharing.acceptCoordsFromNation){
            handleWaypointCreation(message, "nation");
            return showMessage;
        }
        else if (message.contains("[Ally]") && config.coordSharing.acceptCoordsFromAlly){
            handleWaypointCreation(message, "ally");
            return showMessage;
        }

        return true;

    }


    private static void handleWaypointCreation(String message, String playerName) {
        String regex = "my coords \\s*\\(([^,]+),\\s*([^,]+),\\s*([^\\)]+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        if (!matcher.find()) {
            System.out.println("No coordinates found in the message.");
            return;
        }

        try {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            makePlayerWaypoint(x, y, z, playerName);

        } catch (NumberFormatException e) {
            if (!message.contains("[E]") || config.coordSharing.encryptionKey.isBlank()) return;

            Integer x = Encryption.decryptNumber(matcher.group(1));
            Integer y = Encryption.decryptNumber(matcher.group(2));
            Integer z = Encryption.decryptNumber(matcher.group(3));

            if (x == null || y == null || z == null) return;

            makePlayerWaypoint(x, y, z, playerName);
        }
    }


    private static void makePlayerWaypoint(int x, int y, int z , String nick){

        if (getWaypointList() == null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("Waypoints are null"));
            return;
        }
        
        getWaypointList().add(new Waypoint(x, y, z, //Add the waypoint with the detected coordinates
                nick + "'s location", "[T]", 65535, 0, true));
        Waypoint waypoint = getWaypointList().get(getWaypointList().size() - 1);
        waypoint.setOneoffDestination(true);

        deleteWaypointInTime(waypoint, config.coordSharing.locationSharing.friendWaypointTimer);
    }

    private static void handlePingCreation(String message, String playerName) {
        String regex = "pinged location \\s*\\{([^,]+),\\s*([^,]+),\\s*([^}]+)}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        if (!matcher.find()) {
            System.out.println("No coordinates found in message.");
            return;
        }

        try {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            makePlayerPing(x, y, z, playerName);

        } catch (NumberFormatException e) {
            if (!message.contains("[E]") || config.coordSharing.encryptionKey.isBlank()) return;

            Integer x = Encryption.decryptNumber(matcher.group(1));
            Integer y = Encryption.decryptNumber(matcher.group(2));
            Integer z = Encryption.decryptNumber(matcher.group(3));

            if (x == null || y == null || z == null) return;

            makePlayerPing(x, y, z, playerName);
        }
    }

    public static void makePlayerPing(int x, int y, int z , String nick){

        if (getWaypointList() == null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("Waypoints are null"));
            return;
        }

        getWaypointList().add(new Waypoint(x, y, z, //Add the waypoint with the detected coordinates
                nick + "'s ping", "o", 0, 0, true));
        Waypoint waypoint = getWaypointList().get(getWaypointList().size() - 1);
        waypoint.setOneoffDestination(true);

        if (config.coordSharing.pingSharing.deletePreviousPing) {

            if (HitboxPlus.pings.containsKey(nick)){
                deleteWaypoint(HitboxPlus.pings.get(nick));
            }

            HitboxPlus.pings.put(nick,waypoint);
        }

        deleteWaypointInTime(waypoint, config.coordSharing.pingSharing.pingWaypointTimer);
    }

}