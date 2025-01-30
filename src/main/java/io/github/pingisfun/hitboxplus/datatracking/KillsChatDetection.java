package io.github.pingisfun.hitboxplus.datatracking;

import io.github.pingisfun.hitboxplus.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.pingisfun.hitboxplus.HitboxPlus.isPlayerOnServer;

public class KillsChatDetection {

    //A lot of optional spaces just in case
    private static List<String> killPatterns = List.of(
            "while\\s+trying\\s+to\\s+escape\\s+([\\w\\d_]+)",
            "by\\s+([\\w\\d_]+)",
            "while\\s+fighting\\s+([\\w\\d_]+)",
            "to\\s+escape\\s+([\\w\\d_]+)",
            "due\\s+to\\s+([\\w\\d_]+)",
            "died\\s+because\\s+of\\s+([\\w\\d_]+)"
    );


    public static void checkKillsInChat(String message) {
        // Return early if the message is custom or the player is not on the server
        if (isCustomChatMessage(message) || !isPlayerOnServer("crusalis.net")) return;

        // Loop through the kill patterns
        for (String pattern : killPatterns) {
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(message);

            if (matcher.find()) {
                String username = matcher.group(1);

                String playerName = MinecraftClient.getInstance().getSession().getUsername();

                if (username.equals(playerName)) {

                    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

                    config.kills++;

                    if (config.playSoundOnKill){
                        playSound();
                    }

                    AutoConfig.getConfigHolder(ModConfig.class).save();
                }

                return;
            }
        }
    }


    private static boolean isCustomChatMessage(String message) {
        return message.contains("[Town]") || message.contains("[Local]") || message.contains("[Nation]") || message.contains("[Ally]")
                || message.contains("[War]");
    }

    public static void playSound() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.BLOCK_NOTE_BLOCK_PLING, // Replace with your desired sound
                    1.0F // Volume
            ));
        }
    }
}
