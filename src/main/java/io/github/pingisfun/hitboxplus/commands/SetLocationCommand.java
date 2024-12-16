package io.github.pingisfun.hitboxplus.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class SetLocationCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) { // TODO add colour param
        dispatcher.register(
                literal("rallyPoint")
                        .then(argument("x", DoubleArgumentType.doubleArg())
                                .then(argument("y", DoubleArgumentType.doubleArg())
                                        .then(argument("z", DoubleArgumentType.doubleArg())
                                                .then(argument("time", IntegerArgumentType.integer(0))
                                                        .then(argument("name", StringArgumentType.string())
                                                                .executes(context -> {
                                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                                    double y = DoubleArgumentType.getDouble(context, "y");
                                                                    double z = DoubleArgumentType.getDouble(context, "z");
                                                                    int time = IntegerArgumentType.getInteger(context, "time");
                                                                    String name = StringArgumentType.getString(context, "name");

                                                                    // Send the message to the player
                                                                    String message = String.format("Set location {%d,%d,%d} [%d] (%s)", (int)x, (int)y, (int)z, time, name);
                                                                    MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(message);

                                                                    return 1; // Indicate success
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }
}
