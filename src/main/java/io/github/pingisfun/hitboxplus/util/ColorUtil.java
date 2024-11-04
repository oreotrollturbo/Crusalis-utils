package io.github.pingisfun.hitboxplus.util;

import io.github.pingisfun.hitboxplus.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.text.Text;

import javax.print.attribute.standard.MediaSize;
import java.awt.*;
import java.util.*;
import java.util.List;


public class ColorUtil {

    public static final ClientPlayerEntity player = MinecraftClient.getInstance().player;


    public static Color getEntityColor(Entity entity) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        //Player config
        if (entity instanceof PlayerEntity && config.isPlayerConfigEnabled) {
            if (entity instanceof ClientPlayerEntity) {
                return ColorUtil.decode(config.self.color, config.self.alpha);
            }

            else if (entity instanceof OtherClientPlayerEntity) {
                assert player != null;


                String username = entity.getName().getString();

                // Default friend logic
                if (config.friend.list.contains((username))) {
                    return ColorUtil.decode(config.friend.color, config.friend.alpha);
                }
                else if (config.enemy.list.contains((username))) {
                    return ColorUtil.decode(config.enemy.color, config.enemy.alpha);
                }


                //Custom Team logic
                if (config.experimental){

                    return ColorUtil.decode(getPlayerPrefixColorHex((OtherClientPlayerEntity) entity),config.friend.alpha);


                } else {
                    if (player.isTeammate(entity)){
                        return ColorUtil.decode(config.friend.color, config.friend.alpha);
                    }
                    else if (entity.getScoreboardTeam() != null){
                        String teamName = entity.getScoreboardTeam().getName();

                        if (config.friendteam.oreolist.contains(teamName)){ //these check for the players team
                            return ColorUtil.decode(config.friend.color, config.friend.alpha);
                        }
                        else if (config.enemyteam.oreolist.contains(teamName)){
                            return ColorUtil.decode(config.enemy.color, config.enemy.alpha);
                        }
                    }
                }


                return ColorUtil.decode(config.neutral.color, config.neutral.alpha);

            }
        }

        // Entity config
        else if (entity instanceof EnderDragonEntity && config.enderDragon.isEnabled && config.enderDragon.boxHitbox) {
            return ColorUtil.decode(config.enderDragon.color, config.enderDragon.alpha);
        } else if (entity instanceof HostileEntity && config.hostile.isEnabled) {
            return ColorUtil.decode(config.hostile.color, config.hostile.alpha);
        } else if ((entity instanceof PassiveEntity || entity instanceof AllayEntity || entity instanceof AmbientEntity) && config.passive.isEnabled) {
            return ColorUtil.decode(config.passive.color, config.passive.alpha);
        } else if ((entity instanceof ProjectileEntity) && config.projectile.isEnabled) {
            if (entity instanceof PersistentProjectileEntity persistentProjectile
                    && !config.projectile.renderStuck
                    && persistentProjectile.pickupType == PersistentProjectileEntity.PickupPermission.DISALLOWED) {
                return ColorUtil.transparent();
            } else if (entity instanceof EnderPearlEntity) {
                return ColorUtil.decode(config.misc.enderPearlEntity.color, config.misc.enderPearlEntity.alpha);
            } else if (entity instanceof TridentEntity) {
                return ColorUtil.decode(config.misc.tridentEntity.color, config.misc.tridentEntity.alpha);
            }
            return ColorUtil.decode(config.projectile.color, config.projectile.alpha);
        } else if ((entity instanceof AbstractDecorationEntity || entity instanceof ArmorStandEntity) && config.decoration.isEnabled) {
            return ColorUtil.decode(config.decoration.color, config.decoration.alpha);
        } else if ((entity instanceof AbstractMinecartEntity || entity instanceof BoatEntity) && config.vehicle.isEnabled) {
            return ColorUtil.decode(config.vehicle.color, config.vehicle.alpha);
        } else if (isMiscEntity(entity) && config.misc.isEnabled) {
            if (entity instanceof AreaEffectCloudEntity) {
                return ColorUtil.decode(config.misc.areaEffectCloud.color, config.misc.areaEffectCloud.alpha);
            } else if (entity instanceof ExperienceOrbEntity) {
                return ColorUtil.decode(config.misc.experienceOrb.color, config.misc.experienceOrb.alpha);
            } else if (entity instanceof EyeOfEnderEntity) {
                return ColorUtil.decode(config.misc.eyeOfEnder.color, config.misc.eyeOfEnder.alpha);
            } else if (entity instanceof FallingBlockEntity) {
                return ColorUtil.decode(config.misc.fallingBlock.color, config.misc.fallingBlock.alpha);
            } else if (entity instanceof ItemEntity) {
                return ColorUtil.decode(config.misc.item.color, config.misc.item.alpha);
            } else if (entity instanceof TntEntity) {
                return ColorUtil.decode(config.misc.tnt.color, config.misc.tnt.alpha);
            } else if (entity instanceof EndCrystalEntity) {
                return ColorUtil.decode(config.misc.endCrystalEntity.color, config.misc.endCrystalEntity.alpha);
            }
        }

        return ColorUtil.decode(config.color, config.alpha);
    }

    public static Color getDragonPartColor() {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.enderDragon.isEnabled) {
            return ColorUtil.decode(config.color, config.alpha);
        } else if (!config.enderDragon.realHitbox) {
            return ColorUtil.decode(config.enderDragon.partColor, 0);
        }
        return ColorUtil.decode(config.enderDragon.partColor, config.enderDragon.partAlpha);
    }

    private static Color decode(int hex, int transparency) {
        int alpha = ((100 - (transparency * 10)) * 25) / 10;

        if (alpha == 0) {
            alpha = 1;
        }

        Color rgb = Color.decode(String.valueOf(hex));
        return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
    }

    private static Color transparent() {
        return new Color(0, 0, 0, 0);
    }

    private static boolean isMiscEntity(Entity entity) {
        return entity instanceof AreaEffectCloudEntity || entity instanceof ExperienceOrbEntity || entity instanceof EyeOfEnderEntity || entity instanceof FallingBlockEntity || entity instanceof ItemEntity || entity instanceof TntEntity || entity instanceof EndCrystalEntity;

    }



    public static int getPlayerPrefixColorHex(PlayerEntity player) {
        Text displayName = player.getDisplayName();

        // Search through each sibling to find the prefix color
        for (Text sibling : displayName.getSiblings()) {

            MinecraftClient.getInstance().player.sendMessage(Text.of(sibling.toString()));

            if (sibling.toString().contains("§")){

                int color = mcColorCodesToHex(sibling.toString());

                if (color == 5){
                    continue;
                }

                MinecraftClient.getInstance().player.sendMessage(Text.of(String.valueOf(color)));

                return color;
            }
        }

        MinecraftClient.getInstance().player.sendMessage(Text.of("Defaulting to white"));

        // Default to white if no color is found
        return 0xFFFFFF;
    }



    private static final Map<Character, Integer> COLOR_CODE_TO_INT = new HashMap<>();

    static {
        COLOR_CODE_TO_INT.put('0', 0x000000); // Black
        COLOR_CODE_TO_INT.put('1', 0x0000AA); // Dark Blue
        COLOR_CODE_TO_INT.put('2', 0x00AA00); // Dark Green
        COLOR_CODE_TO_INT.put('3', 0x00AAAA); // Dark Aqua
        COLOR_CODE_TO_INT.put('4', 0xAA0000); // Dark Red
        COLOR_CODE_TO_INT.put('5', 0xAA00AA); // Dark Purple
        COLOR_CODE_TO_INT.put('6', 0xFFAA00); // Gold
        COLOR_CODE_TO_INT.put('7', 0xAAAAAA); // Gray
        COLOR_CODE_TO_INT.put('8', 0x555555); // Dark Gray
        COLOR_CODE_TO_INT.put('9', 0x5555FF); // Blue
        COLOR_CODE_TO_INT.put('a', 0x55FF55); // Green
        COLOR_CODE_TO_INT.put('b', 0x55FFFF); // Aqua
        COLOR_CODE_TO_INT.put('c', 0xFF5555); // Red
        COLOR_CODE_TO_INT.put('d', 0xFF55FF); // Light Purple
        COLOR_CODE_TO_INT.put('e', 0xFFFF55); // Yellow
        COLOR_CODE_TO_INT.put('f', 0xFFFFFF); // White
    }

    public static int mcColorCodesToHex(String text) {
        int color = 5;
        boolean colorCode = false;

        for (char c : text.toCharArray()) {
            if (colorCode) {
                if (COLOR_CODE_TO_INT.get(c) == null){
                    colorCode = false;
                    continue;
                }
                color = COLOR_CODE_TO_INT.get(c);
                break;
            } else if (c == '§') {
                colorCode = true;
            }
        }

        return color;
    }
}
