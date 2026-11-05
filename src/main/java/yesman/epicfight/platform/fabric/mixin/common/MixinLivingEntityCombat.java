package yesman.epicfight.platform.fabric.mixin.common;

import yesman.epicfight.platform.fabric.shim.clientevent.LivingJumpEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.platform.fabric.util.DropCapture;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

/// Core combat hooks that NeoForge provides through its patched vanilla (LivingIncomingDamageEvent, LivingDamageEvent,
/// LivingKnockBackEvent, LivingDeathEvent, LivingDropsEvent, LivingShieldBlockEvent, LivingJumpEvent, MobEffectEvent).
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityCombat {
    @Unique
    private float epicfight$totalHealthBeforeHurt;

    // LivingIncomingDamageEvent: cancellable, fired at the start of hurt()
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void epicfight$incomingDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (VanillaEntityEventHooks.onDamageIncomes((LivingEntity)(Object)this, source, amount)) {
            cir.setReturnValue(false);
        }
    }

    // LivingDamageEvent.Pre: lets Epic Fight modify the raw damage before armor/enchantment/absorption
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float epicfight$damagePre(float amount, DamageSource source) {
        float[] holder = {amount};
        VanillaEntityEventHooks.onCalculateDamagePre((LivingEntity)(Object)this, source, amount, newAmount -> holder[0] = newAmount);
        return holder[0];
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void epicfight$recordHealth(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        this.epicfight$totalHealthBeforeHurt = self.getHealth() + self.getAbsorptionAmount();
    }

    // LivingDamageEvent.Post: damage actually dealt (health + absorption delta)
    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void epicfight$damagePost(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        float dealt = this.epicfight$totalHealthBeforeHurt - (self.getHealth() + self.getAbsorptionAmount());
        VanillaEntityEventHooks.onCalculateDamagePost(self, source, Math.max(dealt, 0.0F));
    }

    // LivingKnockBackEvent
    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void epicfight$knockback(double strength, double x, double z, CallbackInfo ci) {
        if (VanillaEntityEventHooks.onKnockedBack((LivingEntity)(Object)this)) {
            ci.cancel();
        }
    }

    // LivingDeathEvent
    @Inject(method = "die", at = @At("HEAD"))
    private void epicfight$die(DamageSource source, CallbackInfo ci) {
        VanillaEntityEventHooks.onLivingDeath((LivingEntity)(Object)this, source);
    }

    // LivingDropsEvent: capture item drops spawned during dropAllDeathLoot, then let the patch cancel them
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void epicfight$beginDropCapture(ServerLevel level, DamageSource source, CallbackInfo ci) {
        DropCapture.begin((LivingEntity)(Object)this);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void epicfight$endDropCapture(ServerLevel level, DamageSource source, CallbackInfo ci) {
        DropCapture capture = DropCapture.current();
        DropCapture.end();

        if (capture == null || capture.owner != (Object)this) {
            return;
        }

        LivingEntity self = (LivingEntity)(Object)this;

        if (!VanillaEntityEventHooks.onDropItems(self, source, capture.drops)) {
            capture.drops.forEach(itemEntity -> level.addFreshEntity(itemEntity));
        }
    }

    // LivingShieldBlockEvent: fired when a shield actually absorbs the hit
    @Inject(method = "hurtCurrentlyUsedShield", at = @At("HEAD"))
    private void epicfight$shieldBlock(float damage, CallbackInfo ci) {
        VanillaEntityEventHooks.onBlockAttacksWithShield((LivingEntity)(Object)this);
    }

    // LivingJumpEvent
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void epicfight$jump(CallbackInfo ci) {
        VanillaEntityEventHooks.onJump((LivingEntity)(Object)this);
    }

    // MobEffectEvent.Added
    @Inject(method = "onEffectAdded", at = @At("TAIL"))
    private void epicfight$effectAdded(MobEffectInstance instance, Entity source, CallbackInfo ci) {
        VanillaEntityEventHooks.onMobEffectAdded(instance, (LivingEntity)(Object)this);
    }

    // MobEffectEvent.Remove / Expired (vanilla routes both through onEffectRemoved)
    @Inject(method = "onEffectRemoved", at = @At("TAIL"))
    private void epicfight$effectRemoved(MobEffectInstance instance, CallbackInfo ci) {
        VanillaEntityEventHooks.onMobEffectRemoved(instance, (LivingEntity)(Object)this);
    }

    // LivingEntityUseItemEvent.Start (cancellable): blocks item use during actions or when the style forbids offhand use
    @Inject(method = "startUsingItem", at = @At("HEAD"), cancellable = true)
    private void epicfight$startUsingItem(InteractionHand hand, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (!(self instanceof Player)) {
            return;
        }

        EpicFightCapabilities.getPlayerPatchAsOptional(self).ifPresent(playerpatch -> {
            if (!playerpatch.getEntityState().canUseItem()) {
                ci.cancel();
            } else if (hand == InteractionHand.OFF_HAND && !playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getStyle(playerpatch).canUseOffhand()) {
                ci.cancel();
            }
        });
    }
}
