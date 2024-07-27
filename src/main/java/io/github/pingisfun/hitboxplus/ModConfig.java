package io.github.pingisfun.hitboxplus;

import io.github.pingisfun.hitboxplus.util.ConfEnums;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Config(name = HitboxPlus.MOD_ID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Category(value = "general")
    public boolean isModEnabled = true;

    @ConfigEntry.Category(value = "general")
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.ColorPicker()
    public int color = 0xFFFFFF;

    @ConfigEntry.Category(value = "general")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(max = 10, min = 0)
    public int alpha = 10;

    @ConfigEntry.Category(value = "general")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public ConfEnums.PlayerListTypes middleClick = ConfEnums.PlayerListTypes.FRIEND; //Instead of looping middle click just adds friends

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category(value = "players")
    public boolean isPlayerConfigEnabled = false;


    @ConfigEntry.Category(value = "players")
    @ConfigEntry.Gui.CollapsibleObject
    public PlayerListConfig friend = new PlayerListConfig(0x20FF00);

    @ConfigEntry.Category(value = "players")
    @ConfigEntry.Gui.CollapsibleObject
    public PlayerListConfig enemy = new PlayerListConfig(0xD40000);

    @ConfigEntry.Category(value = "players")
    @ConfigEntry.Gui.CollapsibleObject
    public PlayerSingleConfig neutral = new PlayerSingleConfig();

    @ConfigEntry.Category(value = "players")
    @ConfigEntry.Gui.CollapsibleObject
    public PlayerSingleConfig self = new PlayerSingleConfig();

    @ConfigEntry.Category(value = "entity")
    @ConfigEntry.Gui.CollapsibleObject
    public Entity passive = new Entity();

    @ConfigEntry.Category(value = "entity")
    @ConfigEntry.Gui.CollapsibleObject
    public Entity hostile = new Entity();

    @ConfigEntry.Category(value = "entity")
    @ConfigEntry.Gui.CollapsibleObject
    public Entity decoration = new Entity();

    @ConfigEntry.Category(value = "entity")
    @ConfigEntry.Gui.CollapsibleObject
    public ProjectileEntity projectile = new ProjectileEntity();

    @ConfigEntry.Category(value = "entity")
    @ConfigEntry.Gui.CollapsibleObject
    public Entity vehicle = new Entity();

    @ConfigEntry.Category(value = "entity")
    @ConfigEntry.Gui.CollapsibleObject
    public EnderDragonEntity enderDragon = new EnderDragonEntity();

    @ConfigEntry.Category(value = "entity")
    @ConfigEntry.Gui.CollapsibleObject
    public MiscEntityDropdown misc = new MiscEntityDropdown();

    @ConfigEntry.Category(value = "oreo")
    @ConfigEntry.Gui.CollapsibleObject  //The sections with friendly teams
    public PlayerOreoListConfig friendteam = new PlayerOreoListConfig();

    @ConfigEntry.Category(value = "oreo")
    @ConfigEntry.Gui.CollapsibleObject //The section with enemy teams
    public PlayerOreoListConfig enemyteam = new PlayerOreoListConfig();

    @ConfigEntry.Category(value = "oreo")
    @ConfigEntry.Gui.CollapsibleObject //The prefix to team name list
    public PlayerOreoListConfig prefix = new PlayerOreoListConfig();

    @ConfigEntry.Category(value = "oreo")
    @ConfigEntry.Gui.CollapsibleObject //The huge section with the flag to waypoint settings
    public Oreo pingTowns = new Oreo();

    @ConfigEntry.Category(value = "oreo")
    @ConfigEntry.Gui.CollapsibleObject //Special towns section for sounds and notifications
    public Sounds specialTowns = new Sounds();



    public static class MiscEntityDropdown {
        public boolean isEnabled = false;

        @ConfigEntry.Gui.CollapsibleObject
        public MiscEntity areaEffectCloud = new MiscEntity();

        @ConfigEntry.Gui.CollapsibleObject
        public MiscEntity experienceOrb = new MiscEntity();

        @ConfigEntry.Gui.CollapsibleObject
        public MiscEntity eyeOfEnder = new MiscEntity();

        @ConfigEntry.Gui.CollapsibleObject
        public MiscEntity fallingBlock = new MiscEntity();

        @ConfigEntry.Gui.CollapsibleObject
        public MiscEntity item = new MiscEntity();

        @ConfigEntry.Gui.CollapsibleObject
        public MiscEntity tnt = new MiscEntity();

        @ConfigEntry.Gui.CollapsibleObject
        public MiscEntity endCrystalEntity = new MiscEntity();

        @ConfigEntry.Gui.CollapsibleObject
        public MiscEntity enderPearlEntity = new MiscEntity();

        @ConfigEntry.Gui.CollapsibleObject
        public MiscEntity tridentEntity = new MiscEntity();


    }

    public static class ProjectileEntity {

        public boolean isEnabled = false;

        @ConfigEntry.ColorPicker()
        public int color = 0xFFFFFF;

        @ConfigEntry.BoundedDiscrete(max = 10, min = 0)
        public int alpha = 10;

        public boolean renderStuck = false;
    }

    public static class Entity {
        public boolean isEnabled = false;

        @ConfigEntry.ColorPicker()
        public int color = 0xFFFFFF;

        @ConfigEntry.BoundedDiscrete(max = 10, min = 0)
        public int alpha = 10;
    }

    public static class MiscEntity {
        @ConfigEntry.ColorPicker()
        public int color = 0xFFFFFF;

        @ConfigEntry.BoundedDiscrete(max = 10, min = 0)
        public int alpha = 10;
    }

    public static class EnderDragonEntity {
        public boolean isEnabled = true;
        public boolean realHitbox = true;
        public boolean boxHitbox = false;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.ColorPicker()
        public int color = 0xFFFFFF;

        @ConfigEntry.BoundedDiscrete(max = 10, min = 0)
        public int alpha = 10;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.ColorPicker()
        public int partColor = 0xFFFFFF;

        @ConfigEntry.BoundedDiscrete(max = 10, min = 0)
        public int partAlpha = 10;
    }

    public static class PlayerSingleConfig {
        @ConfigEntry.ColorPicker()
        public int color = 0xFFFFFF;

        @ConfigEntry.BoundedDiscrete(max = 10, min = 0)
        public int alpha = 10;
    }

    public static class PlayerListConfig {

        public boolean acceptCoordsFromFriends = false; //Add the setting to accept coordinates from teammates

        public PlayerListConfig(int color) {
            this.color = color; //The color picker doesent work : (
        }
        public List<String> list = new ArrayList<>(); //Making the list

        @ConfigEntry.ColorPicker()
        public int color; //The integer of the color

        @ConfigEntry.BoundedDiscrete(max = 10, min = 0)
        public int alpha = 10; //The hitbbox alpha
    }

    public static class PlayerOreoListConfig {

        public List<String> oreolist = new ArrayList<>(); //This is just for a list
    }

    public static class Oreo {
        public boolean isPingingEnabled = true;

        public int yOffset = 200;

        public int removeCooldown = 240;
        public int liberateRemoveCooldown = 150;

        public List<String> oreoModList = new ArrayList<>();
        public List<String> enemyTownList = new ArrayList<>(); // you can make two lists in one class

        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ConfEnums.FlagLimiter limitRange = ConfEnums.FlagLimiter.DISABLED;

        public int pingDistanceLimit = 600;
    }

    public static class Sounds { //This is for the special town stuff
        public boolean playFlagSounds = false;

        public List<String> soundList = new ArrayList<>();

        @ConfigEntry.BoundedDiscrete(max = 5, min = 1)
        public int pitch = 1;

        public boolean showNotifications = false;
    }
}