package io.github.pingisfun.hitboxplus.waypoints;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.pingisfun.hitboxplus.waypoints.FlagsPlacedDetector.getWaypoints;

public class RallyPointDetection {

    /**
     * Handles rally point detection when a player sends a message.
     *
     * @param message the message sent by the player
     */
    public static void handleRallyPointMessage(String message) {



        Pattern pattern = Pattern.compile(
                "\\s*Set\\s+location\\s*\\{\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\}\\s*\\[\\s*(\\d+)\\s*\\]\\s*\\((\\w+)\\)"
        );

        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {

            if (!WaypointUtils.isValid(message,null)) return;

            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            int time = Integer.parseInt(matcher.group(4));
            String name = matcher.group(5);

            handleRallyPoint(x, y, z, time, name);
        }
    }

    public static void handleRallyPointMessageChat(String message, GameProfile player) {

        Pattern pattern = Pattern.compile(
                "\\s*Set\\s+location\\s*\\{\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\}\\s*\\[\\s*(\\d+)\\s*\\]\\s*\\((\\w+)\\)"
        );

        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {

            if (!WaypointUtils.isValid(message,player)) return;

            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            int time = Integer.parseInt(matcher.group(4));
            String name = matcher.group(5);

            handleRallyPoint(x, y, z, time, name);
        }
    }

    /**
     * Handles general rally point detection logic.
     */
    private static void handleRallyPoint(int x, int y, int z, int time, String name) {
        //6 is yellow
        // Placeholder for additional processing logic

        WaypointUtils.makeTimerWaypoint(getWaypoints(),x,y,0,z,6,name,"[O]",time,false);
    }
}
