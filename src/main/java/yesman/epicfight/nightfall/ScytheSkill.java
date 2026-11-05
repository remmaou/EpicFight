package yesman.epicfight.nightfall;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.platform.fabric.util.FabricHolders;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.utils.side.ClientOnly;
import net.minecraft.world.damagesource.DamageSource;
import yesman.epicfight.client.events.engine.ControlEngine;

/// Skill bawaan Crimson Moon: Blood Power (0..1000), efek Curse of Blood / Blood Blessing, mode sarung, Scarlet End.
/// Port dari ScytheSkill Epic Fight Nightfall 3.4.2.
public class ScytheSkill extends WeaponInnateSkill {
	private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "textures/gui/crimson_moon/background.png");
	private static final ResourceLocation CHARGE = ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "textures/gui/crimson_moon/charge.png");
	private static final ResourceLocation CHARGE_FULL = ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "textures/gui/crimson_moon/charge_full.png");
	
	private static final ResourceLocation S_BG = ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "textures/gui/shadow/background.png");
	private static final ResourceLocation S_CHARGE = ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "textures/gui/shadow/charge.png");
	private static final ResourceLocation S_CHARGE_FULL = ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "textures/gui/shadow/charge_full.png");
	
	public static final float MAX_BLOOD_POWER = 1000.0F;
	
	private static boolean isShadow(LivingEntity entity) {
		return entity.getMainHandItem().is(NightfallItems.SHADOW.get());
	}
	
	public ScytheSkill(WeaponInnateSkill.Builder<?> builder) {
		super(builder);
	}
	
	public static float getMaxBloodPower() {
		return MAX_BLOOD_POWER;
	}
	
	private static float getPower(SkillContainer container) {
		return container.getDataManager().getDataValue(NightfallSkillDataKeys.SCYTHE_BLOOD_POWER);
	}
	
	private static void addPower(SkillContainer container, float amount) {
		container.getDataManager().setDataSync(NightfallSkillDataKeys.SCYTHE_BLOOD_POWER, Mth.clamp(getPower(container) + amount, 0.0F, MAX_BLOOD_POWER));
	}
	
	private static boolean isSheathed(SkillContainer container) {
		return container.getDataManager().getDataValue(NightfallSkillDataKeys.SCYTHE_SHEATH);
	}
	
	private static boolean isLastPhase(AnimationAccessor<? extends StaticAnimation> animation, AnimationPlayer player) {
		if (player == null || !(animation.get() instanceof AttackAnimation attackAnimation)) {
			return false;
		}
		
		return attackAnimation.getPhaseOrderByTime(player.getElapsedTime()) == attackAnimation.phases.length - 1;
	}
	
	@Override
	public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
		super.onInitiate(container, eventListener);
		
		// Mode sarung: aksi apa pun dibatalkan dulu dan senjata dihunus
		eventListener.registerEvent(EpicFightEventHooks.Player.CAST_SKILL, event -> {
			if (this.isDisabled(container) && container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory() == CapabilityItem.WeaponCategories.FIST) {
				return;
			}
			
			if (isSheathed(container) && container.getExecutor().getEntityState().canUseSkill()) {
				Skill skill = event.getSkillContainer().getSkill();
				
				if (skill instanceof yesman.epicfight.skill.common.ComboAttacks || skill instanceof ScytheSkill || event.getSkillContainer() == container.getExecutor().getSkill(SkillSlots.GUARD)) {
					event.cancel();
					container.runOnServer(serverExecutor -> serverExecutor.playAnimationSynchronized(NightfallScytheAnimations.SCYTHE_UNSHEATHED, 0.0F));
				}
			}
		}, this);
		
		// Dodge hemat stamina saat disarungkan
		eventListener.registerEvent(EpicFightEventHooks.Player.CONSUME_SKILL, event -> {
			if (!this.isDisabled(container) && isSheathed(container) && event.getSkill().getCategory() == SkillCategories.DODGE) {
				event.setAmount(event.getAmount() * 0.25F);
			}
		}, this);
		
		// Sebelum damage: tag dan pengali
		eventListener.registerEvent(EpicFightEventHooks.Entity.DELIVER_DAMAGE_PRE, event -> {
			if (this.isDisabled(container)) {
				return;
			}
			
			EpicFightDamageSource source = event.getDamageSource();
			LivingEntity target = event.getTarget();
			Player player = container.getExecutor().getOriginal();
			AnimationAccessor<? extends StaticAnimation> animation = source.getAnimation();
			
			if (animation == NightfallScytheAnimations.SCYTHE_SCARLET_END) {
				source.addRuntimeTag(EpicFightDamageTypeTags.BYPASS_DODGE);
				source.addRuntimeTag(EpicFightDamageTypeTags.GUARD_PUNCTURE);
				source.addRuntimeTag(EpicFightDamageTypeTags.UNBLOCKALBE);
				source.addRuntimeTag(EpicFightDamageTypeTags.FINISHER);
				source.addRuntimeTag(EpicFightDamageTypeTags.IS_MELEE);
				source.addRuntimeTag(EpicFightDamageTypeTags.COUNTER);
				
				AnimationPlayer animationPlayer = container.getExecutor().getAnimator().getPlayerFor(null);
				
				if (animationPlayer != null) {
					if (!isLastPhase(animation, animationPlayer)) {
						source.addExtraDamage(NightfallExtraDamage.EX_DAMAGE_BY_COB_EFFECT.create(0.064F, 0.0F));
					} else {
						source.addExtraDamage(NightfallExtraDamage.EX_DAMAGE_BY_COB_EFFECT.create(0.112F, 0.003F));
					}
				}
			}
			
			if (animation == NightfallScytheAnimations.SCYTHE_HARVEST) {
				if (target.hasEffect(NightfallMobEffects.real(NightfallMobEffects.CURSE_OF_BLOOD)) || player.hasEffect(NightfallMobEffects.real(NightfallMobEffects.BLOOD_BLESSINGS))) {
					source.attachDamageModifier(ValueModifier.multiplier(1.25F));
				} else {
					source.attachDamageModifier(ValueModifier.multiplier(0.25F));
				}
			}
		}, this);
		
		// Setelah damage: efek darah dan Blood Power
		eventListener.registerEvent(EpicFightEventHooks.Entity.DELIVER_DAMAGE_POST, event -> {
			if (this.isDisabled(container)) {
				return;
			}
			
			EpicFightDamageSource source = event.getDamageSource();
			Player player = container.getExecutor().getOriginal();
			LivingEntity target = event.getTarget();
			AnimationAccessor<? extends StaticAnimation> animation = source.getAnimation();
			
			if ((animation == NightfallScytheAnimations.SCYTHE_HARVEST || animation == NightfallScytheAnimations.SCYTHE_SCARLET_END) && target.level() instanceof ServerLevel bloodLevel) {
				Vec3 bp = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D);
				bloodLevel.sendParticles((isShadow(player) ? NightfallParticles.SHADOW_HARVEST : NightfallParticles.BLOOD_HARVEST).get(), bp.x, bp.y, bp.z, 0, bloodLevel.random.nextFloat() * 360.0F, 1.6D, 0.0D, 1.0D);
			}
			
			if (animation == NightfallScytheAnimations.SCYTHE_SCARLET_END) {
				MobEffectInstance curse = target.getEffect(NightfallMobEffects.real(NightfallMobEffects.CURSE_OF_BLOOD));
				MobEffectInstance blessing = player.getEffect(NightfallMobEffects.real(NightfallMobEffects.BLOOD_BLESSINGS));
				int amplifier = 0;
				
				if (curse != null) {
					amplifier = curse.getAmplifier() + 1;
				} else if (blessing != null) {
					amplifier = blessing.getAmplifier() + 1;
				}
				
				player.heal(player.getMaxHealth() * 0.03F * amplifier);
				
				if (player.level() instanceof ServerLevel serverLevel) {
					Vec3 pos = player.position().add(0.0D, player.getBbHeight() / 2.0F, 0.0D);
					serverLevel.sendParticles((isShadow(player) ? NightfallParticles.SHADOW_DROP : NightfallParticles.BLOOD_DROP).get(), pos.x, pos.y, pos.z, 4, 0.35D, 0.6D, 0.35D, 0.6D);
				}
				
				if (isLastPhase(animation, container.getExecutor().getAnimator().getPlayerFor(null))) {
					if (target.hasEffect(NightfallMobEffects.real(NightfallMobEffects.CURSE_OF_BLOOD))) {
						target.removeEffect(NightfallMobEffects.real(NightfallMobEffects.CURSE_OF_BLOOD));
					} else if (player.hasEffect(NightfallMobEffects.real(NightfallMobEffects.BLOOD_BLESSINGS))) {
						player.removeEffect(NightfallMobEffects.real(NightfallMobEffects.BLOOD_BLESSINGS));
					}
				}
				
				return;
			}
			
			float powerIncreased = 20.0F;
			boolean canBeAffected = target.canBeAffected(new MobEffectInstance(NightfallMobEffects.real(NightfallMobEffects.CURSE_OF_BLOOD), 0, 0, false, false, true));
			
			if (animation == NightfallScytheAnimations.SCYTHE_AUTO1 || animation == NightfallScytheAnimations.SCYTHE_RPG_AUTO1 || animation == NightfallScytheAnimations.SCYTHE_RPG_WHIRL || animation == NightfallScytheAnimations.SCYTHE_RPG_SLAM || animation == NightfallScytheAnimations.SCYTHE_AUTO3 || animation == NightfallScytheAnimations.SCYTHE_AUTO5) {
				// Serangan biasa menumpuk Curse of Blood pada musuh (atau Blood Blessing pada pemain bila musuh kebal)
				LivingEntity effectHolder = canBeAffected ? target : player;
				net.minecraft.core.Holder<MobEffect> effect = canBeAffected ? NightfallMobEffects.real(NightfallMobEffects.CURSE_OF_BLOOD) : NightfallMobEffects.real(NightfallMobEffects.BLOOD_BLESSINGS);
				MobEffectInstance instance = effectHolder.getEffect(effect);
				int level = instance != null ? instance.getAmplifier() : -1;
				
				effectHolder.addEffect(new MobEffectInstance(effect, 400, Mth.clamp(level + 1, 0, 255), false, false, true));
			} else if (animation == NightfallScytheAnimations.SCYTHE_DASH || animation == NightfallScytheAnimations.SCYTHE_AIR_SLASH || animation == NightfallScytheAnimations.SCYTHE_HARVEST) {
				// Serangan skill menukar satu tumpukan darah menjadi buff
				LivingEntity removeTarget = canBeAffected ? target : player;
				net.minecraft.core.Holder<MobEffect> effect = canBeAffected ? NightfallMobEffects.real(NightfallMobEffects.CURSE_OF_BLOOD) : NightfallMobEffects.real(NightfallMobEffects.BLOOD_BLESSINGS);
				MobEffectInstance stack = removeTarget.getEffect(effect);
				
				if (stack != null) {
					int targetLevel = stack.getAmplifier() - 1;
					
					if (targetLevel >= 0) {
						removeTarget.addEffect(new MobEffectInstance(effect, stack.getDuration(), targetLevel, false, false, true), target);
					} else {
						removeTarget.removeEffect(effect);
					}
					
					if (animation == NightfallScytheAnimations.SCYTHE_DASH) {
						MobEffectInstance reduction = player.getEffect(NightfallMobEffects.real(NightfallMobEffects.DAMAGE_REDUCTION));
						int level = reduction != null ? reduction.getAmplifier() : -1;
						player.addEffect(new MobEffectInstance(NightfallMobEffects.real(NightfallMobEffects.DAMAGE_REDUCTION), 300, Mth.clamp(level + 25, 0, 75), false, false, true));
					}
					
					if (animation == NightfallScytheAnimations.SCYTHE_AIR_SLASH) {
						MobEffectInstance speed = player.getEffect(NightfallMobEffects.real(NightfallMobEffects.ATTACK_SPEED_INCREASE));
						int level = speed != null ? speed.getAmplifier() : -1;
						player.addEffect(new MobEffectInstance(NightfallMobEffects.real(NightfallMobEffects.ATTACK_SPEED_INCREASE), 300, Mth.clamp(level + 15, 0, 44), false, false, true));
					}
					
					if (animation == NightfallScytheAnimations.SCYTHE_HARVEST) {
						MobEffectInstance damage = player.getEffect(NightfallMobEffects.real(NightfallMobEffects.ATTACK_DAMAGE_INCREASE));
						int level = damage != null ? damage.getAmplifier() : -1;
						player.addEffect(new MobEffectInstance(NightfallMobEffects.real(NightfallMobEffects.ATTACK_DAMAGE_INCREASE), 300, Mth.clamp(level + 15, 0, 89), false, false, true));
						player.addEffect(new MobEffectInstance(NightfallMobEffects.real(EpicFightMobEffects.STUN_IMMUNITY), 60, 0, false, false, true));
						target.level().playSound(null, target.blockPosition(), EpicFightSounds.BLADE_RUSH_FINISHER.get(), SoundSource.HOSTILE);
						powerIncreased += 60.0F;
					}
				}
			}
			
			addPower(container, powerIncreased);
		}, this);
		
		// Parry menambah Blood Power; Scarlet End kebal serangan; Damage Reduction membuat tidak terpental
		eventListener.registerEvent(EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME, event -> {
			if (this.isDisabled(container)) {
				return;
			}
			
			if (event.isParried()) {
				container.getExecutor().getSkill(SkillSlots.COMBO_ATTACKS).getDataManager().setDataSync(EpicFightSkillDataKeys.COMBO_COUNTER, 2);
				addPower(container, 10.0F);
			}
			
			AnimationPlayer animationPlayer = container.getExecutor().getAnimator().getPlayerFor(null);
			
			if (animationPlayer != null) {
				if (container.getExecutor().getOriginal().hasEffect(NightfallMobEffects.real(NightfallMobEffects.DAMAGE_REDUCTION)) && animationPlayer.getAnimation().get() instanceof AttackAnimation) {
					DamageSource source = event.getDamageSource();
					
					if (source instanceof EpicFightDamageSource epicFightSource) {
						epicFightSource.setStunType(StunType.NONE);
					}
				}
				
				if (animationPlayer.getAnimation().get().getRealAnimation().equals(NightfallScytheAnimations.SCYTHE_SCARLET_END)) {
					event.cancel();
					event.setResult(AttackResult.ResultType.BLOCKED);
					event.setParried(true);
				}
			}
		}, this);
		
		// Damage Reduction: kurangi damage yang diterima (level+1)% maks 75%
		eventListener.registerEvent(EpicFightEventHooks.Entity.TAKE_DAMAGE_PRE, event -> {
			MobEffectInstance reduction = container.getExecutor().getOriginal().getEffect(NightfallMobEffects.real(NightfallMobEffects.DAMAGE_REDUCTION));
			
			if (reduction != null && !this.isDisabled(container)) {
				float percent = Mth.clamp((reduction.getAmplifier() + 1) / 100.0F, 0.0F, 0.75F);
				event.attachValueModifier(ValueModifier.multiplier(1.0F - percent));
			}
		}, this);
	}
	
	@Override
	@ClientOnly
	public void gatherArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {
		// Tombol pakai (klik kanan) ditahan = sarungkan senjata
		arguments.putBoolean("sheath", Minecraft.getInstance().options.keyUse.isDown());
	}
	
	@Override
	public void executeOnServer(SkillContainer container, CompoundTag args) {
		boolean sheath = isSheathed(container);
		float power = getPower(container);
		ServerPlayerPatch executor = container.getServerExecutor();
		
		if (!sheath) {
			if (args != null && args.getBoolean("sheath") && !executor.getEntityState().inaction()) {
				executor.playAnimationSynchronized(NightfallScytheAnimations.SCYTHE_SHEATHED, 0.0F);
			} else if (power >= MAX_BLOOD_POWER && executor.getHoldingSkill() instanceof GuardSkill) {
				container.getDataManager().setDataSync(NightfallSkillDataKeys.SCYTHE_BLOOD_POWER, 0.0F);
				executor.playAnimationSynchronized(NightfallScytheAnimations.SCYTHE_SCARLET_END, 0.0F);
			} else {
				executor.playAnimationSynchronized(NightfallScytheAnimations.SCYTHE_HARVEST, 0.0F);
			}
		}
		
		super.executeOnServer(container, args);
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		container.runOnServer(ShadowDodge::restore);
	}
	
	@Override
	public void updateContainer(SkillContainer container) {
		super.updateContainer(container);
		container.runOnServer(shadowPatch -> ShadowDodge.tick(shadowPatch, isShadow(shadowPatch.getOriginal())));
		
		container.runOnServer(serverExecutor -> {
			if (container.isDisabled() || serverExecutor.getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory() == CapabilityItem.WeaponCategories.FIST || !isSheathed(container)) {
				return;
			}
			
			ServerPlayer player = serverExecutor.getOriginal();
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 2, false, false, true));
			
			// Disarungkan: darah menyembuhkan HP
			float current = getPower(container);
			float healthRatio = player.getHealth() / player.getMaxHealth();
			float healPercent = Mth.clamp(1.0F - healthRatio, 0.0F, 0.2F);
			float cost = Mth.clamp(healPercent / 0.2F * MAX_BLOOD_POWER * 0.1F, 0.0F, 100.0F);
			float finalHeal = current >= cost ? 0.2F : current / 100.0F * 0.2F;
			
			if (finalHeal > 0.0F && healthRatio < 1.0F && player.tickCount % 20 == 0) {
				player.heal(player.getMaxHealth() * finalHeal);
				container.getDataManager().setDataSync(NightfallSkillDataKeys.SCYTHE_BLOOD_POWER, Mth.clamp(current - cost, 0.0F, MAX_BLOOD_POWER));
				
				if (player.level() instanceof ServerLevel serverLevel) {
					Vec3 pos = player.position().add(0.0D, player.getBbHeight() / 2.0F, 0.0D);
					serverLevel.sendParticles((isShadow(player) ? NightfallParticles.SHADOW_DROP : NightfallParticles.BLOOD_DROP).get(), pos.x, pos.y, pos.z, 4, 0.35D, 0.6D, 0.35D, 0.6D);
				}
			}
		});
	}
	
	@Override
	@ClientOnly
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		float current = getPower(container);
		boolean shadowGui = isShadow(container.getExecutor().getOriginal());
		ResourceLocation guiBg = shadowGui ? S_BG : BG;
		ResourceLocation guiCharge = shadowGui ? S_CHARGE : CHARGE;
		ResourceLocation guiChargeFull = shadowGui ? S_CHARGE_FULL : CHARGE_FULL;
		float chargePercent = Mth.clamp(current / MAX_BLOOD_POWER, 0.0F, 1.0F);
		String powerPercent = String.format("%.0f", chargePercent * 100.0F) + "%";
		
		if (chargePercent < 1.0F) {
			FabricHolders.drawString(guiGraphics, gui.getFont(), powerPercent, x - gui.getFont().width(powerPercent) / 2.0F, y + 32.0F - gui.getFont().lineHeight, 0xFFFFFF, true);
		}
		
		if (chargePercent == 1.0F) {
			guiGraphics.blit(guiChargeFull, (int)x, (int)y, 0.0F, 0.0F, 32, 32, 32, 32);
		} else {
			guiGraphics.blit(guiCharge, (int)x, (int)y, 0.0F, 0.0F, 32, 32, 32, 32);
			guiGraphics.blit(guiBg, (int)x, (int)y, 0.0F, 0.0F, 32, Math.round(32.0F * (1.0F - chargePercent)), 32, 32);
		}
		
	}
	
	@Override
	public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
		List<Component> list = Lists.newArrayList();
		
		list.add(Component.translatable("skill.efn.scythe_skill.tooltip").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
		
		for (int i = 1; i <= 6; i++) {
			list.add(Component.translatable("skill.efn.scythe_skill.tooltip" + i));
		}
		
		return list;
	}
}
