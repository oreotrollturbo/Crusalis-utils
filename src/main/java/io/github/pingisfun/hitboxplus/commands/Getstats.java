package io.github.pingisfun.hitboxplus.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.pingisfun.hitboxplus.data.enums.ToggleFriendEnum;
import io.github.pingisfun.hitboxplus.datatracking.DataTracking;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Getstats {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("GetStats")
                .executes(context -> {
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Config menu opened: " + DataTracking.openedConfig));
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Location pings sent: " + DataTracking.locationPings));
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Position pings sent: " + DataTracking.positionPings));
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Players killed: " + DataTracking.kills));
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Deaths: " + DataTracking.deaths));
                            return 0;
                        }
                )
        );
    }
}
