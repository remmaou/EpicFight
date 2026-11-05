package yesman.epicfight.nightfall;

import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public final class NightfallWeaponPresets {
	private NightfallWeaponPresets() {}
	
	/// Tipe senjata "efn:scythe" (dipakai data/efn/capabilities/weapons/crimson_moon.json)
	private static CapabilityItem.Builder<?> build(boolean shadow) {
		WeaponCapability.Builder b = WeaponCapability.builder();
		b.category(CapabilityItem.WeaponCategories.LONGSWORD);
		b.styleProvider((entitypatch) -> {
			if (entitypatch instanceof PlayerPatch<?> playerpatch) {
				SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
				
				if (container != null && !container.isEmpty() && container.getSkill() instanceof ScytheSkill
					&& container.getDataManager().getDataValueOptional(NightfallSkillDataKeys.SCYTHE_SHEATH).orElse(false)) {
					return CapabilityItem.Styles.SHEATH;
				}
			}
			
			return CapabilityItem.Styles.TWO_HAND;
		});
		b.hitSound(EpicFightSounds.BLADE_HIT.get());
		b.hitParticle(NightfallParticles.CRIMSON_HIT);
		b.collider(ColliderPreset.LONGSWORD);
		b.canBePlacedOffhand(false);
		b.reach(0.45F);
		if (shadow) {
			b.newStyleCombo(CapabilityItem.Styles.SHEATH, NightfallScytheAnimations.SCYTHE_SHADOW_AUTO1, NightfallScytheAnimations.SCYTHE_SHADOW_AUTO2, NightfallScytheAnimations.SCYTHE_SHADOW_WHIRL);
		} else {
			b.newStyleCombo(CapabilityItem.Styles.SHEATH, NightfallScytheAnimations.SCYTHE_RPG_AUTO1, NightfallScytheAnimations.SCYTHE_RPG_AUTO2, NightfallScytheAnimations.SCYTHE_RPG_WHIRL);
		}
		if (shadow) {
			b.newStyleCombo(CapabilityItem.Styles.TWO_HAND,
				NightfallScytheAnimations.SCYTHE_SHADOW_AUTO1,
				NightfallScytheAnimations.SCYTHE_SHADOW_AUTO2,
				NightfallScytheAnimations.SCYTHE_SHADOW_WHIRL,
				NightfallScytheAnimations.SCYTHE_SHADOW_LEAP,
				NightfallScytheAnimations.SCYTHE_SHADOW_SLAM,
				NightfallScytheAnimations.SCYTHE_DASH,
				NightfallScytheAnimations.SCYTHE_AIR_SLASH);
		} else {
			b.newStyleCombo(CapabilityItem.Styles.TWO_HAND,
			NightfallScytheAnimations.SCYTHE_RPG_AUTO1,
			NightfallScytheAnimations.SCYTHE_RPG_AUTO2,
			NightfallScytheAnimations.SCYTHE_RPG_WHIRL,
			NightfallScytheAnimations.SCYTHE_RPG_LEAP,
			NightfallScytheAnimations.SCYTHE_RPG_SLAM,
			NightfallScytheAnimations.SCYTHE_DASH,
			NightfallScytheAnimations.SCYTHE_AIR_SLASH);
		}
		b.innateSkill(CapabilityItem.Styles.SHEATH, (itemstack) -> NightfallSkills.SCYTHE_SKILL.get());
		b.innateSkill(CapabilityItem.Styles.TWO_HAND, (itemstack) -> NightfallSkills.SCYTHE_SKILL.get());
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.BLOCK, NightfallScytheAnimations.SCYTHE_BLOCK);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.IDLE, NightfallScytheAnimations.SCYTHE_IDLE_COMBAT);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.WALK, NightfallScytheAnimations.SCYTHE_WALK_COMBAT);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.CHASE, NightfallScytheAnimations.SCYTHE_RUN_COMBAT);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.RUN, NightfallScytheAnimations.SCYTHE_RUN_COMBAT);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.KNEEL, NightfallScytheAnimations.SCYTHE_IDLE_COMBAT);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.SNEAK, NightfallScytheAnimations.SCYTHE_WALK_COMBAT);
		b.livingMotionModifier(CapabilityItem.Styles.SHEATH, LivingMotions.IDLE, NightfallScytheAnimations.SCYTHE_IDLE_NORMAL);
		b.livingMotionModifier(CapabilityItem.Styles.SHEATH, LivingMotions.WALK, NightfallScytheAnimations.SCYTHE_WALK_NORMAL);
		b.livingMotionModifier(CapabilityItem.Styles.SHEATH, LivingMotions.CHASE, NightfallScytheAnimations.SCYTHE_RUN_NORMAL);
		b.livingMotionModifier(CapabilityItem.Styles.SHEATH, LivingMotions.RUN, NightfallScytheAnimations.SCYTHE_RUN_NORMAL);
		b.livingMotionModifier(CapabilityItem.Styles.SHEATH, LivingMotions.KNEEL, NightfallScytheAnimations.SCYTHE_IDLE_NORMAL);
		b.livingMotionModifier(CapabilityItem.Styles.SHEATH, LivingMotions.SNEAK, NightfallScytheAnimations.SCYTHE_WALK_NORMAL);
		return b;
	}
	
	public static final Function<Item, CapabilityItem.Builder<?>> YAMATO = (item) -> {
		WeaponCapability.Builder b = WeaponCapability.builder();
		b.category(CapabilityItem.WeaponCategories.UCHIGATANA);
		b.styleProvider((entitypatch) -> entitypatch.getOriginal().isShiftKeyDown() ? CapabilityItem.Styles.ONE_HAND : CapabilityItem.Styles.TWO_HAND);
		b.hitSound(EpicFightSounds.BLADE_HIT.get());
		b.hitParticle(yesman.epicfight.registry.entries.EpicFightParticles.HIT_BLADE);
		b.collider(ColliderPreset.UCHIGATANA);
		b.canBePlacedOffhand(false);
		b.newStyleCombo(CapabilityItem.Styles.TWO_HAND, YamatoAnimations.SLASHER_1, YamatoAnimations.SLASHER_2, YamatoAnimations.SLASHER_3, YamatoAnimations.DRIVE, YamatoAnimations.AERIALRAVE_1);
		b.innateSkill(CapabilityItem.Styles.TWO_HAND, (itemstack) -> NightfallSkills.YAMATO.get());
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.IDLE, YamatoAnimations.IDLE);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.KNEEL, YamatoAnimations.KNEEL);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.SNEAK, YamatoAnimations.SNEAK);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.JUMP, YamatoAnimations.JUMP);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.WALK, YamatoAnimations.WALK);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.CHASE, YamatoAnimations.RUN);
		b.livingMotionModifier(CapabilityItem.Styles.TWO_HAND, LivingMotions.RUN, YamatoAnimations.RUN);
		b.newStyleCombo(CapabilityItem.Styles.ONE_HAND, YamatoMoreAnimations.DIVORCE_1, YamatoMoreAnimations.DIVORCE_2, YamatoMoreAnimations.DIVORCE_3, YamatoAnimations.DRIVE, YamatoAnimations.AERIALRAVE_1);
		b.innateSkill(CapabilityItem.Styles.ONE_HAND, (itemstack) -> NightfallSkills.YAMATO.get());
		b.livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.IDLE, YamatoAnimations.IDLE);
		b.livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.KNEEL, YamatoAnimations.KNEEL);
		b.livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.SNEAK, YamatoAnimations.SNEAK);
		b.livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.JUMP, YamatoAnimations.JUMP);
		b.livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.WALK, YamatoAnimations.WALK);
		b.livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.CHASE, YamatoAnimations.RUN);
		b.livingMotionModifier(CapabilityItem.Styles.ONE_HAND, LivingMotions.RUN, YamatoAnimations.RUN);
		return b;
	};
	
	public static final Function<Item, CapabilityItem.Builder<?>> SCYTHE = (item) -> build(false);
	public static final Function<Item, CapabilityItem.Builder<?>> SHADOW = (item) -> build(true);
	
	public static void register(WeaponCapabilityPresetRegistryEvent event) {
		event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "scythe"), SCYTHE);
		event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "yamato"), YAMATO);
		event.getTypeEntry().put(ResourceLocation.fromNamespaceAndPath(NightfallRegistries.MODID, "scythe_shadow"), SHADOW);
	}
}
