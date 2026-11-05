package yesman.epicfight.nightfall;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/// Skill bawaan Yamato: Judgement Cut (klik skill), Helm Breaker (sambil sneak). Mengatur hunus/sarung dan dodge Yamato.
public class YamatoSkill extends WeaponInnateSkill {
	private static final int SHEATHE_DELAY_TICKS = 60;
	private static final Map<UUID, Integer> LAST_ACTION = new ConcurrentHashMap<>();
	
	public YamatoSkill(WeaponInnateSkill.Builder<?> builder) {
		super(builder);
	}
	
	public static boolean isDrawn(LivingEntityPatch<?> entitypatch) {
		if (entitypatch instanceof PlayerPatch<?> playerpatch) {
			SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
			
			if (container != null && container.getSkill() instanceof YamatoSkill) {
				return container.getDataManager().getDataValue(NightfallSkillDataKeys.YAMATO_DRAWN);
			}
		}
		
		return false;
	}
	
	@Override
	@ClientOnly
	public void gatherArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {
		var options = Minecraft.getInstance().options;
		arguments.putBoolean("sneak", options.keyShift.isDown());
		arguments.putBoolean("w", options.keyUp.isDown());
		arguments.putBoolean("s", options.keyDown.isDown());
		arguments.putBoolean("a", options.keyLeft.isDown());
		arguments.putBoolean("d", options.keyRight.isDown());
	}
	
	@Override
	public void executeOnServer(SkillContainer container, CompoundTag args) {
		ServerPlayerPatch executor = container.getServerExecutor();
		
		if (!executor.getEntityState().inaction()) {
			boolean sneak = args != null && args.getBoolean("sneak");
			boolean w = args != null && args.getBoolean("w");
			boolean back = args != null && args.getBoolean("s");
			boolean side = args != null && (args.getBoolean("a") || args.getBoolean("d"));
			AnimationAccessor<? extends StaticAnimation> animation;
			
			if (!executor.getOriginal().onGround()) {
				animation = YamatoMoreAnimations.AERIALFLUSH;
			} else if (sneak && w) {
				animation = YamatoMoreAnimations.UPPERSLASH;
			} else if (sneak && back) {
				animation = YamatoMoreAnimations.KILLERBEE;
			} else if (sneak && side) {
				animation = YamatoMoreAnimations.ORBIT_1;
			} else if (sneak) {
				animation = YamatoAnimations.HELMBREAKER;
			} else if (w) {
				animation = YamatoMoreAnimations.RAPIDSLASH;
			} else if (back) {
				animation = YamatoMoreAnimations.STOMP;
			} else if (side) {
				animation = YamatoMoreAnimations.FLARE_CUT;
			} else {
				animation = YamatoAnimations.JUDGEMENTCUT;
			}
			
			executor.playAnimationSynchronized(animation, 0.0F);
		}
		
		super.executeOnServer(container, args);
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		container.runOnServer(patch -> {
			ShadowDodge.restore(patch);
			LAST_ACTION.remove(patch.getOriginal().getUUID());
		});
	}
	
	@Override
	public void updateContainer(SkillContainer container) {
		super.updateContainer(container);
		container.runOnServer(patch -> {
			ShadowDodge.tick(patch, NightfallSkills.YAMATO_STEP.get());
			
			UUID id = patch.getOriginal().getUUID();
			int now = patch.getOriginal().tickCount;
			
			if (patch.getEntityState().inaction()) {
				LAST_ACTION.put(id, now);
			} else if (container.getDataManager().getDataValue(NightfallSkillDataKeys.YAMATO_DRAWN) && now - LAST_ACTION.getOrDefault(id, now) > SHEATHE_DELAY_TICKS) {
				container.getDataManager().setDataSync(NightfallSkillDataKeys.YAMATO_DRAWN, false);
			}
		});
	}
}
