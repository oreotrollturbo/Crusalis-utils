package io.github.pingisfun.hitboxplus.waypoints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static io.github.pingisfun.hitboxplus.waypoints.WaypointUtils.config;
import static io.github.pingisfun.hitboxplus.waypoints.WaypointUtils.getWaypointList;

public class FlagsBrokenDetector {

    public static void handleFlags (String message){

        handleNormalFlagBreak(message);
        handleDefendFlagBreak(message);

        handleNormalFlagCap(message);
        handleLiberateFlagCap(message);

    }

    private static void handleNormalFlagBreak(String message){

        //########################################################################
        //                                                                       #
        //                     ATTACK FLAG BREAK DETECTION                       #
        //                                                                       #
        //########################################################################

        // We don't have to define a new pattern all the time we can just redefine the old one
        Pattern pattern = Pattern.compile("Attack\\s*at\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*defeated");
        Matcher matcher = pattern.matcher(message);


        if (matcher.find()) { // If the patter is found

            int waypointX = Integer.parseInt(matcher.group(1));
            int waypointZ = Integer.parseInt(matcher.group(3)); // We take the X and Z ignoring the Y


            getWaypointList().removeIf(waypoint -> waypoint.getX() == waypointX && waypoint.getZ() == waypointZ); // Remove a waypoint in the list if it matches the message coordinates
        }
    }

    private static void handleDefendFlagBreak(String message){

        //########################################################################
        //                                                                       #
        //                        DEFEND FLAG BREAK DETECTION                    #
        //                                                                       #
        //########################################################################

        //Still using the same pattern
        Pattern pattern = Pattern.compile("defended\\s+chunk\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*against");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) { //If its found

            int chunkX = Integer.parseInt(matcher.group(1));// get the chunk coordinates
            int chunkZ = Integer.parseInt(matcher.group(2)); //This one doesn't send precise coordinates but ones of its chunk


            getWaypointList().removeIf(waypoint -> ChunkSectionPos.getSectionCoord(waypoint.getX()) == chunkX &&
                    ChunkSectionPos.getSectionCoord(waypoint.getZ()) == chunkZ); //Checks if any getWaypointList() are in the chunk of the message
        }
    }



    private static void handleNormalFlagCap(String message){

        //########################################################################
        //                                                                       #
        //                        FLAG CAPTURE DETECTION                         #
        //                                                                       #
        //########################################################################

        //Still using the same pattern
        Pattern pattern = Pattern.compile("captured\\s*chunk\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*from\\s*(\\w+)");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) { //if its found

            int x = Integer.parseInt(matcher.group(1));//Get the chunk coordinates (X and Z)
            int z = Integer.parseInt(matcher.group(2));
            String town = matcher.group(3); //Get the town name

            if (config.flagToWayPointTownList.contains(town)) { // If the town is within your towns list
                assert getWaypointList() != null;
                getWaypointList().removeIf(waypoint -> ChunkSectionPos.getSectionCoord(waypoint.getX()) == x &&
                        ChunkSectionPos.getSectionCoord(waypoint.getZ()) == z); //Remove any getWaypointList() that are within the chunk from the message

                if (config.flagToWaypointAdvanced.showNotifications) {
                    assert MinecraftClient.getInstance().player != null;
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("§4 Chunk from " + town + " has been captured"), true);
                } //If yoy have the notifications setting enabled and the town is a "special town"
            }
        }

    }

    private static void handleLiberateFlagCap(String message){

        //########################################################################
        //                                                                       #
        //                        FLAG LIBERATE DETECTION                        #
        //                                                                       #
        //########################################################################

        Pattern pattern = Pattern.compile("liberated\\s*chunk\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*from\\s*(\\w+)");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) { // If the pattern is found

            int x = Integer.parseInt(matcher.group(1));// Get the coordinates
            int z = Integer.parseInt(matcher.group(2));
            String town = matcher.group(3); // Get the town

            if (config.flagToWayPointTownList.contains(town)) { // if the town is in the enemy town list
                assert getWaypointList() != null;
                getWaypointList().removeIf(waypoint -> ChunkSectionPos.getSectionCoord(waypoint.getX()) == x &&
                        ChunkSectionPos.getSectionCoord(waypoint.getZ()) == z); // Remove the waypoint if it's within the chunk
            }
        }

    }
}