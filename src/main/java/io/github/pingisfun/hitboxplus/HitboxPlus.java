package io.github.pingisfun.hitboxplus;

import io.github.pingisfun.hitboxplus.commands.Register;
import io.github.pingisfun.hitboxplus.util.ColorUtil;
import io.github.pingisfun.hitboxplus.waypoints.FlagsBrokenDetector;
import io.github.pingisfun.hitboxplus.waypoints.FlagsPlacedDetector;
import io.github.pingisfun.hitboxplus.waypoints.PlayerCoordSharing;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xaero.common.minimap.waypoints.Waypoint;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.pingisfun.hitboxplus.util.ColorUtil. player;

public class HitboxPlus implements ModInitializer {

	public static final String MOD_ID = "hitboxplus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static boolean sendCoordCooldown = true;
	private static boolean pingCooldownDisabled = true;

	public static HashMap<String, Waypoint> pings = new HashMap<>();


	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

		KeyBinding openConfig = new KeyBinding("Open Config", InputUtil.GLFW_KEY_O, "Crusalis Utils");
		KeyBindingHelper.registerKeyBinding(openConfig); // Binding O to opening menu because its a rarely used key

		KeyBinding sendCoords = new KeyBinding("Send your coordinates in chat", InputUtil.GLFW_KEY_J, "Crusalis Utils");
		KeyBindingHelper.registerKeyBinding(sendCoords); // pressing J sends your coordinates and other clients can recieve it

		KeyBinding sendPing = new KeyBinding("Send a location ping", InputUtil.GLFW_KEY_K, "Crusalis Utils");
		KeyBindingHelper.registerKeyBinding(sendPing); // pressing K sends your coordinates and other clients can recieve it

		KeyBinding teamBind = new KeyBinding("Register Team", InputUtil.GLFW_KEY_N, "Crusalis Utils");
		KeyBindingHelper.registerKeyBinding(teamBind); // pressing N adds an entire nation to your "teams list"

		KeyBinding scan = new KeyBinding("Enemy scan", InputUtil.GLFW_KEY_I, "Crusalis Utils");
		KeyBindingHelper.registerKeyBinding(scan);


		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("The_answer_to_life_the_universe_and_everything")
				.executes(context -> { // This is just a meme command
					context.getSource().sendFeedback(Text.literal("42"));
					return 1;
				}
		)));



		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openConfig.wasPressed()) { //When O is pressed

				Screen configScreen = AutoConfig.getConfigScreen(ModConfig.class, client.currentScreen).get();
				client.setScreen(configScreen); // Open the cloth config menu
			}

			if (sendCoords.wasPressed()){ // When the send coords button is pressed
				sendCoordsInChat();
            }

			if (sendPing.wasPressed()){
				sendPing();
			}

			if (scan.wasPressed()){

				sendScanHotbarMessage(scan());
			}


			if (teamBind.wasPressed()) { //Add an entire team to your list
				MinecraftClient clientPlayer = MinecraftClient.getInstance(); //Gets the player you clicked at
				try {
					addTeam(clientPlayer); //Tries to add him to your teams list
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

            }
		});

		ClientCommandRegistrationCallback.EVENT.register(Register::registerCommands); // Registers the commands


		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {

			if  (MinecraftClient.getInstance().player == null){
				return true;
			}


			FlagsPlacedDetector.checkForPlacedFlags(message.toString());

			FlagsBrokenDetector.handleFlags(message.toString());

			PlayerCoordSharing.handleServerWaypoint(message.toString());

			return PlayerCoordSharing.handleServerPing(message.toString());

		});

		ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {

			if  (sender == null || MinecraftClient.getInstance().player == null){
				MinecraftClient.getInstance().player.sendMessage(Text.literal("sender or instance is null"));
				return true;
			}


			PlayerCoordSharing.handlePlayerWaypoint(message.toString(), sender);

			return PlayerCoordSharing.handlePlayerPing(message.toString(), sender);

		});

	}

	public static void sendScanHotbarMessage(List<Integer> list) {

		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null)  return;

		Text enemies = Text.literal("§l" + list.get(0) + " enemies ").formatted(Formatting.RED);
		Text allies = Text.literal("§l" + list.get(1) + " allies ").formatted(Formatting.DARK_AQUA);
		Text nation = Text.literal("§l" + list.get(2) + " nation").formatted(Formatting.GOLD);

		client.player.sendMessage(enemies.copy().append(allies).copy().append(nation), true);
	}

	public static List<Integer> scan(){

		int enemyAmmount = 0;
		int allyAmount = 0;
		int nationAmount = 0;

		for (AbstractClientPlayerEntity player : getPlayersInRenderDistance()){

			if (player.equals(MinecraftClient.getInstance().player)) continue;

			int playerColor = ColorUtil.getPlayerPrefixColorHex(player);

			//Checks for green dark green and dark aqua
			if (playerColor == 0x55FF55 || playerColor == 0x00AA00) {
				nationAmount++;
			} else if (playerColor == 0x00AAAA) {
				allyAmount++;
			} else{
				enemyAmmount++;
			}
		}

		return List.of(enemyAmmount, allyAmount, nationAmount);
	}

	public static List<AbstractClientPlayerEntity> getPlayersInRenderDistance() {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientWorld world = client.world;

		if (world == null) {
			return List.of();
		}

		// Directly retrieve all players in the world, as they're already within render distance
		return world.getPlayers();
	}

	public static void sendCoordsInChat(){

		MinecraftClient mcClient = MinecraftClient.getInstance();
		int x = (int) MinecraftClient.getInstance().player.getX();
		int y = (int) MinecraftClient.getInstance().player.getY(); // Get the players coordinates
		int z = (int) MinecraftClient.getInstance().player.getZ();


		new Thread(() -> {
			// Make a thread with a timer to auto delete the waypoint

			if(sendCoordCooldown){
				MinecraftClient.getInstance().getNetworkHandler().sendChatMessage("my coords (" + x + "," + y + "," + z + ")");
				sendCoordCooldown = false;// make sure the cooldown is off

				try {
					TimeUnit.SECONDS.sleep(5); //Coldown is set to a minute
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				sendCoordCooldown = true;
				//Parses the player coordinates into a string
			}else {
				player.sendMessage(Text.literal("§c Please wait 5 seconds after sharing coords again")); // Send a message for feedback
			}

		}).start();

	}

	private static void sendPing(){

		if(!pingCooldownDisabled){
			player.sendMessage(Text.literal("§cPing is in cooldown")); // Send a message for feedback
			return;
		}

		BlockPos blockToPing = getBlockPosFromRaycast();

		if (blockToPing == null){
			MinecraftClient.getInstance().player.sendMessage(Text.literal("§cNo block found"), true );
			return;
		}

		int x = blockToPing.getX();
		int y = blockToPing.getY();
		int z = blockToPing.getZ();

		new Thread(() -> {

				MinecraftClient.getInstance().getNetworkHandler().sendChatMessage("pinged location {" + x + "," + y + "," + z + "}");
				pingCooldownDisabled = false;// make sure the cooldown is off

				PlayerCoordSharing.makePlayerPing(x,y,z , "your"); //Make the ping show up for yourself

				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				pingCooldownDisabled = true;
		}).start();
	}

	private static void calculateOreRatio(){

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

		if (!(entityHit.getEntity() instanceof OtherClientPlayerEntity)) return;

		//If you hit another player

		if (entityHit.getEntity().getScoreboardTeam() == null) { //If they have no team
			assert MinecraftClient.getInstance().player != null;
			MinecraftClient.getInstance().player.sendMessage(Text.literal("§c§ Player has no team"), true);
			return;
		}


		String team = entityHit.getEntity().getScoreboardTeam().getName();

		// Prefixes are a nested hell

		boolean wasEnemy = config.enemyteam.oreolist.remove(team); //This is to switch between enemy/friend
		boolean wasFriend = config.friendteam.oreolist.remove(team);


		String prefix = null;
		for (Text ogSibling : entityHit.getEntity().getDisplayName().getSiblings()){
			for (Text sibling : ogSibling.getSiblings()){ //Loops through the siblings

				// And finds an eligible prefix
				if ((prefixConvert(sibling.toString(), team)).isEmpty()) { //If the team has no prefix
					continue;
				} else {
					prefix = prefixConvert(sibling.toString(), team);
					break;
				}
			}
		}


		if (prefix == null || prefix.isEmpty()){ //if no prefix was found
			MinecraftClient.getInstance().player.sendMessage(Text.literal("This team has no prefix :("));
		}

		if (wasFriend && wasEnemy) {
			assert true; // Do nothing
		} else if (!wasFriend && !wasEnemy) {
			config.friendteam.oreolist.add(team); //if the player wasnt enemy or friend add him to the friends list
			if (!config.prefix.oreolist.contains(prefix) && !(prefix == null || prefix.isEmpty())) {
				MinecraftClient.getInstance().player.sendMessage(Text.literal("Prefix added to friend list"));
				config.prefix.oreolist.add(prefix);
			}

		} else if (wasFriend) { //if he was a freind add him to the enemy list
			config.enemyteam.oreolist.add(team);
			if (!config.prefix.oreolist.contains(prefix) && !(prefix == null || prefix.isEmpty())) {

				config.prefix.oreolist.add(prefix); //Add his prefix to the "prefix to town" list
			}
		}

		AutoConfig.getConfigHolder(ModConfig.class).setConfig(config);
		AutoConfig.getConfigHolder(ModConfig.class).save();
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

	private static String prefixConvert(String prefix, String team){ //Takes the prefix of the team object
		int startIndex = prefix.indexOf("[");
		int endIndex = prefix.lastIndexOf("] "); //This code wont work if the prefix isnt contained within brackets

		if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
			String desiredSubstring = prefix.substring(startIndex, endIndex + 1);
			return desiredSubstring + " = " + team; // Output: town = [prefix]
		}
		return ""; //Return nothing
	}

	public static BlockPos getBlockPosFromRaycast() {
		double maxDistance = 400;

		MinecraftClient client = MinecraftClient.getInstance();
		Entity cameraEntity = client.cameraEntity;
		if (cameraEntity != null) {
			// Get the player view position and rotation
			Vec3d cameraPos = cameraEntity.getCameraPosVec(1.0f);
			Vec3d rot = cameraEntity.getRotationVec(1.0f);
			Vec3d rayEnd = cameraPos.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);

			// Perform the raycast
			RaycastContext context = new RaycastContext(
					cameraPos, rayEnd,
					RaycastContext.ShapeType.OUTLINE,
					RaycastContext.FluidHandling.NONE,
					cameraEntity
			);

			BlockHitResult hitResult = cameraEntity.getWorld().raycast(context);

			// Check if a block was hit and return its position
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				return hitResult.getBlockPos();
			}
		}

		return null; // Return null if no block is hit
	}

}