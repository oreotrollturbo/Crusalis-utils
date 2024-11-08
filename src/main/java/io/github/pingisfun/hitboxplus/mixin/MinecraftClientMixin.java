package io.github.pingisfun.hitboxplus.mixin;

import io.github.pingisfun.hitboxplus.ModConfig;
import io.github.pingisfun.hitboxplus.util.ConfEnums;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {


    @Inject(at = @At("HEAD"), method = "doItemPick")
    private void toggleHostility(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!client.getEntityRenderDispatcher().shouldRenderHitboxes()) {
            return;
        }
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.isPlayerConfigEnabled) {
            return;
        }
        HitResult hit = client.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) {
            return;
        }
        EntityHitResult entityHit = (EntityHitResult) hit;

        if (entityHit.getEntity() instanceof OtherClientPlayerEntity) {
            if (config.middleClick == ConfEnums.PlayerListTypes.DISABLED) {
                return;
            }
            String name = entityHit.getEntity().getName().getContent().toString();
            boolean wasEnemy = config.enemy.list.remove(name);
            boolean wasFriend = config.friend.list.remove(name);
            // none -> friend -> enemy ->> none -> friend -> enemy ->>
            if (config.middleClick == ConfEnums.PlayerListTypes.CYCLE) {
                if (wasFriend && wasEnemy) {
                    assert true; // Do nothing
                } else if (!wasFriend && !wasEnemy) {
                    config.friend.list.add(name);
                } else if (wasFriend) {
                    config.enemy.list.add(name);
                }
            } else if (config.middleClick == ConfEnums.PlayerListTypes.FRIEND && !wasFriend) {
                config.friend.list.add(name);
            } else if (config.middleClick == ConfEnums.PlayerListTypes.ENEMY && !wasEnemy) {
                config.enemy.list.add(name);
            }

            AutoConfig.getConfigHolder(ModConfig.class).setConfig(config);
            AutoConfig.getConfigHolder(ModConfig.class).save();
        }

    }


    //@Inject(at = @At("HEAD"), method = "inGameHud") //This makes it so that you physically cant hit teammates (banned on crusalis)



//    @Inject(
//            method = "doAttack",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z"
//            ),
//            cancellable = true
//    )
//    private void dontAttackTeammates(CallbackInfoReturnable<Boolean> cir) {
//        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
//        HitResult crosshairTarget = ((MinecraftClient) (Object) this).crosshairTarget;
//
//        if (
//                crosshairTarget != null
//                        && crosshairTarget.getType() == HitResult.Type.ENTITY
//                        && ((EntityHitResult) crosshairTarget).getEntity() instanceof OtherClientPlayerEntity p
//
//        ){
//            OtherClientPlayerEntity otherPlayer = (OtherClientPlayerEntity) ((EntityHitResult) crosshairTarget).getEntity();
//
//
//
//            if (config.protectedPlayers && isProtected(otherPlayer)){
//                cir.setReturnValue(false);
//            }
//
//        }
//    }

//    @Unique
//    public boolean isProtected (OtherClientPlayerEntity player){
//        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
//
//        assert MinecraftClient.getInstance().player != null;
//        return (MinecraftClient.getInstance().player.isTeammate(player)) || (config.friendteam.prefixlist.contains(Objects.requireNonNull(player.getScoreboardTeam()).getName()));
//
//    }

}
