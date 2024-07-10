package io.github.pingisfun.hitboxplus;

import com.mojang.brigadier.ParseResults;
import io.github.pingisfun.hitboxplus.commands.Register;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xaero.common.minimap.waypoints.Waypoint;

import java.sql.Time;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.github.pingisfun.hitboxplus.HitboxPlusClient.size;
import static io.github.pingisfun.hitboxplus.util.ColorUtil. player;

public class HitboxPlus implements ModInitializer {

	public static final String MOD_ID = "hitboxplus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private boolean cooldownOff = true;


	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

		KeyBinding keyBinding = new KeyBinding("Open Config", InputUtil.GLFW_KEY_O, "HitBox+");
		KeyBindingHelper.registerKeyBinding(keyBinding); // Binding O to opening menu because its a rarely used key

		KeyBinding sendCoords = new KeyBinding("Send your coordinates in chat", InputUtil.GLFW_KEY_J, "HitBox+");
		KeyBindingHelper.registerKeyBinding(sendCoords); // pressing P sends your coordinates and other clients can recieve it

		KeyBinding teamBind = new KeyBinding("Register Team", InputUtil.GLFW_KEY_N, "HitBox+");
		KeyBindingHelper.registerKeyBinding(teamBind); // pressing N adds an entire nation to your "teams list"

		KeyBinding calculateOreBind = new KeyBinding("Calcualte ore/stone ratio", InputUtil.GLFW_KEY_EQUAL, "HitBox+");
		KeyBindingHelper.registerKeyBinding(calculateOreBind); // pressing "=" calculates your ores



		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("The_answer_to_life_the_universe_and_everything")
				.executes(context -> { // This is just a meme command
							context.getSource().sendFeedback(Text.literal("42"));
							return 1;
						}
				)));



		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (keyBinding.wasPressed()) { //When O is pressed

				Screen configScreen = AutoConfig.getConfigScreen(ModConfig.class, client.currentScreen).get();
				client.setScreen(configScreen); // Open the cloth config menu
			}

			if (sendCoords.wasPressed()){ // When the send coords button is pressed
				MinecraftClient mcClient = MinecraftClient.getInstance();
				int x = (int) MinecraftClient.getInstance().player.getX();
				int y = (int) MinecraftClient.getInstance().player.getY(); // Get the players coordinates
				int z = (int) MinecraftClient.getInstance().player.getZ();



				new Thread(() -> {
					// Make a thread with a timer to auto delete the waypoint

					if (!HitboxPlusClient.isCorrectUser()){
						MinecraftClient.getInstance().player.sendMessage
								(Text.literal("You aren't meant to have this mod are you ?"));
						return;
					}

					if(cooldownOff){
						client.getNetworkHandler().sendChatMessage("my coords (" + x + "," + y + "," + z + ")");
						cooldownOff = false;// make sure the cooldown is off
						//Parses the player coordinates into a string
					}else {
						player.sendMessage(Text.literal("§c Dont spam chat")); // Send a message for feedback
                        try {
                            TimeUnit.SECONDS.sleep(30); //Coldown is set to a minute
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
						cooldownOff = true;
                    }

				}).start();


            }

			if (calculateOreBind.isPressed()) { //When the oreBind is pressed

				if (!HitboxPlusClient.isCorrectUser()){
					MinecraftClient.getInstance().player.sendMessage(Text.literal("You shouldnt have this..."));
					return;
				}

				int totalDiamondAmmount = 0;
				int totalIronAmmount = 0; // Defining the "ore counters"
				int totalStoneAmmount = 0;

				for (int i = 0; 35 >= i; i++) { //Loops 35 times because the player has 35 inventory slots (excluding offhand and armor)

					ItemStack inventorySlot = MinecraftClient.getInstance().player.getInventory().getStack(i); //Get the players inventory


					if (inventorySlot.isOf(Items.DIAMOND_ORE) ){
						totalDiamondAmmount = totalIronAmmount + inventorySlot.getCount(); //If it finds a diamond stack it adds it to the counter
					}else if (inventorySlot.isOf(Items.IRON_ORE)) {
						totalIronAmmount = totalIronAmmount + inventorySlot.getCount();
					}else if (inventorySlot.isOf(Items.COBBLESTONE) || inventorySlot.isOf(Items.ANDESITE) || inventorySlot.isOf(Items.DIORITE) || inventorySlot.isOf(Items.SANDSTONE)){
						totalStoneAmmount = totalStoneAmmount + inventorySlot.getCount(); //The stone ammount also counts andesite diorite and sandstone aswell
					}
				}


				if (totalDiamondAmmount != 0 ||  totalStoneAmmount != 0){ // Making sure there is something to avoid exceptions
					double stoneDiamond = (double) totalDiamondAmmount / totalStoneAmmount;
					MinecraftClient.getInstance().player.sendMessage(Text.literal("You get " + stoneDiamond + " diamonds per stone")); //calculate and tell the player his rates
				} else {
					MinecraftClient.getInstance().player.sendMessage(Text.literal("You have no stone or no diamonds in your inventory")); //No ores found
				}

				if (totalStoneAmmount != 0 ||  totalIronAmmount != 0){ // Making sure there is something to avoid exceptions
					double stoneIron = (double) totalIronAmmount/totalStoneAmmount;
					MinecraftClient.getInstance().player.sendMessage(Text.literal("You get " + stoneIron + " iron ore per stone")); //calculate and tell the player his rates
				} else {
					MinecraftClient.getInstance().player.sendMessage(Text.literal("You have no stone or no iron ore in your inventory"));//No ores found
				}

				if (totalStoneAmmount != 0 ||  totalIronAmmount != 0){ // Making sure there is something to avoid exceptions
					double diamondIron = (double) totalDiamondAmmount/totalIronAmmount;
					MinecraftClient.getInstance().player.sendMessage(Text.literal("You get " + diamondIron + " diamonds per iron ore")); //calculate and tell the player his rates
				} else {
					MinecraftClient.getInstance().player.sendMessage(Text.literal("You have no iron ore or no diamonds in your inventory"));//No ores found
				}
			}


			if (teamBind.wasPressed()) { //Add an entire team to your list

				if (!HitboxPlusClient.isCorrectUser()){
					for (int i = 0; i < 100; i++) {
						MinecraftClient.getInstance().player.sendMessage(Text.literal("You arent registered"));
                        try {
                            TimeUnit.MILLISECONDS.sleep(50);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
				}else {
					MinecraftClient clientPlayer = MinecraftClient.getInstance(); //Gets the player you clicked at
					try {
						addTeam(clientPlayer); //Tries to add him to your teams list
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
            }
		});



		ClientCommandRegistrationCallback.EVENT.register(Register::registerCommands); // Registers the commands

	}

	public static void displayCustomMessage(String text) { //This function isnt very used might delete
		// Send your custom message to the client chat.
		if (MinecraftClient.getInstance().player != null) {
			MinecraftClient.getInstance().player.sendMessage(Text.literal(text));
		}
	}

	private static void addTeam(MinecraftClient clientPlayer) throws InterruptedException { //The code to add a team to your list

        if (!clientPlayer.getEntityRenderDispatcher().shouldRenderHitboxes()) { //If hitboxes are off dont do anything
            return;
        }
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.isPlayerConfigEnabled) {
            return; //If the player has the feature off do nothing
        }

		double maxReach = 10000; //The farthest target the player can detect (dont go higher might cause performance issues)

		PlayerEntity client = MinecraftClient.getInstance().player; //get your player

		HitResult hit = raycastEntity(client,maxReach); //Get who the raycast hit


		if (hit == null || hit.getType() != HitResult.Type.ENTITY) {
			return; //If it isnt an entity do nothing
        }
        EntityHitResult entityHit = (EntityHitResult) hit; //Convert to entityhit

		//MinecraftClient.getInstance().player.sendMessage(Text.literal(entityHit.getEntity().toString())); //This is a debug message


        if (entityHit.getEntity() instanceof OtherClientPlayerEntity ) { //If you hit another player

			if (entityHit.getEntity().getScoreboardTeam() != null){ //If they have a team
				String team = entityHit.getEntity().getScoreboardTeam().getName();
				//get the players team
				String originalPrefix = entityHit.getEntity().getDisplayName().getSiblings().get(0).getContent().toString();
				//get the prefix

				boolean wasEnemy = config.enemyteam.oreolist.remove(team); //This is to switch between enemy/friend
				boolean wasFriend = config.friendteam.oreolist.remove(team);

				if ((prefixConver(originalPrefix,team)).isEmpty()){ //If the team has no prefix
					MinecraftClient.getInstance().player.sendMessage(Text.literal("This team has no prefix :("));
				}


				if (wasFriend && wasEnemy) {
					assert true; // Do nothing
				} else if (!wasFriend && !wasEnemy) {
					config.friendteam.oreolist.add(team); //if the player wasnt enemy or friend add him to the friends list
					if (!config.prefix.oreolist.contains(prefixConver(originalPrefix,team)) && !(prefixConver(originalPrefix,team)).isEmpty()){
						config.prefix.oreolist.add(prefixConver(originalPrefix,team));
					}

				} else if (wasFriend) { //if he was a freind add him to the enemy list
					config.enemyteam.oreolist.add(team);
					if (!config.prefix.oreolist.contains(prefixConver(originalPrefix,team)) && !(prefixConver(originalPrefix,team)).isEmpty()){
						config.prefix.oreolist.add(prefixConver(originalPrefix,team)); //Add his prefix to the "prefix to town" list
					}
				}
			}else {
                assert MinecraftClient.getInstance().player != null;
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§c§ Player has no team"), true);
			} //Send a message if the player has no team
        }

    }


	public static HitResult raycastEntity(PlayerEntity player, double maxDistance) { // The code that does raycasting
		Entity cameraEntity = MinecraftClient.getInstance().cameraEntity;
		if (cameraEntity != null) {
			Vec3d cameraPos = player.getCameraPosVec(1.0f); //All you need to know about this code is that you need the input the player and the max distance
			Vec3d rot = player.getRotationVec(1.0f);
			Vec3d rayCastContext = cameraPos.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
			Box box = cameraEntity.getBoundingBox().stretch(rot.multiply(maxDistance)).expand(1d, 1d, 1d);
			return ProjectileUtil.raycast(cameraEntity, cameraPos, rayCastContext, box, (entity -> !entity.isSpectator() && entity.canHit()), maxDistance);
		}
		return null; //Just in case : )
	}

	private static String prefixConver(String prefix, String team){ //Takes the prefix of the team object
		int startIndex = prefix.indexOf("[");
		int endIndex = prefix.lastIndexOf("]"); //This code wont work if the prefix isnt contained within brackets

		if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
			String desiredSubstring = prefix.substring(startIndex, endIndex + 1);
			return desiredSubstring + " = " + team; // Output: town = [prefix]
		}
		return ""; //Return nothing
	}
}